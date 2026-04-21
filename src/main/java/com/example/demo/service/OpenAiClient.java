package com.example.demo.service;

import com.example.demo.entity.ClientPortfolio;
import com.example.demo.entity.PortfolioHolding;
import com.example.demo.entity.StockHolding;
import com.example.demo.repository.ClientPortfolioRepository;
import com.example.demo.repository.PortfolioHoldingRepository;
import com.example.demo.repository.StockHoldingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Autowired
    private ClientPortfolioRepository portfolioRepo;

    @Autowired
    private PortfolioHoldingRepository holdingRepo;

    @Autowired
    private StockHoldingRepository stockRepo;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // ─── Result wrapper: recommendations + trace ────────────────────────────────
    public static class AgentResult {
        public List<Map<String, Object>> recommendations;
        public List<Map<String, String>> trace;
        public AgentResult() {}
        public AgentResult(List<Map<String, Object>> r, List<Map<String, String>> t) {
            this.recommendations = r;
            this.trace = t;
        }
    }

    // ─── Tool Definitions for OpenAI ────────────────────────────────────────────
    private ArrayNode buildTools() {
        ArrayNode tools = mapper.createArrayNode();

        ObjectNode t1 = mapper.createObjectNode();
        t1.put("type", "function");
        ObjectNode f1 = mapper.createObjectNode();
        f1.put("name", "get_client_profile");
        f1.put("description", "Fetch the client's risk level and current/target asset allocations from the database.");
        ObjectNode p1 = mapper.createObjectNode();
        p1.put("type", "object");
        ObjectNode props1 = mapper.createObjectNode();
        ObjectNode clientIdProp = mapper.createObjectNode();
        clientIdProp.put("type", "string");
        clientIdProp.put("description", "The unique client ID e.g. U1001");
        props1.set("client_id", clientIdProp);
        p1.set("properties", props1);
        p1.set("required", mapper.createArrayNode().add("client_id"));
        f1.set("parameters", p1);
        t1.set("function", f1);
        tools.add(t1);

        ObjectNode t2 = mapper.createObjectNode();
        t2.put("type", "function");
        ObjectNode f2 = mapper.createObjectNode();
        f2.put("name", "get_fund_holdings");
        f2.put("description", "Fetch all mutual fund holdings for a given client portfolio ID from the database.");
        ObjectNode p2 = mapper.createObjectNode();
        p2.put("type", "object");
        ObjectNode props2 = mapper.createObjectNode();
        ObjectNode portfolioIdProp = mapper.createObjectNode();
        portfolioIdProp.put("type", "string");
        portfolioIdProp.put("description", "The internal portfolio ID returned from get_client_profile.");
        props2.set("portfolio_id", portfolioIdProp);
        p2.set("properties", props2);
        p2.set("required", mapper.createArrayNode().add("portfolio_id"));
        f2.set("parameters", p2);
        t2.set("function", f2);
        tools.add(t2);

        return tools;
    }

    // ─── Tool Execution ─────────────────────────────────────────────────────────
    private String executeTool(String toolName, JsonNode args) {
        try {
            if ("get_client_profile".equals(toolName)) {
                String clientId = args.path("client_id").asText();
                Optional<ClientPortfolio> opt = portfolioRepo.findByClientId(clientId);
                if (opt.isEmpty()) return "{\"error\": \"Client not found\"}";
                ClientPortfolio p = opt.get();
                return String.format(
                    "{\"portfolio_id\":\"%d\",\"client_name\":\"%s\",\"risk_level\":\"%s\",\"current_equity\":%d,\"current_debt\":%d,\"current_commodity\":%d,\"target_equity\":%d,\"target_debt\":%d,\"target_commodity\":%d}",
                    p.getId(), p.getClientName(), p.getRiskLevel(),
                    p.getCurrentEquity(), p.getCurrentDebt(), p.getCurrentCommodity(),
                    p.getTargetEquity(), p.getTargetDebt(), p.getTargetCommodity()
                );
            } else if ("get_fund_holdings".equals(toolName)) {
                long portfolioId = args.path("portfolio_id").asLong();
                List<PortfolioHolding> holdings = holdingRepo.findByClientPortfolioId(portfolioId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < holdings.size(); i++) {
                    PortfolioHolding h = holdings.get(i);
                    
                    // Fetch Market Cap distribution for the fund
                    List<StockHolding> stocks = stockRepo.findByFundId(h.getFund().getId());
                    Map<String, Double> mcap = new HashMap<>();
                    for (StockHolding s : stocks) {
                        if (s.getMarketCap() != null) {
                            mcap.put(s.getMarketCap(), mcap.getOrDefault(s.getMarketCap(), 0.0) + s.getWeight());
                        }
                    }

                    sb.append(String.format("{\"fund\":\"%s\",\"category\":\"%s\",\"marketCap\":%s}",
                        h.getFund().getName(), h.getFund().getCategory(), mapper.writeValueAsString(mcap)));
                    if (i < holdings.size() - 1) sb.append(",");
                }
                sb.append("]");
                return sb.toString();
            }
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return "{\"error\": \"Unknown tool\"}";
    }

    // ─── Agentic Loop ────────────────────────────────────────────────────────────
    public AgentResult runAgentLoop(String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // The trace list that gets returned to the frontend for visualization
        List<Map<String, String>> trace = new ArrayList<>();
        addTrace(trace, "start", "🧠 AI Agent started", "Analyzing portfolio for client: " + clientId);

        String systemPrompt = """
            You are an expert AI financial advisor agent specializing in production-grade portfolio rebalancing using an optimization cost function.
            
            When analyzing a portfolio, you MUST consider:
            1. Overlap Penalty: Calculated as overlap(i,j) × min(weight_i, weight_j). High overlap between large allocations is a critical risk.
            2. Concentration Penalty: Any individual stock exposure > 8% is a "red flag".
            3. Strategy Deviation: Current vs Target allocation gaps in the 5-Finger Strategy (Equity, Debt, Commodities).
            
            Your goal is to find new weights that minimize "Total Cost" while enforcing:
            - Sum of weights = 100%
            - Max weight per fund <= 40%
            - Minimum weight threshold to avoid tiny allocations.
            
            When given data, explain your reasoning using these metrics. For example: "Reducing Fund A because its 42% overlap with Fund B creates excessive redundancy."
            
            Return ONLY a valid JSON array of recommendations. Each object must have:
            - "name": Stock/Fund/Asset name to trade.
            - "reason": Detailed reasoning (10-15 words) mentioning overlap, concentration, or diversification scores.
            - "action": "add" or "remove".
            - "change": Expected % change (e.g. "+7%").
            - "current": Estimated value (e.g. "₹25,000").
            - "color": Hex color (#10b981 for add, #ef4444 for remove).
            """;

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode sysMsg = mapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", "Analyze portfolio for client ID: " + clientId);
        messages.add(userMsg);

        addTrace(trace, "llm", "📡 Calling OpenAI GPT-3.5", "Sending client context and available tools to OpenAI...");

        for (int iteration = 0; iteration < 5; iteration++) {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0.6);
            requestBody.set("tools", buildTools());
            requestBody.put("tool_choice", iteration == 0 ? "required" : "auto");
            requestBody.set("messages", messages);

            try {
                String requestJson = mapper.writeValueAsString(requestBody);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

                JsonNode root = mapper.readTree(response.getBody());
                JsonNode choice = root.path("choices").get(0);
                JsonNode message = choice.path("message");
                String finishReason = choice.path("finish_reason").asText();

                if ("tool_calls".equals(finishReason)) {
                    ObjectNode assistantMsg = mapper.createObjectNode();
                    assistantMsg.put("role", "assistant");
                    if (!message.path("content").isNull()) {
                        assistantMsg.put("content", message.path("content").asText(""));
                    } else {
                        assistantMsg.putNull("content");
                    }
                    assistantMsg.set("tool_calls", message.path("tool_calls"));
                    messages.add(assistantMsg);

                    JsonNode toolCalls = message.path("tool_calls");
                    for (JsonNode toolCall : toolCalls) {
                        String toolCallId = toolCall.path("id").asText();
                        String toolName = toolCall.path("function").path("name").asText();
                        String argsStr = toolCall.path("function").path("arguments").asText();
                        JsonNode toolArgs = (argsStr != null && !argsStr.isEmpty())
                            ? mapper.readTree(argsStr)
                            : mapper.createObjectNode();

                        // ── Trace: OpenAI decided to call a tool ──────────────
                        addTrace(trace, "tool-call",
                            "🔧 OpenAI called tool: " + toolName,
                            "Arguments: " + toolCall.path("function").path("arguments").asText());

                        String toolResult = executeTool(toolName, toolArgs);

                        // ── Trace: DB result returned to OpenAI ───────────────
                        String label = "get_client_profile".equals(toolName) ? "🗄️ Database → client_portfolios" : "🗄️ Database → portfolio_holdings";
                        addTrace(trace, "db-result", label, toolResult.length() > 200 ? toolResult.substring(0, 200) + "..." : toolResult);
                        addTrace(trace, "llm", "📡 Result sent back to OpenAI", "Feeding tool result into GPT context...");

                        ObjectNode toolResultMsg = mapper.createObjectNode();
                        toolResultMsg.put("role", "tool");
                        toolResultMsg.put("tool_call_id", toolCallId);
                        toolResultMsg.put("name", toolName);
                        toolResultMsg.put("content", toolResult);
                        messages.add(toolResultMsg);
                    }

                } else if ("stop".equals(finishReason)) {
                    String content = message.path("content").asText("[]");
                    addTrace(trace, "reasoning", "🤔 OpenAI synthesizing recommendations", "Analyzing all tool data and applying 5-Finger Strategy rules...");

                    String jsonContent = content;
                    java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("\\[.*?\\]", java.util.regex.Pattern.DOTALL)
                        .matcher(content);
                    if (m.find()) jsonContent = m.group();

                    try {
                        JsonNode recNode = mapper.readTree(jsonContent);
                        List<Map<String, Object>> recommendations = new ArrayList<>();
                        if (recNode.isArray()) {
                            for (JsonNode node : recNode) {
                                Map<String, Object> rec = new HashMap<>();
                                rec.put("name", node.path("name").asText("Asset"));
                                rec.put("reason", node.path("reason").asText("Strategic rebalance"));
                                rec.put("action", node.path("action").asText("none"));
                                rec.put("change", node.path("change").asText("0%"));
                                rec.put("current", node.path("current").asText("₹0"));
                                rec.put("color", node.path("color").asText("#6b7280"));
                                recommendations.add(rec);
                            }
                        }
                        if (!recommendations.isEmpty()) {
                            addTrace(trace, "done", "✅ Agent complete", recommendations.size() + " trade recommendations generated successfully");
                            return new AgentResult(recommendations, trace);
                        }
                    } catch (Exception parseEx) {
                        addTrace(trace, "warning", "⚠️ JSON parse issue", "Retrying with explicit JSON prompt...");
                    }

                    ObjectNode retryMsg = mapper.createObjectNode();
                    retryMsg.put("role", "user");
                    retryMsg.put("content", "Return ONLY a JSON array of trade recommendations. No explanations. Just the JSON array.");
                    messages.add(retryMsg);
                    addTrace(trace, "llm", "📡 Retry: requesting strict JSON", "Asking OpenAI to return only the JSON array...");
                }

            } catch (Exception e) {
                addTrace(trace, "error", "❌ Error in iteration " + iteration, e.getMessage());
                break;
            }
        }

        addTrace(trace, "error", "❌ Agent loop exhausted", "Returning fallback response");
        return new AgentResult(
            List.of(Map.of("name", "Agent Error", "reason", "Loop failed", "action", "none", "change", "0%", "current", "₹0", "color", "#ef4444")),
            trace
        );
    }

    private void addTrace(List<Map<String, String>> trace, String type, String title, String detail) {
        Map<String, String> step = new LinkedHashMap<>();
        step.put("type", type);
        step.put("title", title);
        step.put("detail", detail);
        trace.add(step);
        System.out.println("[AI Agent] " + title + " | " + detail);
    }

    public Map<String, String> generateAudioInsights(String summary, String language) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        System.out.println("[OpenAiClient] Generating Tri-Language AI Insights (English + Tamil + Hindi)");

        String systemInstruction = 
            "You are a professional financial advisor. You must generate portfolio insights in THREE languages: English, Tamil, and Hindi. " +
            "For Tamil and Hindi, use a natural, spoken tone suitable for a 1-on-1 advisor briefing. Use the native scripts (Tamil and Devanagari). " +
            "DO NOT just transliterate English words. Translate technical concepts like 'overlap' as 'ஒன்றுடன் ஒன்று இணைதல்' (Tamil) or 'ओवरलैप' (Hindi - if commonly used) and explain them naturally. " +
            "Fund names can be kept as-is or transliterated, but the overall message MUST be in the target language script. " +
            "Return ONLY a valid JSON object with these exact keys: " +
            "'quick', 'quick_ta', 'quick_hin', 'detailed', 'detailed_ta', 'detailed_hin', 'advisor', 'advisor_ta', 'advisor_hin'.";

        String userPrompt = String.format(
            "Based on this analysis summary, generate 3 versions of insights (quick, detailed, advisor) in English, Tamil, and Hindi:\n\n" +
            "Analysis Summary: '%s'\n\n" +
            "1. quick: A 2-minute summary.\n" +
            "2. detailed: A 10-minute masterclass.\n" +
            "3. advisor: A 15-minute briefing.\n\n" +
            "Ensure the Tamil and Hindi versions are fully translated and natural, not just a few words.",
            summary
        );

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode sysMsg = mapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemInstruction);
        messages.add(sysMsg);

        ObjectNode msg = mapper.createObjectNode();
        msg.put("role", "user");
        msg.put("content", userPrompt);
        messages.add(msg);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2500); // Ensure enough space for all 9 insights
        requestBody.set("messages", messages);

        try {
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            // Extract JSON from response
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{.*\\}", java.util.regex.Pattern.DOTALL).matcher(content);
            if (m.find()) {
                JsonNode result = mapper.readTree(m.group());
                Map<String, String> insights = new HashMap<>();
                
                // Populate all keys
                String[] suffixes = {"", "_ta", "_hin"};
                String[] types = {"quick", "detailed", "advisor"};
                
                for (String type : types) {
                    for (String suffix : suffixes) {
                        String key = type + suffix;
                        if (result.has(key)) {
                            insights.put(key, result.path(key).asText());
                        }
                    }
                }
                
                return insights;
            }
        } catch (Exception e) {
            System.err.println("Error generating AI insights: " + e.getMessage());
        }

        Map<String, String> fallback = new HashMap<>();
        // Quick versions
        fallback.put("quick", "Analysis complete. " + summary);
        fallback.put("quick_ta", "பகுப்பாய்வு முடிந்தது. " + summary);
        fallback.put("quick_hin", "विश्लेषण पूरा हुआ। " + summary);
        
        // Detailed versions
        fallback.put("detailed", "We have performed a deep-dive analysis. " + summary);
        fallback.put("detailed_ta", "நாங்கள் ஆழமான பகுப்பாய்வைச் செய்துள்ளோம். " + summary);
        fallback.put("detailed_hin", "हमने एक विस्तृत विश्लेषण किया है। " + summary);
        
        // Advisor versions
        fallback.put("advisor", "Welcome to your senior advisor briefing. " + summary);
        fallback.put("advisor_ta", "உங்கள் மூத்த ஆலோசகர் விளக்கத்திற்கு வரவேற்கிறோம். " + summary);
        fallback.put("advisor_hin", "आपके वरिष्ठ सलाहकार ब्रीफिंग में आपका स्वागत है। " + summary);
        
        return fallback;
    }
}
