package com.alphawallet.app.service;

import android.net.Uri;

import com.alphawallet.app.entity.EtherscanEvent;
import com.alphawallet.app.entity.OkxEvent;
import com.alphawallet.app.entity.okx.TokenListReponse;
import com.alphawallet.app.entity.okx.TransactionListResponse;
import com.alphawallet.app.entity.tokens.TokenInfo;
import com.alphawallet.app.repository.KeyProviderFactory;
import com.alphawallet.app.util.JsonUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkLinkService
{
    private static final String BASE_URL = "https://www.oklink.com/api";
    private static final String LIMIT = "50"; // Max limit; default is 20
    private static final String CHAIN_SHORT_NAME = "OKC"; // Max limit; default is 20
    public static OkLinkService instance;
    private final OkHttpClient httpClient;

    public OkLinkService(OkHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public static OkLinkService get(OkHttpClient httpClient)
    {
        if (instance == null)
        {
            instance = new OkLinkService(httpClient);
        }
        return instance;
    }

    private Request buildRequest(String api)
    {
        Request.Builder requestB = new Request.Builder()
            .url(api)
            .header("User-Agent", "Chrome/74.0.3729.169")
            .addHeader("Content-Type", "application/json")
            .addHeader("Ok-Access-Key", KeyProviderFactory.get().getOkLinkKey())
            .get();
        return requestB.build();
    }

    private String executeRequest(String api)
    {
        try (okhttp3.Response response = httpClient.newCall(buildRequest(api)).execute())
        {
            if (response.isSuccessful())
            {
                ResponseBody responseBody = response.body();
                if (responseBody != null)
                {
                    return responseBody.string();
                }
            }
            else
            {
                return Objects.requireNonNull(response.body()).string();
            }
        }
        catch (Exception e)
        {
            Timber.e(e);
            return e.getMessage();
        }

        return JsonUtils.EMPTY_RESULT;
    }

    public EtherscanEvent[] getEtherscanEvents(String address, boolean isNft)
    {
        List<OkxEvent> events = new ArrayList<>();
        int page = 1;
        int totalPage = 0;

        do
        {
            TransactionListResponse response2 = new Gson().fromJson(
                fetchTransactions(address, isNft ? "token_721" : "token_20", String.valueOf(page++)),
                TransactionListResponse.class);

            if (response2.data.size() > 0)
            {
                totalPage = Integer.parseInt(response2.data.get(0).totalPage);
                events.addAll(response2.data.get(0).transactionLists);
            }
        } while (page <= totalPage);

        List<EtherscanEvent> etherscanEvents = new ArrayList<>();
        for (OkxEvent ev : events)
        {
            try
            {
                etherscanEvents.add(ev.getEtherscanTransferEvent(isNft));
            }
            catch (Exception e)
            {
                Timber.e(e);
            }
        }

        return etherscanEvents.toArray(new EtherscanEvent[0]);
    }

    public String fetchTransactions(String address,
                                    String protocolType,
                                    String page)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(BASE_URL + "/v5/explorer/address/transaction-list")
            .appendQueryParameter("address", address)
            .appendQueryParameter("protocolType", protocolType)
            .appendQueryParameter("chainShortName", CHAIN_SHORT_NAME)
            .appendQueryParameter("limit", LIMIT)
            .appendQueryParameter("page", page);
        String url = builder.build().toString();
        Timber.d("URL: " + protocolType + " : " + url);
        return executeRequest(url);
    }

    //JB: Add a method to return TokenInfo data for both NFT and ERC20.
    public TokenInfo getTokenInfo(String contractAddress)
    {
        TokenListReponse.TokenDetails tokenDetails = getTokenDetails(contractAddress);

        TokenInfo tokenInfo = new TokenInfo(
            tokenDetails.tokenContractAddress,
            tokenDetails.tokenFullName,
            tokenDetails.token,
            Integer.parseInt(tokenDetails.precision),
            true,
            66);

        // JDG TODO: Remove logs
        Timber.d("tokenInfo address: " + tokenInfo.address);
        Timber.d("tokenInfo tokenFullName: " + tokenInfo.name);
        Timber.d("tokenInfo token: " + tokenInfo.symbol);
        Timber.d("tokenInfo decimals: " + tokenInfo.decimals);

        return tokenInfo;
    }

    public TokenListReponse.TokenDetails getTokenDetails(String contractAddress)
    {
        TokenListReponse response = new Gson().fromJson(fetchTokenDetails(contractAddress), TokenListReponse.class);
        if (response.data.size() > 0)
        {
            List<TokenListReponse.TokenDetails> tokenList = response.data.get(0).tokenList;

            if (tokenList.size() > 0)
            {
                return tokenList.get(0);
            }
        }
        return null;
    }

    public String fetchTokenDetails(String contractAddress)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(BASE_URL + "/v5/explorer/token/token-list")
            .appendQueryParameter("tokenContractAddress", contractAddress)
            .appendQueryParameter("chainShortName", CHAIN_SHORT_NAME);
        String url = builder.build().toString();
        return executeRequest(url);
    }
}
