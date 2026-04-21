package com.example.demo.dao.impl;

import com.example.demo.dao.PortfolioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PortfolioDaoImpl implements PortfolioDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getFundStockHoldings(String pan) {
        String sql = "SELECT " +
                "    EMS.SchemeName AS fundName, " +
                "    HOLD.COMPNAME AS stockName, " +
                "    SUM(HOLD.HOLDPERCENTAGE) AS weight " +
                "FROM ECAS_MFC_Summary EMS " +
                "JOIN AFT_MFC_MAPPING AMM ON EMS.AMC = AMM.MFC_Amc_code AND EMS.SchemeCode = AMM.MFC_Scheme_code " +
                "JOIN AFT_SCHEME_RT_CODE ASRC ON AMM.RTA_Scheme_code = ASRC.RT_SCHEME_CODE " +
                "JOIN Aft_Amc_mst_new AAMN ON AMM.RTA_Amc_code = AAMN.rtamccode_1 " +
                "JOIN AFT_SCHEME_DETAILS ASD ON ASRC.AFT_SCHEME_CODE = ASD.SCHEMECODE AND AAMN.amc_code = ASD.AMC_CODE "
                +
                "JOIN AFT_MF_PORTFOLIO HOLD ON ASD.PRIMARY_FD_CODE = HOLD.SCHEMECODE " +
                "WHERE EMS.MarketValue > 0 " +
                "AND EMS.ECAS_ReferenceId IN (SELECT ECAS_ReferenceId FROM ECAS_MFC_Investordetails WHERE PAN = ?) " +
                "AND HOLD.INVENDDATE = (SELECT MAX(H2.INVENDDATE) FROM AFT_MF_PORTFOLIO H2 WHERE H2.SCHEMECODE = HOLD.SCHEMECODE) "
                +
                "GROUP BY EMS.SchemeName, HOLD.COMPNAME";

        return jdbcTemplate.queryForList(sql, pan);
    }

    @Override
    public List<Map<String, Object>> getClientFunds(String pan) {
        String sql = "SELECT " +
                "    SchemeName AS fundName, " +
                "    SUM(MarketValue) AS marketValue, " +
                "    (SUM(MarketValue) * 100.0 / SUM(SUM(MarketValue)) OVER()) AS fundWeight " +
                "FROM ECAS_MFC_Summary " +
                "WHERE MarketValue > 0 " +
                "AND ECAS_ReferenceId IN (" +
                "    SELECT ECAS_ReferenceId " +
                "    FROM ECAS_MFC_Investordetails " +
                "    WHERE PAN = ?" +
                ") " +
                "GROUP BY SchemeName";
        return jdbcTemplate.queryForList(sql, pan);
    }

    @Override
    public Map<String, Object> getPortfolioPerformance(String pan) {
        String sql = "select SUM(ems.MarketValue) as currentValue, SUM(ems.CostValue) as investedAmount " +
                "from ECAS_MFC_InvestorDetails emid " +
                "JOIN ECAS_MFC_Summary ems on emid.ECAS_ReferenceId = ems.ECAS_ReferenceId " +
                "where PAN = ?";
        try {
            return jdbcTemplate.queryForMap(sql, pan);
        } catch (Exception e) {
            return Map.of("currentValue", 0.0, "investedAmount", 0.0);
        }
    }

    @Override
    public List<Map<String, Object>> getAssetTypeAllocations(String pan) {
        String sql = "SELECT " +
                "    SCM.ASSET_TYPE as assetType, " +
                "    SUM(EMS.MarketValue) AS assetTypeValue, " +
                "    ROUND( " +
                "        SUM(EMS.MarketValue) * 100.0 " +
                "        / SUM(SUM(EMS.MarketValue)) OVER (), " +
                "    2) AS allocationPercent " +
                "FROM ECAS_MFC_InvestorDetails EMID " +
                "JOIN ECAS_MFC_Summary EMS " +
                "    ON EMID.ECAS_ReferenceId = EMS.ECAS_ReferenceId " +
                "    AND EMS.MarketValue > 0 " +
                "JOIN AFT_MFC_MAPPING AMM " +
                "    ON EMS.AMC = AMM.MFC_Amc_code " +
                "    AND EMS.SchemeCode = AMM.MFC_Scheme_code " +
                "JOIN AFT_SCHEME_RT_CODE ASRC " +
                "    ON AMM.RTA_Scheme_code = ASRC.RT_SCHEME_CODE " +
                "JOIN Aft_Amc_mst_new AAMN " +
                "    ON AMM.RTA_Amc_code = AAMN.rtamccode_1 " +
                "JOIN AFT_SCHEME_DETAILS ASD " +
                "    ON ASRC.AFT_SCHEME_CODE = ASD.SCHEMECODE " +
                "    AND AAMN.amc_code = ASD.AMC_CODE " +
                "    AND ASD.STATUS = 'Active' " +
                "JOIN AFT_SCLASS_MST SCM " +
                "    ON SCM.CLASSCODE = ASD.CLASSCODE " +
                "WHERE EMID.PAN = ? " +
                "GROUP BY SCM.ASSET_TYPE " +
                "ORDER BY SCM.ASSET_TYPE";
        return jdbcTemplate.queryForList(sql, pan);
    }
    @Override
    public String getInvestorName(String pan) {
        String sql = "SELECT InvestorName FROM InvestorBasicDetail WHERE TRIM(PAN) = TRIM(?)";
        try {
            String name = jdbcTemplate.queryForObject(sql, String.class, pan);
            if (name != null && !name.trim().isEmpty()) return name.trim();
        } catch (Exception e) {
            System.err.println("[Database Error] Failed to fetch InvestorName for PAN " + pan + ": " + e.getMessage());
        }
        return "Valued Investor"; // Fallback
    }
}
