package fr.xtof54.sgfsearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
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

	static void test2() throws Exception {
		GZIPInputStream f = new GZIPInputStream(new FileInputStream("/home/xtof/dgsArchive.tgz"));
		TarInputStream tf = new TarInputStream(f);
		TarEntry entry;
		int count;
		int n=0;
		byte data[] = new byte[1000000];
		while((entry = tf.getNextEntry()) != null) {
			String nom=entry.getName();
			while((count = tf.read(data)) != -1) {
				String sgf=new String(data,0,count);
				System.out.println("loaded "+nom+" .. "+sgf);
// attention: une entree ne termine pas forcement un fichier.
				// il faut plusieurs entrees, mais le nom est le meme
				// par contre, je ne sais pas si on peut avoir plusieurs fichiers
				// dans une entree
				PrintWriter fo = new PrintWriter(new FileWriter("/tmp/tt"+n));
				fo.println(nom);
				fo.println(sgf);
				fo.close();
				
				++n;
if (n>=2) System.exit(1);
			}
		}
		tf.close();
	}

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

	public static void main(String args[]) throws Exception {
		test2();
	}
}
