
package com.lewa.lockscreen.laml;

import com.lewa.lockscreen.laml.elements.ButtonScreenElement;
import com.lewa.lockscreen.laml.elements.ButtonScreenElement.ButtonAction;

public abstract interface InteractiveListener {
    public abstract void onButtonInteractive(ButtonScreenElement ele,
            ButtonAction action);
}
