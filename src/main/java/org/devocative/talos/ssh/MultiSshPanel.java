package org.devocative.talos.ssh;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSshPanel {
	private final List<SshInfo> infos;
	private final Map<String, JediTermWidget> widgets = new HashMap<>();

	// ------------------------------

	public MultiSshPanel(List<SshInfo> list) {
		infos = list;

		final JFrame frame = new JFrame("Multi SSH");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final ConnectorWBus bus = new ConnectorWBus(infos);

		final String masterName = infos.get(0).getName();
		bus.setMaster(masterName);

		frame.getContentPane().add(BorderLayout.NORTH, createRButtons(bus, masterName));
		frame.getContentPane().add(BorderLayout.CENTER, createTermWidgets(bus));

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setResizable(true);

		widgets.get(masterName).requestFocus();
	}

	// ------------------------------

	private Component createRButtons(ConnectorWBus bus, String masterName) {
		final Box box = Box.createHorizontalBox();

		final ButtonGroup bg = new ButtonGroup();
		for (SshInfo info : infos) {
			final JRadioButton button = new JRadioButton(info.getName(), info.getName().equals(masterName));
			button.setActionCommand(info.getName());
			button.addActionListener(e -> {
				final String name = e.getActionCommand();
				bus.setMaster(name);
				widgets.get(name).requestFocus();
			});
			bg.add(button);
			box.add(button);
		}

		final JRadioButton none = new JRadioButton("None", false);
		none.addActionListener(e -> bus.removeMaster());
		bg.add(none);
		box.add(none);

		return box;
	}

	private Component createTermWidgets(ConnectorWBus bus) {
		final Box box = Box.createVerticalBox();

		final SettingsProvider settingsProvider = new DefaultSettingsProvider();
		for (SshInfo info : infos) {
			box.add(new JLabel(String.format("%s - %s", info.getName(), info.getHostname())));

			final JediTermWidget widget = new JediTermWidget(80, 20, settingsProvider);
			widget.setTtyConnector(bus.getConnector(info.getName()));
			widget.start();

			box.add(widget);
			widgets.put(info.getName(), widget);
		}

		return box;
	}

	/*public static void main(String[] args) {
		MultiSshPanel panel = new MultiSshPanel();
		panel.addSession(new SshInfo("172.16.191.135", "user", "test", "r1"));
		panel.addSession(new SshInfo("172.16.191.132", "user", "test", "r2"));
		panel.show();
	}*/
}
