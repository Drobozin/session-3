package ru.sbt.jschool.session3.problem1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class AccountServiceImpl implements AccountService {
    protected FraudMonitoring fraudMonitoring;
    ArrayList <Account> list = new ArrayList<>();
    ArrayList<Long> operationId = new ArrayList<>();


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
            list.add(new Account(clientID, accountID, currency, initialBalance));
            return Result.OK;
        }
    }

    @Override public List<Account> findForClient(long clientID) {
        ArrayList<Account> _list = new ArrayList<>();
        for (Account i: list) {
            if(clientID == i.getClientID()){
                _list.add(i);
            }
        }
        return _list != null ? _list : Collections.EMPTY_LIST;
    }

    @Override public Account find(long accountID) {
        for (Account i: list) {
            if(accountID == i.getAccountID()){
                return list.get(list.indexOf(i));
            }
        }
        return null;
    }

    @Override public Result doPayment(Payment payment) {
        Account payer = find(payment.getPayerAccountID());
        Account recipient = find(payment.getRecipientAccountID());

        if(payer == null){
            return Result.PAYER_NOT_FOUND;
        }
        if(!findForClient(payment.getRecipientAccountID()).contains(payment.getPayerAccountID())){
            return Result.RECIPIENT_NOT_FOUND;
        }
        if (payer.getBalance() < payment.getAmount()){
            return Result.INSUFFICIENT_FUNDS;
        }
        operationId.add(payment.getOperationID());
        for (long i: operationId){
            if (i == payment.getOperationID()){
                return Result.ALREADY_EXISTS;
            }
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
