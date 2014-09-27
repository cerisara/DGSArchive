package fr.xtof54.sgfsearch;

import java.io.BufferedReader;
import java.io.FileReader;

public class SgfLoad {
	static Board goban;

	public static void load(String n) throws Exception {
		GoFrame bi = new GoFrame("toto");
		goban = new Board(19, bi);
		BufferedReader f = new BufferedReader(new FileReader(n));
		goban.load(f);
		f.close();
	}
	public static void main(String args[]) throws Exception {
		load("/home/xtof/simple.sgf");
		{
			int i=0;
			try {
				for (i=0;;i++) {
					goban.gotoMove(i);
				}
			} catch (Exception e) {
				System.out.println("found nmoves "+i);
			}
		}
		// position: i=column, j=row, en partant du coin en haut à gauche
		for (int i=0;i<19;i++)
			for (int j=0;j<19;j++) {
				int c=goban.P.color(i, j);
				// 1=black, -1=white
				if (c!=0) System.out.println(i+" "+j+" "+c);
			}
	}
}
