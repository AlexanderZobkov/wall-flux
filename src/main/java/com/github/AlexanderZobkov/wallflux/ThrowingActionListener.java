package com.github.AlexanderZobkov.wallflux;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@FunctionalInterface
interface ThrowingActionListener extends ActionListener {
    void actionPerformedWithException(ActionEvent e) throws Exception;

    @Override
    default void actionPerformed(final ActionEvent e) {
        try {
            actionPerformedWithException(e);
        } catch (Exception error) {
            handleException(error);
        }
    }

    default void handleException(final Exception error) {
        JOptionPane.showMessageDialog(
                null,
                "Error: " + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
