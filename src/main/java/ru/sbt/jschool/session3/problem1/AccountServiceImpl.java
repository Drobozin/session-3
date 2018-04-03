package ru.sbt.jschool.session3.problem1;

import java.util.*;

/**
 */
public class AccountServiceImpl implements AccountService {
    protected FraudMonitoring fraudMonitoring;
    private Map <Long, Account> list =   new HashMap<>();
    private Map <Long, Payment> operationId = new HashMap<>();
    private Map<Long, List<Account>> clientsList = new HashMap<>();



    public AccountServiceImpl(FraudMonitoring fraudMonitoring) {
        this.fraudMonitoring = fraudMonitoring;
    }

    @Override public Result create(long clientID, long accountID, float initialBalance, Currency currency) {
        if (fraudMonitoring.check(clientID)){
            return Result.FRAUD;
        }
        if (find(accountID) != null){
            return Result.ALREADY_EXISTS;
        }
        else {
            Account acc = new Account(clientID, accountID, currency, initialBalance);
            list.put(accountID,acc);
            List<Account> clientsAcc = clientsList.get(clientID) ;
            if (clientsAcc == null){
                clientsAcc = new ArrayList<>();
            }
            else{
                for(Account i : clientsAcc){
                    if(i.getAccountID() == accountID){
                        return Result.ALREADY_EXISTS;
                    }
                }
            }
            clientsAcc.add(acc);
            clientsList.put(clientID, clientsAcc);
            return Result.OK;
        }
    }
    @Override public List<Account> findForClient(long clientID) {
        return clientsList.get(clientID);

    }

    @Override public Account find(long accountID) {
        return list.containsKey(accountID)?list.get(accountID):null;
    }

    @Override public Result doPayment(Payment payment) {
        Account payer = find(payment.getPayerAccountID());
        Account recipient = find(payment.getRecipientAccountID());
        if (operationId.containsKey(payment.getOperationID())){
            return Result.ALREADY_EXISTS;
        }
        if(payer == null){
            return Result.PAYER_NOT_FOUND;
        }
        if (recipient == null || recipient.getClientID() != payment.getRecipientID()){
            return Result.RECIPIENT_NOT_FOUND;
        }

        operationId.put(payment.getOperationID(), payment);
        if (payer.getBalance() < payment.getAmount()){
            return Result.INSUFFICIENT_FUNDS;
        }
        if (payer.getCurrency() != recipient.getCurrency()){
            float x = payer.getCurrency().to(payment.getAmount(), recipient.getCurrency());
            float result = x + recipient.getBalance();
            recipient.setBalance(result);
            payer.setBalance(payer.getBalance()-payment.getAmount());
            return Result.OK;
        }
        recipient.setBalance(recipient.getBalance() + payment.getAmount());
        payer.setBalance(payer.getBalance() - payment.getAmount());
        return Result.OK;
    }
}
