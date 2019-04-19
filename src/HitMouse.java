import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.Timer;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class HitMouse extends JFrame implements ActionListener, MouseMotionListener, MouseListener {

	Container contentPane = getContentPane();
	JButton jButton_start = new JButton("Start");
	JButton jButton_stop = new JButton("Stop");
	JLabel jLabel_time = new JLabel("<= Press Start to Play");
	JLabel jLabel_score = new JLabel("Score: 0");
	JLabel jLabel_copyright = new JLabel("By 吳柏霆 鄒亞微 王薏婷 劉曉薇");

	Random random = new Random();
	Hole[] hole = new Hole[10];
	int mode = 0;
	int score = 0;
	double time_left;
	Timer timer_refresh_screen = new javax.swing.Timer(500, this);

	Cursor cursor_normal = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("cursor@150.png").getImage(),
			new Point(30, 130), "custom cursor");
	Cursor cursor_down = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("cursor@150_d.png").getImage(),
			new Point(20, 20), "custom cursor");

	BufferedImage image_normal;
	BufferedImage image_hit;

	PlaySound playSonud_hit = new PlaySound("surprise.wav");
	PlaySound playSonud_bg;

	public HitMouse() throws Exception {

		super("Hit Mouse");
		setResizable(false);
		setSize(650, 650);
		setLocationRelativeTo(null);

		setCursor(cursor_normal);

		try {
			image_normal = ImageIO.read(new File("hhw.png"));
			image_hit = ImageIO.read(new File("hhw_hit.png"));
		} catch (Exception e) {
			System.out.println("Load Picture Fail");
		}

		playSonud_hit.start();

		hole[0] = new Hole();

		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 3; j++) {
				hole[(i - 1) * 3 + j] = new Hole();
				hole[(i - 1) * 3 + j].x = 200 + (j - 1) * 150;
				hole[(i - 1) * 3 + j].y = 200 + (i - 1) * 150;
			}
		}

		jButton_start.setBounds(30, 50, 80, 50);
		jButton_stop.setBounds(30, 120, 80, 50);

		jLabel_time.setBounds(150, 27, 300, 100);
		jLabel_time.setFont(new Font("Serif", Font.BOLD, 24));

		jLabel_score.setBounds(450, 27, 200, 100);
		jLabel_score.setFont(new Font("Serif", Font.BOLD, 24));

		jLabel_copyright.setBounds(290, 550, 500, 100);
		jLabel_copyright.setFont(new Font("Serif", Font.BOLD, 24));

		jButton_stop.setEnabled(false);

		contentPane.setLayout(null);
		contentPane.add(jButton_start);
		contentPane.add(jButton_stop);
		contentPane.add(jLabel_time);
		contentPane.add(jLabel_score);
		contentPane.add(jLabel_copyright);

		jButton_start.addActionListener(this);
		jButton_stop.addActionListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);

		setVisible(true);
	}

	public void paint(Graphics g) {
		super.paint(g);

		for (int i = 1; i <= 9; i++) {
			if (i != mode % 10)
				g.drawOval(hole[i].x - 50, hole[i].y - 50, 100, 100);
		}

		if (mode > 10) {
			g.drawImage(image_hit, hole[mode % 10].x - 50, hole[mode % 10].y - 100, this);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		} else if (mode != 0)
			g.drawImage(image_normal, hole[mode].x - 50, hole[mode].y - 100, this);
	}

	public void paintPlaying(Graphics g) {

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == timer_refresh_screen) {
			time_left -= 0.5;
			mode = random.nextInt(10);
			jLabel_time.setText("Time: " + (int) time_left + " sec(s)");
			repaint();
		} else if (e.getSource() == jButton_start) {
			try {
				jButton_start.setEnabled(false);
				jButton_stop.setEnabled(true);
				playSonud_bg = new PlaySound("bg.wav");
				Thread.sleep(50);
				playSonud_bg.start();
				Thread.sleep(50);
				synchronized (playSonud_bg) {
					playSonud_bg.notify();
				}
			} catch (Exception e1) {
			}
			time_left = 60;
			score = 0;
			timer_refresh_screen.start();
			return;
		} else if (e.getSource() == jButton_stop) {
			time_left = -1;

		}

		if (time_left <= 0) {
			// playSonud_bg.closeDataLine();
			playSonud_bg.stopSound();
			jButton_start.setEnabled(true);
			jButton_stop.setEnabled(false);
			timer_refresh_screen.stop();
			jLabel_time.setText("<= Press Start to Play");
			mode = 0;
			repaint();
		}
	}

	public void mouseDragged(MouseEvent e) {
	};

	public void mouseMoved(MouseEvent e) {
	};

	public void mouseEntered(MouseEvent e) {
	};

	public void mouseExited(MouseEvent e) {
	};

	public void mousePressed(MouseEvent e) {
		setCursor(cursor_down);

		if ((hole[mode].x - e.getX()) * (hole[mode].x - e.getX())
				+ (hole[mode].y - e.getY()) * (hole[mode].y - e.getY()) < 3000) {

			synchronized (playSonud_hit) {
				playSonud_hit.notify();
			}

			score++;
			jLabel_score.setText("Score: " + score);
			mode = mode * 10 + mode;
			repaint();
		}
	};

	public void mouseReleased(MouseEvent e) {
		setCursor(cursor_normal);
	};

	public void mouseClicked(MouseEvent e) {
	};

	public static void main(String args[]) throws Exception {
		HitMouse app = new HitMouse();
		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}

class Hole {
	int x;
	int y;
}

class PlaySound extends Thread {

	SourceDataLine sourceDataLine;
	AudioInputStream audioInputStream;
	AudioFormat audioFormat;
	DataLine.Info dataLineInfo;
	byte tempBuffer[] = new byte[32];
	File mySound;

	PlaySound(String mediaFileName) throws Exception {
		mySound = new File(mediaFileName);
		audioFormat = AudioSystem.getAudioInputStream(mySound).getFormat();
		dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
				audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
	}

	synchronized public void run() {
		while (true) {
			try {

				audioInputStream = AudioSystem.getAudioInputStream(mySound);

				// Open output device
				sourceDataLine.open(audioFormat);

				// Decode sound file to InputStream
				if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
					audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);

				sourceDataLine.start();

				wait();

				// Read InputStream and output to OutputStream
				int count;
				while ((count = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) > 0)
					sourceDataLine.write(tempBuffer, 0, count);

				// Block until all the data is output
				sourceDataLine.drain();

			} catch (Exception e) {
			}
		}
	}

	void stopSound() {
		try {
			audioInputStream.skip(audioInputStream.available());
			// audioInputStream.readAllBytes();
		} catch (IOException e) {
		}
	}
}