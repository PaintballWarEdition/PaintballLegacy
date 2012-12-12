package me.blablubbabc.paintball;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class Musiker {
	public boolean success;

	private Plugin plugin;

	private Melodie def_winMel;
	private Melodie def_defeatMel;
	private Melodie def_drawMel;

	private Melodie winMel;
	private Melodie defeatMel;
	private Melodie drawMel;

	private boolean use_defWin;
	private boolean use_defDefeat;
	private boolean use_defDraw;

	public Musiker(Plugin plugin, String winFilename, String defeatFilename, String drawFilename) {
		this.plugin = plugin;
		this.success = false;

		this.def_winMel = new Melodie();
		this.def_defeatMel = new Melodie();
		this.def_drawMel = new Melodie();

		this.winMel = new Melodie();
		this.defeatMel = new Melodie();
		this.drawMel = new Melodie();

		this.use_defWin = false;
		this.use_defDefeat = false;
		this.use_defDraw = false;

		File path;
		File def_winFile;
		File def_defeatFile;
		File def_drawFile;

		File winFile;
		File defeatFile;
		File drawFile;


		path = new File(plugin.getDataFolder().toString());
		if (!path.exists())
			path.mkdirs();

		///// write default melodie files:
		// Default win:
		def_winFile = new File(path + "/win.txt");
		InputStream in = null;
		OutputStream out = null;
		try {
			in = plugin.getResource("win.txt");
			if (in != null) {
				out = new FileOutputStream(def_winFile);
				byte[] buffer = new byte[10240];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
			} else {
				log("ERROR: Couldn't load the default win melody file from jar!");
				return;
			}
		} catch (Exception e) {
			log("ERROR: Couldn't write the default win melody file!");
			e.printStackTrace();
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//default defeat:
		def_defeatFile = new File(path + "/defeat.txt");
		in = null;
		out = null;
		try {
			in = plugin.getResource("defeat.txt");
			if (in != null) {
				out = new FileOutputStream(def_defeatFile);
				byte[] buffer = new byte[10240];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
			} else {
				log("ERROR: Couldn't load the default defeat melody file from jar!");
				return;
			}
		} catch (Exception e) {
			log("ERROR: Couldn't write the default defeat melody file!");
			e.printStackTrace();
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//default draw:
		def_drawFile = new File(path + "/draw.txt");
		in = null;
		out = null;
		try {
			in = plugin.getResource("draw.txt");
			if (in != null) {
				out = new FileOutputStream(def_drawFile);
				byte[] buffer = new byte[10240];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
			} else {
				log("ERROR: Couldn't load the default draw melody file from jar!");
				return;
			}
		} catch (Exception e) {
			log("ERROR: Couldn't write the default draw melody file!");
			e.printStackTrace();
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		////// get melodies:
		//default win
		log("Loading the default win melody: " + def_winFile.getName());
		def_winMel = loadMelodie(def_winFile);
		if (def_winMel == null) {
			return;
		}

		//// get win melodie:
		winFile = new File(path + "/" + winFilename + ".txt");
		if (!winFile.exists()) {
			log("ERROR: Couldn't find the specified win melody file.");
			log("Using the default win melody now: " + def_winFile.getName());
			use_defWin = true;
		} else {
			if (!winFile.equals(def_winFile)) {
				log("Loading the specified win melody now: "
						+ winFile.getName());
				winMel = loadMelodie(winFile);
				if (winMel == null) {
					log("ERROR: Couldn't load the specified win melody file!");
					log("Do you use a valid win melody file?");
					log("Using the default win melodie now: " + def_winFile.getName());
					use_defWin = true;
				}
			} else {
				log("Using the default win melody now: " + def_winFile.getName());
				use_defWin = true;
			}
		}

		//default defeat
		log("Loading the default defeat melody: " + def_defeatFile.getName());
		def_defeatMel = loadMelodie(def_defeatFile);
		if (def_defeatMel == null) {
			return;
		}

		//// get default melodie:
		defeatFile = new File(path + "/" + defeatFilename + ".txt");
		if (!defeatFile.exists()) {
			log("ERROR: Couldn't find the specified defeat melody file.");
			log("Using the default defeat melody now: " + def_defeatFile.getName());
			use_defDefeat = true;
		} else {
			if (!defeatFile.equals(def_defeatFile)) {
				log("Loading the specified defeat melody now: "
						+ defeatFile.getName());
				defeatMel = loadMelodie(defeatFile);
				if (defeatMel == null) {
					log("ERROR: Couldn't load the specified defeat melody file!");
					log("Do you use a valid defeat melody?");
					log("Using the default defeat melody now: " + def_defeatFile.getName());
					use_defDefeat = true;
				}
			} else {
				log("Using the default defeat melody now: " + def_defeatFile.getName());
				use_defDefeat = true;
			}
		}

		//default draw
		log("Loading the default draw melody: " + def_drawFile.getName());
		def_drawMel = loadMelodie(def_drawFile);
		if (def_drawMel == null) {
			return;
		}

		//// get default melodie:
		drawFile = new File(path + "/" + drawFilename + ".txt");
		if (!drawFile.exists()) {
			log("ERROR: Couldn't find the specified draw melody file.");
			log("Using the default draw melody now: " + def_drawFile.getName());
			use_defDraw = true;
		} else {
			if (!drawFile.equals(def_drawFile)) {
				log("Loading the specified draw melody now: "
						+ drawFile.getName());
				drawMel = loadMelodie(drawFile);
				if (drawMel == null) {
					log("ERROR: Couldn't load the specified draw melody file!");
					log("Do you use a valid draw melody?");
					log("Using the default draw melody now: " + def_drawFile.getName());
					use_defDraw = true;
				}
			} else {
				log("Using the default draw melody now: " + def_drawFile.getName());
				use_defDraw = true;
			}
		}
		//
		this.success = true;
	}

	public void playWin(final Plugin plugin, final Player p) {
		if(use_defWin) {
			def_winMel.play(plugin, p);
		}else {
			winMel.play(plugin, p);
		}
	}

	public void playDefeat(final Plugin plugin, final Player p) {
		if(use_defDefeat) {
			def_defeatMel.play(plugin, p);
		}else {
			defeatMel.play(plugin, p);
		}
	}
	
	public void playDraw(final Plugin plugin, final Player p) {
		if(use_defDraw) {
			def_drawMel.play(plugin, p);
		}else {
			drawMel.play(plugin, p);
		}
	}

	private Melodie loadMelodie(File file) {
		Melodie melodie = new Melodie();
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			int line = 0;
			while (scanner.hasNextLine()) {
				line++;
				String text = scanner.nextLine();
				// Spaces entfernen
				text.replaceAll(" ", "");
				//get tones:
				String[] tones = text.split("-");
				for(String ts : tones) {
					if(!ts.isEmpty()) {
						String[] ton = ts.split(":");
						if(ton.length != 2) {
							log(""+ton.length);
							log(""+ts);
							log(ton.toString());
							log("ERROR: Couldn't get the note in line: "+line);
							return null;
						}
						//instrument
						Instrument instrument = getInstrument(ton[0]);
						if(instrument == null) {
							log("ERROR: Couldn't get the instrument in line: "+line);
							return null;
						}
						//note (id between 0-24)
						Integer id = getNoteId(ton[1]);
						if(id == null) {
							log("ERROR: Couldn't get a valid note id in line: "+line);
							return null;
						}
						Note note = new Note(id);
						//delay in ticks (line * 2 ticks):
						long delay = (line-1)*2;
						//add note to melodie:
						melodie.addTon(new Ton(instrument, note, delay));
					}
				}
			}
			log("Scanned melody. Duration: " + line + " lines x 2 ticks => " +(line*2));
			return melodie;
		} catch (Exception e) {
			log("ERROR: Couldn't load the specified melody file.");
			e.printStackTrace();
			return null;
		} finally {
			if(scanner != null) scanner.close();
		}

	}

	private enum Instrus {
		PI, BG, BD, SD, ST
	}

	private Instrument getInstrument(String s) {
		try {
			Instrus i = Instrus.valueOf(s.toUpperCase());
			switch (i) {
			case PI: return Instrument.PIANO;
			case BG: return Instrument.BASS_GUITAR;
			case BD: return Instrument.BASS_DRUM;
			case SD: return Instrument.SNARE_DRUM;
			case ST: return Instrument.STICKS;
			default: return null;
			}
		} catch(Exception e) {
			return null;
		}
	}

	private Integer getNoteId(String s) {
		try {
			Integer id = Integer.parseInt(s);
			if(id < 0 || id > 24) return null;
			else return id;
		} catch(Exception e) {
			return null;
		}
	}

	private void log(String message) {
		System.out.println("[" + plugin.getName() + "] " + message);
	}

}
