package cn.deemons.tinypng;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.Arrays;

public class ChooseKeyDialog extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<String> listView;
    private JButton addButton;
    private JButton deleteButton;
    private final String DATA_KEY = "data_key";

    private ButtonListener mListener;
    private String mSelectKey;
    private KeyBean mKeyBean;

    private Project project;

    public ChooseKeyDialog(Project project) {
        this.project = project;

        setContentPane(contentPane);

        getRootPane().setDefaultButton(buttonOK);

        initView();

        initData();

        setListener();
    }

    private void initView() {
        setTitle("请选择API Key");
        setAutoRequestFocus(true);
        setAlwaysOnTop(true);
    }

    private void initData() {
        String json = PropertiesComponent.getInstance().getValue(DATA_KEY);
        if (!TextUtils.isEmpty(json)) {
            mKeyBean = new Gson().fromJson(json, KeyBean.class);
        } else {
            String[] strings = {"RkoczqavMW1TjFrlhFE9WakzGCYmc2Rh",
                    "HTVYrIxcjEoba-hUaqFwl7qzaMt5kQE_",
                    "vP7UbLIinPUX4g6Exxbj6accdEDqeK6z",
                    "q6PRmH_98vCYBWZqkIMFSkDlFp4P1gcq",
                    "i4S-XnT3ykpdjwSMrA8T1aPfxcZpvXVE"};
            mKeyBean = new KeyBean();
            mKeyBean.keys.addAll(Arrays.asList(strings));
            PropertiesComponent.getInstance().setValue(DATA_KEY, new Gson().toJson(mKeyBean));
        }


        listView.setListData(mKeyBean.keys);

        listView.setSelectedIndex(0);
        mSelectKey = mKeyBean.keys.get(0);
    }

    private void setListener() {
        deleteButton.setEnabled(false);

        listView.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                mSelectKey = listView.getSelectedValue();
                if (listView.getSelectedIndex() <= 4) {
                    deleteButton.setEnabled(false);
                } else deleteButton.setEnabled(true);
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                showInputDialog();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = listView.getSelectedIndex();
                mKeyBean.keys.remove(selectedIndex);
                refresh();

            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void refresh() {
        listView.setListData(mKeyBean.keys);
    }

    private void showInputDialog() {
        String key = Messages.showInputDialog(project, "请输入 TinyPNG 的 API KEY", "TinyPNG", Messages.getQuestionIcon());
        if (!TextUtils.isEmpty(key)) {
            mKeyBean.keys.add(key);
            refresh();
        }
        setVisible(true);

    }

    private void onOK() {
        if (mSelectKey == null || "".equals(mSelectKey)) return;
        dispose();
        if (mListener != null) {
            mListener.onClick(mSelectKey);
        }
    }

    @Override
    public void dispose() {
        PropertiesComponent.getInstance().unsetValue(DATA_KEY);
        PropertiesComponent.getInstance().setValue(DATA_KEY, new Gson().toJson(mKeyBean));

        super.dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setEnterButtonListener(ButtonListener listener) {
        mListener = listener;
    }


    public interface ButtonListener {
        void onClick(String api);
    }
}
