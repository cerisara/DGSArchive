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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	public static void parseDGSarchive(SgfParser parser) {
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
	public static void test1() throws Exception {
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

	static void test5() {
		final int[] sum = new int[2015-2000];
		final int[] nb = new int[sum.length];
		class Printer implements SgfParser {
			int year, month, day;
			int elo=-1;
			public Printer() {
				Arrays.fill(nb,0);
			}
			public void parse(final String sgf) {
				int i=sgf.indexOf("DT[");
				if (i>=0) {
					int ii=sgf.indexOf(']',i);
					int j=sgf.lastIndexOf(',',ii);
					int k=sgf.lastIndexOf('[',ii);
					if (k>j) j=k;
					if (j>=0) {
						Pattern p = Pattern.compile("(20\\d\\d)-(\\d\\d)-(\\d\\d)");
						Matcher m = p.matcher(sgf.substring(j+1, ii));
						if (m.matches()) {
							year = Integer.parseInt(m.group(1));
//							month = Integer.parseInt(m.group(2));
//							day = Integer.parseInt(m.group(3));
						}
					}
				}
				i=sgf.indexOf("White Start Rating:");
				if (i>=0) {
					int j=sgf.indexOf(" ELO ",i);
					if (j>=0) {
						int k=sgf.indexOf('\n',j+5);
						int kk=sgf.indexOf(']',j+5);
						if (kk>=0&&kk<k) k=kk;
//						int ee=j+70>=sgf.length()?sgf.length():j+70;
//						System.out.println("dbug "+ee+" "+sgf.length()+" "+i+" "+j);
//						System.out.println("dbug "+ee+" "+sgf.length()+" "+sgf.substring(i,ee));
						elo = Integer.parseInt(sgf.substring(j+5,k));
						i=year-2000;
						sum[i]+=elo;
						nb[i]++;
					}
				}
				i=sgf.indexOf("Black Start Rating:");
				if (i>=0) {
					int j=sgf.indexOf(" ELO ",i);
					if (j>=0) {
						int k=sgf.indexOf('\n',j+5);
						int kk=sgf.indexOf(']',j+5);
						if (kk>=0&&kk<k) k=kk;
						elo = Integer.parseInt(sgf.substring(j+5,k));
						i=year-2000;
						sum[i]+=elo;
						nb[i]++;
					}
				}
			}
		}
		parseDGSarchive(new Printer());
		for (int i=0;i<sum.length;i++) {
			if (nb[i]>0) {
				float e = (float)sum[i]/(float)nb[i];
				System.out.println((2000+i)+" "+e);
			}
		}
	}

	public static void main(String args[]) throws Exception {
//		test3();
//		test5();
//		test1();
	}
}
