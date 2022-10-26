package com.alphawallet.app.analytics;

public class Analytics
{
    public static final String ERROR = "Error";

    // Properties
    public static final String PROPS_IMPORT_WALLET_TYPE = "import_wallet_type";
    public static final String PROPS_WALLET_TYPE = "wallet_type";
    public static final String PROPS_GAS_SPEED = "gas_speed";
    public static final String PROPS_URL = "url";
    public static final String PROPS_QR_SCAN_SOURCE = "qr_scan_source";
    public static final String PROPS_ACTION_SHEET_SOURCE = "action_sheet_source";
    public static final String PROPS_ACTION_SHEET_MODE = "action_sheet_mode";
    public static final String PROPS_SWAP_FROM_TOKEN = "from_token";
    public static final String PROPS_SWAP_TO_TOKEN = "to_token";
    public static final String PROPS_ERROR_MESSAGE = "error_message";

    public static final String PROPS_CUSTOM_NETWORK_NAME = "network_name";
    public static final String PROPS_CUSTOM_NETWORK_RPC_URL = "rpc_url";
    public static final String PROPS_CUSTOM_NETWORK_CHAIN_ID = "chain_id";
    public static final String PROPS_CUSTOM_NETWORK_SYMBOL = "symbol";
    public static final String PROPS_CUSTOM_NETWORK_IS_TESTNET = "is_testnet";

    public enum Navigation
    {
        WALLET("Screen: Wallet"),
        ACTIVITY("Screen: Activity"),
        BROWSER("Screen: Browser"),
        SETTINGS("Screen: Settings"),
        MY_ADDRESS("Screen: My Wallet Address"),
        MY_WALLETS("Screen: My Wallets"),
        BACK_UP_WALLET("Screen: Back Up Wallet"),
        SHOW_SEED_PHRASE("Screen: Show Seed Phrase"),
        NAME_WALLET("Screen: Name Wallet"),
        SELECT_NETWORKS("Screen: Select Networks"),
        CHANGE_LANGUAGE("Screen: Change Language"),
        CHANGE_CURRENCY("Screen: Change Currency"),
        SETTINGS_DARK_MODE("Screen: Settings - Dark Mode"),
        SETTINGS_ADVANCED("Screen: Settings - Advanced"),
        SETTINGS_SUPPORT("Screen: Settings - Support"),

        ADD_CUSTOM_NETWORK("Screen: Add Custom Network"),

        IMPORT_WALLET("Screen: Import Wallet"),
        COINBASE_PAY("Screen: Buy with Coinbase Pay"),
        WALLET_CONNECT_SESSION_DETAIL("Screen: Wallet Connect Session Detail"),
        WALLET_CONNECT_SESSIONS("Screen: Wallet Connect Sessions"),

        ACTION_SHEET("Screen: ActionSheet"),
        ACTION_SHEET_FOR_TRANSACTION_CONFIRMATION("Screen: Txn Confirmation"),
        ACTION_SHEET_FOR_TRANSACTION_CONFIRMATION_SUCCESSFUL("Screen: Txn Confirmation Successful"),
        ACTION_SHEET_FOR_TRANSACTION_CONFIRMATION_FAILED("Screen: Txn Confirmation Failed"),
        TOKEN_SWAP("Screen: Token Swap"),
        //        SIGN_MESSAGE_REQUEST("Screen: Sign Message Request"),

        SCAN_QR_CODE("Screen: QR Code Scanner");


//        ON_RAMP("Screen: Fiat On-Ramp"),
//        ON_UNISWAP("Screen: Uniswap"),
//        ON_XDAI_BRIDGE("Screen: xDai Bridge"),
//        ON_HONEYSWAP("Screen: Honeyswap"),
//        ON_ONEINCH("Screen: Oneinch"),
//        ON_CARTHAGE("Screen: Carthage"),
//        ON_ARBITRUM_BRIDGE("Screen: Arbitrum Bridge"),
//        ON_QUICK_SWAP("Screen: QuickSwap"),
//        FALLBACK("Screen: <Fallback>"),
//        SWITCH_SERVERS("Screen: Switch Servers"),
//        SHOW_DAPPS("Screen: Dapps"),
//        SHOW_HISTORY("Screen: Dapp History"),
//        TAP_BROWSER_MORE("Screen: Browser More Options"),
//        DEEP_LINK("Screen: DeepLink"),
//        FAQ("Screen: FAQ"),
//        DISCORD("Screen: Discord"),
//        TELEGRAM_CUSTOMER_SUPPORT("Screen: Telegram: Customer Support"),
//        TWITTER("Screen: Twitter"),
//        REDDIT("Screen: Reddit"),
//        FACEBOOK("Screen: Facebook"),
//        GITHUB("Screen: Github"),
//        EXPLORER("Screen: Explorer"),
//        OPEN_SHORTCUT("Screen: Shortcut"),
//        OPEN_HELP_URL("Screen: Help URL"),
//        BLOCKSCAN_CHAT("Screen: Blockscan Chat");

        private final String screenName;

        Navigation(String screenName)
        {
            this.screenName = screenName;
        }

        public String getValue()
        {
            return screenName;
        }
    }

    public enum Action
    {
        IMPORT_WALLET("Import Wallet"),
        USE_GAS_WIDGET("Use Gas Widget"),
        LOAD_URL("Load URL"),
        ACTION_SHEET_COMPLETED("ActionSheet Completed"),
        ACTION_SHEET_CANCELLED("ActionSheet Cancelled"),
        SCAN_QR_CODE_SUCCESS("Scan QR Code Completed"),
        SCAN_QR_CODE_CANCELLED("Scan QR Code Cancelled"),
        SCAN_QR_CODE_ERROR("Scan QR Code Error"),

        ADD_CUSTOM_CHAIN("Add Custom Chain"),
        EDIT_CUSTOM_CHAIN("Edit Custom Chain"),

        WALLET_CONNECT_SESSION_REQUEST("WalletConnect - Session Request"),
        WALLET_CONNECT_SESSION_APPROVED("WalletConnect - Session Approved"),
        WALLET_CONNECT_SESSION_REJECTED("WalletConnect - Session Rejected"),
        WALLET_CONNECT_SESSION_ENDED("WalletConnect - Session Ended"),
        WALLET_CONNECT_SIGN_MESSAGE_REQUEST("WalletConnect - Sign Message Request"),
        WALLET_CONNECT_SIGN_TRANSACTION_REQUEST("WalletConnect - Sign Transaction Request"),
        WALLET_CONNECT_SEND_TRANSACTION_REQUEST("WalletConnect - Send Transaction Request"),
        WALLET_CONNECT_SWITCH_NETWORK_REQUEST("WalletConnect - Switch Network Request"),
        WALLET_CONNECT_TRANSACTION_SUCCESS("WalletConnect - Transaction Success"),
        WALLET_CONNECT_TRANSACTION_FAILED("WalletConnect - Transaction Failed"),
        WALLET_CONNECT_TRANSACTION_CANCELLED("WalletConnect - Transaction Cancelled"),
        WALLET_CONNECT_CONNECTION_TIMEOUT("WalletConnect - Connection Timeout");

//        WALLET_CONNECT_CANCEL("WalletConnect Cancel"),
//        WALLET_CONNECT_CONNECTION_FAILED("WalletConnect Connection Failed"),


//        SIGN_MESSAGE_REQUEST("Sign Message Request"),
//        CANCEL_SIGN_MESSAGE_REQUEST("Cancel Sign Message Request"),
//        SWITCHED_SERVER("Switch Server Completed"),
//        CANCELS_SWITCH_SERVER("Switch Server Cancelled"),
        //        RECTIFY_SEND_TRANSACTION_ERROR_IN_ACTION_SHEET("Rectify Send Txn Error"),


//        CLEAR_BROWSER_CACHE("Clear Browser Cache"),
        //        SHARE_URL("Share URL"),
//        ADD_DAPP("Add Dapp"),
//        ENTER_URL("Enter URL"),
        //        RELOAD_BROWSER("Reload Browser"),


//        PING_INFURA("Ping Infura"),
//        NAME_WALLET("Name Wallet"),
//        FIRST_WALLET_ACTION("First Wallet Action"),
//        SUBSCRIBE_TO_EMAIL_NEWSLETTER("Subscribe Email Newsletter"),
//        TAP_SAFARI_EXTENSION_REWRITTEN_URL("Tap Safari Extension Rewritten URL");

        private final String actionName;

        Action(String actionName)
        {
            this.actionName = actionName;
        }

        public String getValue()
        {
            return actionName;
        }
    }
}