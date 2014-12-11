package fr.xtof54.sgfsearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

public class SgfLoad {
	static Board goban;

	public static void load(String n) throws Exception {
		GoFrame bi = new GoFrame("toto");
		goban = new Board(19, bi);
		BufferedReader f = new BufferedReader(new FileReader(n));
		goban.load(f);
		f.close();
	}
	public static void show() {
		// position: i=column, j=row, en partant du coin en haut à gauche
		for (int i=0;i<19;i++)
			for (int j=0;j<19;j++) {
				int c=goban.P.color(i, j);
				// 1=black, -1=white
				if (c!=0) System.out.println(i+" "+j+" "+c);
			}
	}

	static public interface SgfParser {
		public void parse(String sgf);
	}
	static void parseDGSarchive(SgfParser parser) {
		try {
			GZIPInputStream f = new GZIPInputStream(new FileInputStream("/home/xtof/dgsArchive.tgz"));
			TarInputStream tf = new TarInputStream(f);
			TarEntry entry;
			int count;
			int n=0;
			byte data[] = new byte[1000000];
			String cursgf="";
			String prevnom="";
			while((entry = tf.getNextEntry()) != null) {
				String nom=entry.getName();
				if (nom.equals(prevnom)) {
				} else {
					if (cursgf.length()>0) {
						parser.parse(cursgf);
						++n;
					}
					prevnom=nom;
					cursgf="";
				}
				while((count = tf.read(data)) != -1) {
					cursgf+=new String(data,0,count);
				}
			}
			tf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// test de la decompression de l'archive DGS
	static void test2() throws Exception {
		GZIPInputStream f = new GZIPInputStream(new FileInputStream("/home/xtof/dgsArchive.tgz"));
		TarInputStream tf = new TarInputStream(f);
		TarEntry entry;
		int count;
		int n=0;
		byte data[] = new byte[1000000];
		String cursgf="";
		String prevnom="";
		while((entry = tf.getNextEntry()) != null) {
			String nom=entry.getName();
			if (nom.equals(prevnom)) {
			} else {
				if (cursgf.length()>0) {
					// save prev data
					PrintWriter fo = new PrintWriter(new FileWriter("/tmp/tt"+n+".sgf"));
					fo.println(nom);
					fo.println(cursgf);
					fo.close();
					++n;
					if (n>=10) System.exit(1);
				}
				prevnom=nom;
				cursgf="";
			}
			while((count = tf.read(data)) != -1) {
				cursgf+=new String(data,0,count);
			}
		}
		tf.close();
	}

	// test du loading d'un unique SGF
	public static void test1(String args[]) throws Exception {
		load("/home/xtof/simple.sgf");
		int i=0;
		try {
			for (i=0;;i++) {
				goban.gotoMove(i);
			}
		} catch (Exception e) {
			System.out.println("found nmoves "+i);
		}
	}

	// test: compte du nb de coups total joue
	// il faut 10' sur un portable
	// il y a 777,080 parties et 121,401,553 de coups 
	static void test3() {
		long tdeb = System.currentTimeMillis();
		class Compteur implements SgfParser {
			long nmv=0, ngames=0;
			@Override
			public void parse(final String sgf) {
				GoFrame bi = new GoFrame("toto");
				goban = new Board(19, bi);
				BufferedReader f = new BufferedReader(new InputStreamReader(new StringBufferInputStream(sgf)));
				try {
					goban.load(f);
					while (goban.goforward()) nmv++;
					ngames++;
					System.out.println("nmoves "+ngames+" "+nmv);
					f.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		Compteur c = new Compteur();
		parseDGSarchive(c);
		long tfin = System.currentTimeMillis();
		System.out.println("timediff "+tdeb+" "+tfin);
	}
	
	// prints the results
	/*
	 * Game ends: 18% on time, 36% resign, 46% agreement
	 * amongst resigns: 46% B resigns, 54% W resigns
	 * amongst time: 52.6% W wins, 47.4% B wins
	 * 48.8% B wins, 51.0% W wins, 0.2% draw
	 */
	static void test4() {
		final ArrayList<Float> scores = new ArrayList<Float>();
		class Printer implements SgfParser {
			public void parse(final String sgf) {
				int i=sgf.indexOf("Result:");
				if (i<0) System.out.println("noresult");
				else {
					int j=sgf.indexOf(']',i);
					if (j<0) j=sgf.length();
					String r = sgf.substring(i+7, j);
					if (r.contains("Resign")) {
					} else if (r.contains("Draw")) {
					} else if (r.contains("Time")) {
					} else {
						i=r.indexOf('+');
						float sc = Float.parseFloat(r.substring(i+1));
						scores.add(sc);
					}
				}
			}
		}
		parseDGSarchive(new Printer());
		float[] sc = new float[scores.size()];
		for (int i=0;i<sc.length;i++) sc[i]=scores.get(i);
		Histoplot.showit(sc);
	}
	
	public static void main(String args[]) throws Exception {
//		test3();
		test4();
	}
}
