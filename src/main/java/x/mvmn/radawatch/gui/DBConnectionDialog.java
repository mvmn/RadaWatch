package x.mvmn.radawatch.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import x.mvmn.radawatch.swing.SwingHelper;

public class DBConnectionDialog extends JDialog {
	private static final long serialVersionUID = -7224903503193785935L;

	protected final JTextField tfHost = new JTextField("localhost");
	protected final JTextField tfPort = new JTextField("3306");
	protected final JTextField tfDbName = new JTextField("radawatch");
	protected final JTextField tfLogin = new JTextField("root");
	protected final JPasswordField tfPassword = new JPasswordField();

	protected final JRadioButton rbEmbeddedDb = new JRadioButton("Embedded H2 DB");
	protected final JRadioButton rbMySQL = new JRadioButton("MySQL");

	protected final JButton btnOk = new JButton("Ok");

	public DBConnectionDialog() {
		this.setModal(true);

		final Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		final JPanel mySqlInputsPanel = new JPanel(new GridLayout(5, 1));
		final JPanel rbPanel = new JPanel(new GridLayout(1, 2));

		rbPanel.add(rbEmbeddedDb);
		rbPanel.add(rbMySQL);
		tfHost.setBorder(BorderFactory.createTitledBorder("Host"));
		mySqlInputsPanel.add(tfHost);
		tfPort.setBorder(BorderFactory.createTitledBorder("Port"));
		mySqlInputsPanel.add(tfPort);
		tfDbName.setBorder(BorderFactory.createTitledBorder("DB name"));
		mySqlInputsPanel.add(tfDbName);
		tfLogin.setBorder(BorderFactory.createTitledBorder("Login"));
		mySqlInputsPanel.add(tfLogin);
		tfPassword.setBorder(BorderFactory.createTitledBorder("Password"));
		mySqlInputsPanel.add(tfPassword);

		tfHost.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfPort.requestFocus();
			}
		});
		tfPort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfDbName.requestFocus();
			}
		});
		tfDbName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfLogin.requestFocus();
			}
		});
		tfLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfPassword.requestFocus();
			}
		});
		tfPassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnOk.requestFocus();
			}
		});
		rbPanel.setBorder(BorderFactory.createTitledBorder("Select DB type"));
		mySqlInputsPanel.setBorder(BorderFactory.createTitledBorder("MySQL settings"));

		contentPane.add(rbPanel, BorderLayout.NORTH);
		contentPane.add(mySqlInputsPanel, BorderLayout.CENTER);
		contentPane.add(btnOk, BorderLayout.SOUTH);

		final ButtonGroup rbGroup = new ButtonGroup();
		rbGroup.add(rbEmbeddedDb);
		rbGroup.add(rbMySQL);
		rbEmbeddedDb.setSelected(true);
		rbMySQL.setSelected(false);

		enableMySqlFields(false);

		rbEmbeddedDb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				enableMySqlFields(false);
			}
		});

		rbMySQL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				enableMySqlFields(true);
			}
		});

		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DBConnectionDialog.this.setVisible(false);
			}
		});
	}

	protected void enableMySqlFields(final boolean enable) {
		tfHost.setEnabled(enable);
		tfPort.setEnabled(enable);
		tfDbName.setEnabled(enable);
		tfLogin.setEnabled(enable);
		tfPassword.setEnabled(enable);
	}

	public void showInput() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					DBConnectionDialog.this.pack();
					SwingHelper.moveToScreenCenter(DBConnectionDialog.this);
					DBConnectionDialog.this.setVisible(true);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean useEmbeddedDb() {
		return rbEmbeddedDb.isSelected();
	}

	public String getDbHost() {
		return tfHost.getText();
	}

	public Integer getDbPort() {
		Integer result = null;
		try {
			result = Integer.parseInt(tfPort.getText().trim());
		} catch (final Exception e) {
		}

		return result;
	}

	public String getDbName() {
		return tfDbName.getText();
	}

	public String getLogin() {
		return tfLogin.getText();
	}

	public String getPasswordOnce() {
		final char[] charPass = tfPassword.getPassword();
		tfPassword.setText(null);
		return new String(charPass);
	}

}
