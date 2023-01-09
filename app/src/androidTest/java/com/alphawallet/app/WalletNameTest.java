package com.alphawallet.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.alphawallet.app.assertions.Should.shouldNotSee;
import static com.alphawallet.app.assertions.Should.shouldSee;
import static com.alphawallet.app.steps.Steps.createNewWallet;
import static com.alphawallet.app.steps.Steps.getWalletAddress;
import static com.alphawallet.app.steps.Steps.gotoWalletPage;
import static com.alphawallet.app.steps.Steps.input;
import static com.alphawallet.app.steps.Steps.watchWalletWithENS;
import static com.alphawallet.app.util.Helper.click;

import com.alphawallet.app.util.Helper;

import org.junit.Test;

public class WalletNameTest extends BaseE2ETest
{
    public void shouldSeeFormattedAddress(String address) {
        shouldSee(address.substring(0, 6) + "..." + address.substring(address.length() - 4)); // 0xabcd...wxyz
    }

    @Test
    public void should_show_custom_name_instead_of_address()
    {
        createNewWallet();
        String address = getWalletAddress();

        gotoWalletPage();
        shouldSeeFormattedAddress(address);

        renameWalletTo("TestWallet");
        shouldSee("TestWallet");

        renameWalletTo("");
        shouldSeeFormattedAddress(address);
    }

    private void renameWalletTo(String name)
    {
        click(withId(R.id.action_my_wallet));
        click(withSubstring("Rename this Wallet"));
        onView(withId(R.id.edit_text)).perform(replaceText(name));
        input(R.id.input_name, name);
        click(withText("Save Name"));
        Helper.wait(2);
    }

    @Test
    public void should_show_ENS_name_instead_of_address()
    {
        watchWalletWithENS("vitalik.eth");
        shouldSee("vitalik.eth");
    }

    @Test
    public void should_show_custom_name_instead_of_ENS_name()
    {
    }
}
