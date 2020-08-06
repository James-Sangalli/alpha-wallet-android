package com.alphawallet.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.DApp;
import com.alphawallet.app.entity.DAppFunction;
import com.alphawallet.app.entity.SignAuthenticationCallback;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokenscript.TokenScriptRenderCallback;
import com.alphawallet.app.entity.tokenscript.WebCompletionCallback;
import com.alphawallet.app.util.DappBrowserUtils;
import com.alphawallet.app.util.KeyboardUtils;
import com.alphawallet.app.viewmodel.TokenFunctionViewModel;
import com.alphawallet.app.viewmodel.TokenFunctionViewModelFactory;
import com.alphawallet.app.web3.OnSetValuesListener;
import com.alphawallet.app.web3.OnSignPersonalMessageListener;
import com.alphawallet.app.web3.Web3TokenView;
import com.alphawallet.app.web3.entity.Address;
import com.alphawallet.app.web3.entity.FunctionCallback;
import com.alphawallet.app.web3.entity.PageReadyCallback;
import com.alphawallet.app.widget.AWalletAlertDialog;
import com.alphawallet.app.widget.FunctionButtonBar;
import com.alphawallet.app.widget.SignMessageDialog;
import com.alphawallet.app.widget.SignTransactionDialog;
import com.alphawallet.app.widget.SystemView;
import com.alphawallet.token.entity.*;
import com.alphawallet.token.tools.Numeric;

import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.alphawallet.app.C.Key.TICKET;
import static com.alphawallet.app.entity.CryptoFunctions.sigFromByteArray;
import static com.alphawallet.app.entity.Operation.SIGN_DATA;
import static com.alphawallet.app.entity.tokenscript.TokenscriptFunction.TOKENSCRIPT_CONVERSION_ERROR;

/**
 * Created by James on 4/04/2019.
 * Stormbird in Singapore
 */
public class FunctionActivity extends BaseActivity implements FunctionCallback,
                                                              PageReadyCallback, OnSignPersonalMessageListener, SignAuthenticationCallback,
                                                              StandardFunctionInterface, TokenScriptRenderCallback, WebCompletionCallback,
                                                              OnSetValuesListener
{
    @Inject
    protected TokenFunctionViewModelFactory viewModelFactory;
    private TokenFunctionViewModel viewModel;

    private Token token;
    private List<BigInteger> tokenIds;
    private BigInteger tokenId;
    private String actionMethod;
    private SystemView systemView;
    private Web3TokenView tokenView;
    private SignMessageDialog dialog;
    private final Map<String, String> args = new HashMap<>();
    private StringBuilder attrs;
    private AWalletAlertDialog alertDialog;
    private EthereumMessage messageToSign;
    private FunctionButtonBar functionBar;
    private final Handler handler = new Handler();
    private int parsePass = 0;
    private int resolveInputCheckCount;
    private TSAction action;

    private void initViews() {
        token = getIntent().getParcelableExtra(TICKET);
        actionMethod = getIntent().getStringExtra(C.EXTRA_STATE);
        String tokenIdStr = getIntent().getStringExtra(C.EXTRA_TOKEN_ID);
        if (tokenIdStr == null || tokenIdStr.length() == 0) tokenIdStr = "0";
        tokenIds = token.stringHexToBigIntegerList(tokenIdStr);
        tokenId = tokenIds.get(0);

        if (token == null)
        {
            showInitError();
            return;
        }

        tokenView = findViewById(R.id.web3_tokenview);

        tokenView.setChainId(token.tokenInfo.chainId);
        tokenView.setWalletAddress(new Address(token.getWallet()));
        tokenView.setupWindowCallback(this);
        tokenView.setRpcUrl(token.tokenInfo.chainId);
        tokenView.setOnReadyCallback(this);
        tokenView.setOnSignPersonalMessageListener(this);
        tokenView.setOnSetValuesListener(this);
        tokenView.setKeyboardListenerCallback(this);
        viewModel.startGasPriceUpdate(token.tokenInfo.chainId);
        viewModel.getCurrentWallet();
        parsePass = 0;
    }

    private void displayFunction(String tokenAttrs)
    {
        try
        {
            Map<String, TSAction> functions = viewModel.getAssetDefinitionService().getTokenFunctionMap(token.tokenInfo.chainId, token.getAddress());
            TSAction action = functions.get(actionMethod);
            String magicValues = viewModel.getAssetDefinitionService().getMagicValuesForInjection(token.tokenInfo.chainId);

            String injectedView = tokenView.injectWeb3TokenInit(action.view.tokenView, tokenAttrs, tokenId);
            injectedView = tokenView.injectJSAtEnd(injectedView, magicValues);
            injectedView = tokenView.injectStyleAndWrapper(injectedView, action.style + "\n" + action.view.style);

            String base64 = Base64.encodeToString(injectedView.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            tokenView.loadData(base64, "text/html; charset=utf-8", "base64");
        }
        catch (Exception e)
        {
            fillEmpty();
        }
    }

    private void getAttrs()
    {
        try
        {
            attrs = viewModel.getAssetDefinitionService().getTokenAttrs(token, tokenId, 1);
            //add extra tokenIds if required
            addMultipleTokenIds(attrs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Fetch attributes local to this action and add them to the injected token properties
        Map<String, TSAction> functions = viewModel.getAssetDefinitionService().getTokenFunctionMap(token.tokenInfo.chainId, token.getAddress());
        action = functions.get(actionMethod);
        List<Attribute> localAttrs = (action != null && action.attributes != null) ? new ArrayList<>(action.attributes.values()) : null;

        viewModel.getAssetDefinitionService().resolveAttrs(token, tokenIds, localAttrs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onAttr, this::onError, () -> displayFunction(attrs.toString()))
                    .isDisposed();
    }

    private void addMultipleTokenIds(StringBuilder sb)
    {
        Map<String, TSAction> functions = viewModel.getAssetDefinitionService().getTokenFunctionMap(token.tokenInfo.chainId, token.getAddress());
        TSAction action = functions.get(actionMethod);
        boolean hasTokenIds = false;

        if (action != null && action.function != null)
        {
            for (MethodArg arg : action.function.parameters)
            {
                int index = arg.getTokenIndex();
                if (arg.isTokenId() && index >= 0 && index < tokenIds.size())
                {
                    if (!hasTokenIds)
                    {
                        sb.append("tokenIds: [");
                    }
                    else
                    {
                        sb.append(", ");
                    }
                    sb.append("\"");
                    sb.append(tokenIds.get(index));
                    sb.append("\"");
                    hasTokenIds = true;
                }
            }

            if (hasTokenIds)
            {
                sb.append("],\n");
            }
        }
    }

    private void onError(Throwable throwable)
    {
        throwable.printStackTrace();
        displayFunction(attrs.toString());
    }

    private void onAttr(TokenScriptResult.Attribute attribute)
    {
        //is the attr incomplete?
        if (BuildConfig.DEBUG) System.out.println("ATTR/FA: " + attribute.id + " (" + attribute.name + ")" + " : " + attribute.text);
        TokenScriptResult.addPair(attrs, attribute.id, attribute.text);
    }

    private void fillEmpty()
    {
        findViewById(R.id.layout_webwrapper).setVisibility(View.VISIBLE);
        tokenView.loadData("<html><body>No Data</body></html>", "text/html", "utf-8");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_view);
        setupViews();
    }

    private void setupViews()
    {
        initViewModel();
        initViews();
        toolbar();
        setTitle(actionMethod);
        setupFunctions();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (viewModel == null)
        {
            initViews();
        }

        if (parsePass == 0)
        {
            parsePass = 1;
            viewModel.getAssetDefinitionService().clearResultMap();
            args.clear();
            getAttrs();
        }
    }

    private void initViewModel()
    {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(TokenFunctionViewModel.class);
        systemView = findViewById(R.id.system_view);
        systemView.hide();
        viewModel.invalidAddress().observe(this, this::errorInvalidAddress);
        viewModel.insufficientFunds().observe(this, this::errorInsufficientFunds);
    }

    private void setupFunctions()
    {
        functionBar = findViewById(R.id.layoutButtons);
        functionBar.setupFunctionList(this, actionMethod);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void completeTokenScriptFunction(String function)
    {
        Map<String, TSAction> functions = viewModel.getAssetDefinitionService().getTokenFunctionMap(token.tokenInfo.chainId, token.getAddress());
        action = functions.get(function);

        if (action != null && action.function != null) //if no function then it's handled by the token view
        {
            resolveTokenIds();
            resolveUserInput();
        }
    }

    private void resolveUserInput()
    {
        resolveInputCheckCount = 0;
        CalcJsValueCallback cb = new CalcJsValueCallback()
        {
            @Override
            public void calculationCompleted(String value, String result, TokenscriptElement e, Attribute attr)
            {
                if (BuildConfig.DEBUG) System.out.println("ATTR/FA: Resolve " + value + " : " + result);
                //need to find attr
                e.value = viewModel.getAssetDefinitionService().convertInputValue(attr, result);

                if (e.value.startsWith(TOKENSCRIPT_CONVERSION_ERROR)) //handle parse error
                {
                    String message = e.value.substring(TOKENSCRIPT_CONVERSION_ERROR.length());
                    if (!TextUtils.isEmpty(attr.name)) tokenScriptError(getString(R.string.complete_tokenscript_value, attr.name), attr.name);
                    else tokenScriptError(message, null);
                }
                else
                {
                    resolveInputCheckCount--;
                    completeAction();
                }
            }

            @Override
            public void unresolvedSymbolError(String value)
            {
                if (BuildConfig.DEBUG) System.out.println("ATTR/FA: Resolve: ERROR: " + value);
                tokenScriptError(value, null);
            }
        };

        //fetch any user-input params needed for native transaction
        if (action.function.tx != null)
        {
            for (TokenscriptElement e : action.function.tx.args.values())
            {
                checkTokenScriptElement(cb, action, e);
            }
        }

        //fetch user-input params for transaction
        for (MethodArg arg : action.function.parameters)
        {
            checkTokenScriptElement(cb, action, arg.element);
        }

        //check if action can be completed
        completeAction();
    }

    private void checkTokenScriptElement(CalcJsValueCallback cb, TSAction action, TokenscriptElement e)
    {
        if (e.ref != null && e.ref.length() > 0 && action.attributes != null)
        {
            Attribute attr = action.attributes.get(e.ref);
            if (attr != null && attr.userInput)
            {
                resolveInputCheckCount++;
                evaluateJavaScript(cb, e.ref, e, attr);
            }
        }
    }

    private void completeAction()
    {
        if (resolveInputCheckCount == 0)
        {
            if (!viewModel.handleFunction(action, tokenId, token, this))
            {
                showTransactionError();
            }
            viewModel.getAssetDefinitionService().clearResultMap();
        }
    }

    private void resolveTokenIds()
    {
        for (MethodArg arg : action.function.parameters)
        {
            int index = arg.getTokenIndex();
            if (arg.isTokenId() && index >= 0 && index < tokenIds.size())
            {
                arg.element.value = tokenIds.get(index).toString();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        viewModel.stopGasSettingsFetch();
        viewModel.getAssetDefinitionService().clearResultMap();
    }

    private void errorInvalidAddress(String address)
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        alertDialog.setTitle(R.string.error_invalid_address);
        alertDialog.setMessage(getString(R.string.invalid_address_explain, address));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    private void errorInsufficientFunds(Token currency)
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        alertDialog.setTitle(R.string.error_insufficient_funds);
        alertDialog.setMessage(getString(R.string.current_funds, currency.getCorrectedBalance(currency.tokenInfo.decimals), currency.getSymbol()));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    private void tokenScriptError(String elementName, String title)
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        if (title != null) alertDialog.setTitle(title);
        else alertDialog.setTitle(R.string.tokenscript_error);
        alertDialog.setMessage(getString(R.string.tokenscript_error_detail, elementName));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    private void showInitError()
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        alertDialog.setTitle(R.string.error_fail_load_tokens);
        alertDialog.setMessage(getString(R.string.ticket_not_valid));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    private void showTransactionError()
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        alertDialog.setTitle(R.string.tokenscript_error);
        alertDialog.setMessage(getString(R.string.invalid_parameters));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void signMessage(byte[] sign, DAppFunction dAppFunction, EthereumMessage message)
    {
        showProgressSpinner(true);
        viewModel.signMessage(sign, dAppFunction, message, token.tokenInfo.chainId);
    }

    @Override
    public void functionSuccess()
    {
        LinearLayout successOverlay = findViewById(R.id.layout_success_overlay);
        if (successOverlay != null) successOverlay.setVisibility(View.VISIBLE);
        handler.postDelayed(closer, 1000);
    }

    private Runnable closer = new Runnable()
    {
        @Override
        public void run()
        {
            finish();
        }
    };

    private Runnable progress = new Runnable()
    {
        @Override
        public void run()
        {
            onProgress(true);
        }
    };

    private Runnable progressOff = new Runnable()
    {
        @Override
        public void run()
        {
            onProgress(false);
        }
    };

    @Override
    public void functionFailed()
    {
        if (BuildConfig.DEBUG) System.out.println("ATTR/FA: FAIL: " + actionMethod);
    }

    @Override
    public void onPageLoaded(WebView view)
    {
        tokenView.callToJS("refresh()");
    }

    @Override
    public void onPageRendered(WebView view)
    {
        findViewById(R.id.layout_webwrapper).setVisibility(View.VISIBLE);
        if (parsePass == 1)
        {
            tokenView.reload();
        }

        parsePass++;
    }

    @Override
    public boolean overridePageLoad(WebView view, String url)
    {
        if (handleMapClick(url))
            return true;                     //handle specific map click
        else
            return handleURLClick(url);      //otherwise handle an attempt to visit a URL from TokenScript. If URL isn't in the approved DAPP list then fail
    }

    @Override
    public void onPause()
    {
        super.onPause();
        viewModel.resetSignDialog();
    }

    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        tokenView.saveState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            tokenView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onSignPersonalMessage(EthereumMessage message)
    {
        dialog = new SignMessageDialog(this, message);
        dialog.setAddress(token.getAddress());
        dialog.setMessage(message.value);
        dialog.setOnApproveListener(v -> {
            dialog.dismiss();
            messageToSign = message;
            viewModel.getAuthorisation(this, this);
        });
        dialog.setOnRejectListener(v -> {
            tokenView.onSignCancel(message);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void testRecoverAddressFromSignature(String message, String sig)
    {
        String prefix = DappBrowserFragment.PERSONAL_MESSAGE_PREFIX + message.length();
        byte[] msgHash = (prefix + message).getBytes();
        String msgBytes = Numeric.toHexString(msgHash);
        if (BuildConfig.DEBUG) System.out.println(msgBytes);

        byte[] equivHash = Hash.sha3(msgHash);
        String hashBytes = Numeric.toHexString(equivHash);
        if (BuildConfig.DEBUG) System.out.println(hashBytes);

        byte[] signatureBytes = Numeric.hexStringToByteArray(sig);
        Sign.SignatureData sd = sigFromByteArray(signatureBytes);
        String addressRecovered;

        try
        {
            BigInteger recoveredKey = Sign.signedMessageToKey(msgHash, sd);
            addressRecovered = "0x" + Keys.getAddress(recoveredKey);
            if (BuildConfig.DEBUG) System.out.println("Recovered: " + addressRecovered);
        }
        catch (SignatureException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onProgress(boolean shouldShowProgress) {
        hideDialog();
        if (shouldShowProgress) {
            alertDialog = new AWalletAlertDialog(this);
            alertDialog.setProgressMode();
            alertDialog.setTitle(R.string.dialog_title_sign_message);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    private void hideDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode,resultCode,intent);

        if (requestCode >= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS && requestCode <= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS + 10)
        {
            gotAuthorisation(resultCode == RESULT_OK);
        }
    }

    protected void showProgressSpinner(boolean show)
    {
        if (show) handler.post(progress);
        else handler.post(progressOff);
    }

    private void urlNotWhiteListed(String url)
    {
        hideDialog();
        alertDialog = new AWalletAlertDialog(this);
        alertDialog.setIcon(AWalletAlertDialog.ERROR);
        alertDialog.setTitle(R.string.error_not_whitelisted);
        alertDialog.setMessage(getString(R.string.explain_not_whitelisted, url));
        alertDialog.setButtonText(R.string.button_ok);
        alertDialog.setButtonListener(v ->alertDialog.dismiss());
        alertDialog.show();
    }

    private void openInDappBrowser(String url)
    {
        Intent intent = new Intent(FunctionActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private boolean handleURLClick(String url)
    {
        if (!TextUtils.isEmpty(url))
        {
            //try one of the whitelisted URL's and open in dapp browser
            List<DApp> myDapps = DappBrowserUtils.getDappsList(getApplicationContext());
            for (DApp thisDapp : myDapps)
            {
                //must start with the whitelisted URL, to avoid simply adding the URL as a param
                if (url.startsWith(thisDapp.getUrl()))
                {
                    openInDappBrowser(url);
                    return true;
                }
            }

            //not whitelisted
            urlNotWhiteListed(url);
        }

        return true;
    }

    private boolean handleMapClick(String url)
    {
        if (!TextUtils.isEmpty(url))
        {
            int index = url.indexOf(C.DAPP_PREFIX_MAPS);
            if (index > 0)
            {
                index += C.DAPP_PREFIX_MAPS.length();
                if (index < url.length())
                {
                    String geoCoords = url.substring(index);
                    Uri gmmIntentUri = Uri.parse("geo:My+Location?q=" + geoCoords);

                    //pass the location to the intent
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                               gmmIntentUri);
                    startActivity(intent);
                    //finish this activity
                    functionSuccess();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void gotAuthorisation(boolean gotAuth)
    {
        if (gotAuth) viewModel.completeAuthentication(SIGN_DATA);
        else viewModel.failedAuthentication(SIGN_DATA);

        if (gotAuth)
        {
            DAppFunction dAppFunction = new DAppFunction()
            {
                @Override
                public void DAppError(Throwable error, Signable message)
                {
                    showProgressSpinner(false);
                    tokenView.onSignCancel(message);
                    functionFailed();
                }

                @Override
                public void DAppReturn(byte[] data, Signable message)
                {
                    showProgressSpinner(false);
                    String signHex = Numeric.toHexString(data);
                    signHex = Numeric.cleanHexPrefix(signHex);
                    tokenView.onSignPersonalMessageSuccessful(message, signHex);
                    testRecoverAddressFromSignature(message.getMessage(), signHex);
                }
            };

            String convertedMessage = messageToSign.value;
            String signMessage = DappBrowserFragment.PERSONAL_MESSAGE_PREFIX
                    + convertedMessage.length()
                    + convertedMessage;
            signMessage(signMessage.getBytes(), dAppFunction, messageToSign);
        }
    }

    @Override
    public void cancelAuthentication()
    {

    }

    @Override
    public void handleTokenScriptFunction(String function, List<BigInteger> selection)
    {
        args.clear();
        //run the onConfirm JS and await callback
        tokenView.TScallToJS(function, "onConfirm" + "('sig')", this);
    }

    @Override
    public void callToJSComplete(String function, String result)
    {
        completeTokenScriptFunction(function);
    }

    @Override
    public void enterKeyPressed()
    {
        KeyboardUtils.hideKeyboard(getCurrentFocus());
    }

    @Override
    public void setValues(Map<String, String> updates)
    {
        boolean newValues = false;
        //called when values update
        for (String key : updates.keySet())
        {
            String value = updates.get(key);
            String old = args.put(key, updates.get(key));
            if (!value.equals(old)) newValues = true;
        }

        if (newValues)
        {
            viewModel.getAssetDefinitionService().addLocalRefs(args);
            //rebuild the view
            getAttrs();
        }
    }

    /**
     * JavaScript methods to obtain values from within the rendered view
     */
    private interface CalcJsValueCallback
    {
        void calculationCompleted(String value, String result, TokenscriptElement e, Attribute attr);
        void unresolvedSymbolError(String value);
    }

    private void evaluateJavaScript(CalcJsValueCallback callback, String value, TokenscriptElement e, Attribute attr)
    {
        tokenView.evaluateJavascript(
                "(function() { var x = document.getElementById(\"" + value + "\");\n" +
                        "            return x.value; })();",
                html -> {
                    StringBuilder sb = new StringBuilder();
                    for (char ch : html.toCharArray()) if (ch!='\"') sb.append(ch);
                    if (!html.equals("null"))
                    {
                        callback.calculationCompleted(value, sb.toString(), e, attr);
                    }
                    else
                    {
                        getValueFromInnerHTML(callback, value, e, attr); //the input wasn't in the .value field, try innerHTML before failing
                    }
                });
    }

    private void getValueFromInnerHTML(CalcJsValueCallback callback, String value, TokenscriptElement e, Attribute attr)
    {
        tokenView.evaluateJavascript(
                "(function() { var x = document.getElementById(\"" + value + "\");\n" +
                        "            return x.innerHTML; })();",
                html -> {
                    StringBuilder sb = new StringBuilder();
                    for (char ch : html.toCharArray()) if (ch!='\"') sb.append(ch);
                    if (!html.equals("null"))
                    {
                        callback.calculationCompleted(value, sb.toString(), e, attr);
                    }
                    else
                    {
                        callback.unresolvedSymbolError(value);
                    }
                });
    }

    private void repopulateInputField(String key, String value)
    {
        tokenView.evaluateJavascript(
                "(function() { document.getElementById(\"" + key + "\").innerHTML = \"" + value + "\"; })();",
                html -> {
                    System.out.println("Worked?");
                });
    }
}
