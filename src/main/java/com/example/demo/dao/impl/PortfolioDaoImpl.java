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
                     "    EMS.SchemeName as fundName, " +
                     "    HOLD.COMPNAME as stockName, " +
                     "    HOLD.HOLDPERCENTAGE as weight " +
                     "FROM ECAS_MFC_Summary EMS " +
                     "JOIN AFT_MFC_MAPPING AMM ON EMS.AMC = AMM.MFC_Amc_code AND EMS.SchemeCode = AMM.MFC_Scheme_code " +
                     "JOIN AFT_SCHEME_RT_CODE ASRC ON AMM.RTA_Scheme_code = ASRC.RT_SCHEME_CODE " +
                     "JOIN Aft_Amc_mst_new AAMN ON AMM.RTA_Amc_code = AAMN.rtamccode_1 " +
                     "JOIN AFT_SCHEME_DETAILS ASD ON ASRC.AFT_SCHEME_CODE = ASD.SCHEMECODE AND AAMN.amc_code = ASD.AMC_CODE " +
                     "JOIN AFT_MF_PORTFOLIO HOLD ON ASD.PRIMARY_FD_CODE = HOLD.SCHEMECODE " +
                     "WHERE EMS.MarketValue > 0 " +
                     "AND EMS.ECAS_ReferenceId IN (SELECT ECAS_ReferenceId FROM ECAS_MFC_Investordetails WHERE PAN = ?) " +
                     "AND HOLD.INVENDDATE = (SELECT MAX(H2.INVENDDATE) FROM AFT_MF_PORTFOLIO H2 WHERE H2.SCHEMECODE = HOLD.SCHEMECODE)";



        
        return jdbcTemplate.queryForList(sql, pan);
    }

    @Override
    public List<Map<String, Object>> getClientFunds(String pan) {
        String sql = "SELECT " +
                     "    SchemeName as fundName, " +
                     "    MarketValue as marketValue, " +
                     "    (MarketValue / SUM(MarketValue) OVER()) * 100 as fundWeight " +
                     "FROM ECAS_MFC_Summary " +
                     "WHERE MarketValue > 0 " +
                     "AND ECAS_ReferenceId IN (SELECT ECAS_ReferenceId FROM ECAS_MFC_Investordetails WHERE PAN = ?)";
        return jdbcTemplate.queryForList(sql, pan);
    }
}
