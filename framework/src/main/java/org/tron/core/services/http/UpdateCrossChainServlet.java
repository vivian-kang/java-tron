package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Wallet;
import org.tron.protos.Protocol;
import org.tron.protos.contract.BalanceContract;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Component
public class UpdateCrossChainServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
              .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      boolean visible = Util.getVisiblePost(contract);
      BalanceContract.CrossChainInfo.Builder build = BalanceContract.CrossChainInfo.newBuilder();
      JsonFormat.merge(contract, build, visible);
      Protocol.Transaction tx = wallet.createTransactionCapsule(build.build(),
              Protocol.Transaction.Contract.ContractType.UpdateCrossChainContract).getInstance();
      JSONObject jsonObject = JSONObject.parseObject(contract);
      tx = Util.setTransactionPermissionId(jsonObject, tx);
      response.getWriter().println(Util.printCreateTransaction(tx, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}