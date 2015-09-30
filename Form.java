package arhivator;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class Form
{
	// protected final Form form = this;
	volatile public static JTextArea jt;
	volatile private static Thread th;
	volatile public static JCheckBox Jserv;
	volatile public static JTextField hour;

	// public static Form form;

	public Form()
	{
		hour = new JTextField("24");
		hour.setToolTipText("Сколько часов длится пауза между запусками процесса");
		Jserv = new JCheckBox("Работать в фоне");
		Jserv.setSelected(true);
		JFrame jlog = new JFrame();
		jlog.setBounds(0, 80, 580, 500);
		jlog.setVisible(true);
		jlog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JFrame jf = new JFrame("Архиватор баз `Postgres SQL`");
		// jf.setIconImage(image);
		// jf.setSize(300, 600);
		jf.setLayout(new GridLayout(1, 3));

		JButton jb = new JButton("Архивировать");
		jf.add(jb);

		jb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				th = new Thread(new Runnable() {
					public void run() {
						try {
							new Script().DoScript();
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				});

				th.start();
			}
		});

		jt = new JTextArea(new Date().toString());
		jt.setLineWrap(true);
		jlog.add(new JScrollPane(jt));

		JButton bstop = new JButton("Прервать процесс");
		jf.add(bstop);
		bstop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// JOptionPane.showMessageDialog(null, "Попытка прервать
				// процесс");
				th.interrupt();

				JOptionPane.showMessageDialog(null, th.getState());

				// pane=new JOptionPane();
				// pane.setMessage("Start");
				// pane.

			}
		});

		jf.add(Jserv);

		jf.add(hour);

		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
}
