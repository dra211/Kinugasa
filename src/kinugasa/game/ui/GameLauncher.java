/*
 * The MIT License
 *
 * Copyright 2022 Dra.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package kinugasa.game.ui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.File;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import kinugasa.game.GameOption;
import kinugasa.game.LockUtil;
import kinugasa.game.system.GameSystem;
import kinugasa.graphics.RenderingQuality;

/**
 *
 * @author owner
 */
public class GameLauncher extends javax.swing.JFrame {

	/**
	 * Creates new form GameOptionFrame
	 */
	public GameLauncher(String name) {
		initComponents();
		init(name);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        lockFileDelete = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        windowSize = new javax.swing.JComboBox<>();
        logFile = new javax.swing.JTextField();
        mouse = new javax.swing.JCheckBox();
        keyboard = new javax.swing.JCheckBox();
        gamepad = new javax.swing.JCheckBox();
        rendering = new javax.swing.JComboBox<>();
        language = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        fps = new javax.swing.JComboBox<>();
        debugMode = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        args = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("オプション");
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
        setResizable(false);

        jLabel1.setText("ウインドウサイズ");

        jToolBar1.setRollover(true);

        lockFileDelete.setText("ロックファイルを削除する（ゲームが正常終了しなかった場合に選択してください）");
        lockFileDelete.setToolTipText("OFFにすると、ゲームを多重起動できなくなります。ゲームが正常に終了しなかった場合はロックファイルが残っているため、ONにしてください。");

        jLabel3.setText("ログファイル");

        jLabel4.setText("入力デバイス");

        jLabel5.setText("FPS");

        jLabel6.setText("レンダリング品質");

        jLabel7.setText("言語/Language");

        windowSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "720,480(*1)", "1440,960(*2)" }));

        logFile.setEditable(false);
        logFile.setToolTipText("ログファイルの格納場所を指定します。ログファイルは、ゲームの終了後消しても平気です。");
        logFile.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logFileMouseClicked(evt);
            }
        });
        logFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logFileActionPerformed(evt);
            }
        });

        mouse.setText("マウス");

        keyboard.setSelected(true);
        keyboard.setText("キーボード");

        gamepad.setSelected(true);
        gamepad.setText("コントローラー");

        rendering.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "速度（推奨）", "品質", "バランス" }));

        jButton1.setFont(new java.awt.Font("MS UI Gothic", 0, 24)); // NOI18N
        jButton1.setText("起動");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        fps.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "30", "50", "60", "61", "62" }));
        fps.setSelectedIndex(2);

        debugMode.setText("デバッグモードを有効にする");

        jLabel2.setText("ARGS：");

        args.setToolTipText("特別な引数がある場合、ここに半角スペース区切りで入力します。");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(debugMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lockFileDelete, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(logFile, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(windowSize, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(mouse, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(keyboard, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(gamepad, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                                    .addComponent(fps, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rendering, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(language, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(args)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(windowSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(logFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(mouse)
                    .addComponent(keyboard)
                    .addComponent(gamepad))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(rendering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(language, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lockFileDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(debugMode)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(args, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

		option = new GameOption(getTitle());

		String a = args.getText().trim().replaceAll(" ", "/");
		while (a.contains("//")) {
			a = a.replaceAll("//", "/");
		}
		String[] aa = a.contains("/") ? a.split("/") : new String[]{a};
		option.setArgs(aa);

		if (windowSize.getSelectedItem().toString().equals("720,480(*1)")) {
			option.setWindowSize(new Dimension(720, 480));
			option.setDrawSize(1);
		} else if (windowSize.getSelectedItem().toString().equals("1440,960(*2)")) {
			option.setWindowSize(new Dimension(1440, 960));
			option.setDrawSize(2);
		}
		option.setWindowLocation(new Point(0, 0));
		option.setCenterOfScreen();

		String logPath = new File(logFile.getText()).getParentFile().getAbsolutePath();
		String logName = new File(logFile.getText()).getName();
		option.setLogPath(logPath);
		option.setLogName(logName);

		option.setUseMouse(mouse.isSelected());
		option.setUseKeyboard(keyboard.isSelected());
		option.setUseGamePad(gamepad.isSelected());

		option.setFps(Integer.parseInt(fps.getSelectedItem().toString()));

		if (rendering.getSelectedItem().toString().equals("速度（推奨）")) {
			option.setRenderingQuality(RenderingQuality.SPEED);
		} else if (rendering.getSelectedItem().toString().equals("品質")) {
			option.setRenderingQuality(RenderingQuality.QUALITY);
		} else if (rendering.getSelectedItem().toString().equals("バランス")) {
			option.setRenderingQuality(RenderingQuality.DEFAULT);
		}

		String lang = language.getSelectedItem().toString();
		assert lang.contains(".") : "locale file is missmatch";

		option.setLang(new Locale(lang.replaceAll(".ini", "")));

		if (lockFileDelete.isSelected()) {
			LockUtil.deleteAllLockFile();
		}

		option.setDebugMode(debugMode.isSelected());

		setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void logFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logFileActionPerformed

		JFileChooser fileChooser = new JFileChooser(logFile.getText());
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() && f.canWrite() || (f.getName().toLowerCase().endsWith(".log") || f.getName().toLowerCase().endsWith(".txt"));
			}

			@Override
			public String getDescription() {
				return "テキストファイル(*.log,*.txt)";
			}
		});
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setMultiSelectionEnabled(false);
		int res = fileChooser.showSaveDialog(this);
		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File selected = fileChooser.getSelectedFile();
		if (selected == null) {
			return;
		}
		logFile.setText(selected.getAbsolutePath());


    }//GEN-LAST:event_logFileActionPerformed

    private void logFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logFileMouseClicked
		logFileActionPerformed(null);
    }//GEN-LAST:event_logFileMouseClicked

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(GameLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(GameLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(GameLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(GameLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>
		//</editor-fold>
		//</editor-fold>
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new GameLauncher("").setVisible(true);
			}
		});
	}

	private void init(String name) {
		//アイコンの設定
		setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());

		//ログファイルのデフォルトパス記入
		logFile.setText(new File("KinugasaGame.log").getAbsolutePath());

		//翻訳ファイルの選択肢追加
		for (File f : new File("translate").listFiles(p -> p.getName().toLowerCase().endsWith(".ini"))) {
			language.addItem(f.getName());
		}

		//ウインドウ位置の変更
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= getWidth() / 2;
		center.y -= getHeight() / 2;
		setLocation(center);

		//ウインドウタイトルの設定
		setTitle(name);
	}

	private GameOption option;

	public GameOption getGameOption() {
		return option;
	}

	public GameLauncher setMaouse(boolean f) {
		mouse.setSelected(f);
		return this;
	}

	public GameLauncher setKeyboard(boolean f) {
		keyboard.setSelected(f);
		return this;
	}

	public GameLauncher setGamePad(boolean f) {
		gamepad.setSelected(f);
		return this;
	}

	public GameLauncher lockMouse() {
		mouse.setEnabled(false);
		return this;
	}

	public GameLauncher lockKeyboard() {
		keyboard.setEnabled(false);
		return this;
	}

	public GameLauncher lockGamePad() {
		gamepad.setEnabled(false);
		return this;
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField args;
    private javax.swing.JCheckBox debugMode;
    private javax.swing.JComboBox<String> fps;
    private javax.swing.JCheckBox gamepad;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JCheckBox keyboard;
    private javax.swing.JComboBox<String> language;
    private javax.swing.JCheckBox lockFileDelete;
    private javax.swing.JTextField logFile;
    private javax.swing.JCheckBox mouse;
    private javax.swing.JComboBox<String> rendering;
    private javax.swing.JComboBox<String> windowSize;
    // End of variables declaration//GEN-END:variables
}
