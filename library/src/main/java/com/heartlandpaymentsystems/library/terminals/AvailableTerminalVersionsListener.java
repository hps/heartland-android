package com.heartlandpaymentsystems.library.terminals;

import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;

import java.util.List;

public interface AvailableTerminalVersionsListener {

    /**
     * Callback fired when information about the available terminal versions is successfully
     * received.
     */
    void onAvailableTerminalVersionsReceived(TerminalUpdateType type,
                                             List<String> versions);

    /**
     * Callback fired when an error is encountered when trying to get terminal version info.
     *
     * @param error {@link Error}
     */
    void onTerminalVersionInfoError(Error error);
}
