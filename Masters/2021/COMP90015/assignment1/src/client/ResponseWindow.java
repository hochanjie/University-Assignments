/**
 * @Title: COMP90015 Distributed Systems 2021 Sem 1 Assignment 1
 * @Description: Multithreaded dictionary server
 * @author Chan Jie Ho
 * @studentID 961948
 * @email chanjieh@student.unimelb.edu.au
 * @date 19/4/2021
 */

package client;

import javax.swing.*;

/**
 * A class to display the response received from the server.
 */

public class ResponseWindow {

    /* ============================================================================================================== */
    /* GUI COMPONENTS */
    /* -------------- */

    private JPanel mainPanel;
    private JPanel responsePanel;
    private JLabel response;
    private JList meaningList;

    /* ============================================================================================================== */
    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * ResponseWindow constructor that creates the response window.
     */
    public ResponseWindow(String word, String[] meanings) {

        JFrame frame = new JFrame("Response");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        meaningList.setVisible(false);
        response.setText(word);

        // Print out the list of meanings if not null
        if ((meanings != null) && (meanings.length != 0)) {
            DefaultListModel model = new DefaultListModel();
            for (int i = 0, n = meanings.length; i < n; i++) {
                model.addElement(meanings[i]);
            }
            meaningList.setVisible(true);
            meaningList.setModel(model);
        }
    }

    /* ============================================================================================================== */
}


