package com.heartlandpaymentsystems.library.terminals;

import androidx.annotation.Nullable;

public interface UpdateTerminalListener {

    /**
     * Callback fired when terminal update is in progress.
     */
    void onProgress(@Nullable Double completionPercentage, @Nullable String progressMessage);

    /**
     * Callback fired when terminal update is successfully completed.
     */
    void onTerminalUpdateSuccess();

    /**
     * Callback fired when an error is encountered when trying to update terminal.
     *
     * @param error {@link Error}
     */
    void onTerminalUpdateError(Error error);
}
