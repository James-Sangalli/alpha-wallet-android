package com.alphawallet.app.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.ConfirmationType;
import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.Transaction;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.interact.FetchTransactionsInteract;
import com.alphawallet.app.interact.FindDefaultNetworkInteract;
import com.alphawallet.app.interact.GenericWalletInteract;
import com.alphawallet.app.repository.TokenRepositoryType;
import com.alphawallet.app.router.ExternalBrowserRouter;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.ConfirmationActivity;
import com.alphawallet.app.ui.TransactionDetailActivity;
import com.alphawallet.app.util.Utils;

import org.web3j.crypto.RawTransaction;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class TransactionDetailViewModel extends BaseViewModel {
    private final ExternalBrowserRouter externalBrowserRouter;
    private final TokensService tokenService;
    private final FindDefaultNetworkInteract networkInteract;
    private final TokenRepositoryType tokenRepository;
    private final FetchTransactionsInteract fetchTransactionsInteract;

    private final MutableLiveData<BigInteger> lastestBlock = new MutableLiveData<>();
    private final MutableLiveData<Transaction> lastestTx = new MutableLiveData<>();
    public LiveData<BigInteger> latestBlock() {
        return lastestBlock;
    }
    public LiveData<Transaction> lastestTx() { return lastestTx; }
    private String walletAddress;

    @Nullable
    private Disposable pendingUpdateDisposable;

    @Nullable
    private Disposable currentBlockUpdateDisposable;

    TransactionDetailViewModel(
            FindDefaultNetworkInteract findDefaultNetworkInteract,
            ExternalBrowserRouter externalBrowserRouter,
            TokenRepositoryType tokenRepository,
            TokensService service,
            FetchTransactionsInteract fetchTransactionsInteract) {
        this.networkInteract = findDefaultNetworkInteract;
        this.externalBrowserRouter = externalBrowserRouter;
        this.tokenService = service;
        this.tokenRepository = tokenRepository;
        this.fetchTransactionsInteract = fetchTransactionsInteract;
    }

    public void prepare(final int chainId, final String walletAddr)
    {
        walletAddress = walletAddr;
        currentBlockUpdateDisposable = Observable.interval(0, 10, TimeUnit.SECONDS)
                .doOnNext(l -> {
                    disposable = tokenRepository.fetchLatestBlockNumber(chainId)
                            .subscribeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(lastestBlock::postValue, t -> { this.lastestBlock.postValue(BigInteger.ZERO); });
                }).subscribe();
    }

    public void showMoreDetails(Context context, Transaction transaction) {
        Uri uri = buildEtherscanUri(transaction);
        if (uri != null) {
            externalBrowserRouter.open(context, uri);
        }
    }

    public void startPendingTimeDisplay(final String txHash)
    {
        pendingUpdateDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .doOnNext(l -> displayCurrentPendingTime(txHash)).subscribe();
    }

    //TODO: move to display new transaction
    private void displayCurrentPendingTime(final String txHash)
    {
        //check if TX has been written
        Transaction tx = fetchTransactionsInteract.fetchCached(walletAddress, txHash);
        if (tx != null)
        {
            lastestTx.postValue(tx);
            if (!tx.blockNumber.equals("0"))
            {
                if (pendingUpdateDisposable != null && !pendingUpdateDisposable.isDisposed())
                    pendingUpdateDisposable.dispose();
            }
        }
    }

    public void shareTransactionDetail(Context context, Transaction transaction) {
        Uri shareUri = buildEtherscanUri(transaction);
        if (shareUri != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_transaction_detail));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareUri.toString());
            context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }

    public Token getToken(int chainId, String address)
    {
        return tokenService.getToken(chainId, address);
    }

    @Nullable
    private Uri buildEtherscanUri(Transaction transaction) {
        NetworkInfo networkInfo = networkInteract.getNetworkInfo(transaction.chainId);
        if (networkInfo != null && !TextUtils.isEmpty(networkInfo.etherscanUrl)) {
            return Uri.parse(networkInfo.etherscanUrl)
                    .buildUpon()
                    .appendEncodedPath(transaction.hash)
                    .build();
        }
        return null;
    }

    public boolean hasEtherscanDetail(Transaction tx)
    {
        NetworkInfo networkInfo = networkInteract.getNetworkInfo(tx.chainId);
        return networkInfo.etherscanUrl != null && networkInfo.etherscanUrl.length() != 0;
    }

    public String getNetworkName(int chainId)
    {
        return networkInteract.getNetworkName(chainId);
    }

    public String getNetworkSymbol(int chainId)
    {
        return networkInteract.getNetworkInfo(chainId).symbol;
    }

    public void onDispose()
    {
        if (pendingUpdateDisposable != null && !pendingUpdateDisposable.isDisposed()) pendingUpdateDisposable.dispose();
        if (currentBlockUpdateDisposable != null && !currentBlockUpdateDisposable.isDisposed()) currentBlockUpdateDisposable.dispose();
    }

    public void reSendTransaction(Transaction tx, Context ctx, Token token, ConfirmationType type)
    {
        Intent intent = new Intent(ctx, ConfirmationActivity.class);
        intent.putExtra(C.EXTRA_TXHASH, tx.hash);
        intent.putExtra(C.EXTRA_TRANSACTION_DATA, tx.input);
        intent.putExtra(C.EXTRA_TO_ADDRESS, tx.to);
        intent.putExtra(C.EXTRA_AMOUNT, tx.value);
        intent.putExtra(C.EXTRA_NONCE, tx.nonce);
        intent.putExtra(C.EXTRA_TOKEN_ID, token);
        intent.putExtra(C.EXTRA_CONTRACT_ADDRESS, tx.to);
        intent.putExtra(C.EXTRA_GAS_PRICE, tx.gasPrice);
        intent.putExtra(C.EXTRA_GAS_LIMIT, tx.gasUsed);
        String symbol = token != null ? token.tokenInfo.symbol : "";
        intent.putExtra(C.EXTRA_SYMBOL, symbol);
        //TODO: reverse resolve 'tx.to' ENS
        //intent.putExtra(C.EXTRA_ENS_DETAILS, ensDetails);
        intent.putExtra(C.EXTRA_NETWORKID, tx.chainId);
        intent.putExtra(C.TOKEN_TYPE, type.ordinal());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
