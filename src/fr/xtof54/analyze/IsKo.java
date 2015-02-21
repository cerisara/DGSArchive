package fr.xtof54.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import debug.Wait;

import fr.xtof54.sgfsearch.Board;
import fr.xtof54.sgfsearch.GoFrame;
import fr.xtof54.sgfsearch.SgfLoad;
import fr.xtof54.sgfsearch.SgfLoad.SgfParser;

/**
 * Look for KO in SGF
 * 
 * @author xtof
 *
 */
@SuppressWarnings("deprecation")
public class IsKo implements SgfParser {

	class MoveBuffer {
		int[][] moveBuffer = new int[3][3];
		int posbuf=0;
		
		public void addmove(int[] m) {
			moveBuffer[posbuf][0]=m[0];
			moveBuffer[posbuf][1]=m[1];
			moveBuffer[posbuf++][2]=m[2];
			if (posbuf>=moveBuffer.length) posbuf=0;
		}
		public boolean isko() {
			if (moveBuffer[0][1]>=0&&moveBuffer[0][0]==moveBuffer[2][0] &&
					moveBuffer[0][1]==moveBuffer[2][1] &&
							moveBuffer[0][2]==moveBuffer[2][2]) {
				System.out.println("ko found "+Arrays.toString(moveBuffer[0]));
				return true;
			}
			return false;
		}
	}
	
	char getLine(int l) {
		char c=(char)((int)'a'+l);
		if (c>='i') c++;
		return c;
	}
	String getcol(int c) {
		return ""+(c+1);
	}
	
	int getStone(int li, int co) {
		return goban.P.color(li, goban.getboardsize()-1-co);
	}
	
	void tolatexSmall() {
		System.out.println("nmoves "+nlocmv);
		goban.allback();
		String s="\\gobansize{"+goban.getboardsize()+"}\n";

		int debli=buf.moveBuffer[0][1]-4;
		if (debli<0) debli=0;
		int finli=buf.moveBuffer[0][1]+4;
		if (finli>goban.getboardsize()-1) finli=goban.getboardsize()-1;
		int debco=buf.moveBuffer[0][2]-4;
		if (debco<0) debco=0;
		int finco=buf.moveBuffer[0][2]+4;
		if (finco>goban.getboardsize()-1) finco=goban.getboardsize()-1;
		ArrayList<String> white = new ArrayList<String>();
		ArrayList<String> black = new ArrayList<String>();
		for (int li=debli;li<=finli;li++) {
			for (int co=debco;co<=finco;co++) {
				int st = getStone(li, co);
				if (st==-1) {
					char cli=getLine(li);
					String cco=getcol(co);
					white.add(""+cli+cco);
				} else if (st==1) {
					char cli=getLine(li);
					String cco=getcol(co);
					black.add(""+cli+cco);
				}
			}
		}
		String ss=white.toString().replace('[', ' ').replace(']', ' ').trim();
		s+="\\white{"+ss+"}";
		ss=black.toString().replace('[', ' ').replace(']', ' ').trim();
		s+="\\black{"+ss+"}";
		
		System.out.println(s);

		Wait.waitUser();
		
		System.out.println("\\showgoban");
	}

	void tolatexBig() {
		System.out.println("nmoves "+nlocmv);
		String s="\\gobansize{"+goban.getboardsize()+"}\n";

		int debli=0;
		int finli=goban.getboardsize()-1;
		int debco=0;
		int finco=goban.getboardsize()-1;
		ArrayList<String> white = new ArrayList<String>();
		ArrayList<String> black = new ArrayList<String>();
		for (int li=debli;li<=finli;li++) {
			for (int co=debco;co<=finco;co++) {
				int st = getStone(li, co);
				if (st==-1) {
					char cli=getLine(li);
					String cco=getcol(co);
					white.add(""+cli+cco);
				} else if (st==1) {
					char cli=getLine(li);
					String cco=getcol(co);
					black.add(""+cli+cco);
				}
			}
		}
		String ss=white.toString().replace('[', ' ').replace(']', ' ').replaceAll(" ", "");
		s+="\\white{"+ss+"}\n";
		ss=black.toString().replace('[', ' ').replace(']', ' ').replaceAll(" ", "");
		s+="\\black{"+ss+"}\n";
		
		{
			int li=buf.moveBuffer[0][1];
			int co=goban.getboardsize()-buf.moveBuffer[0][2]-1;
			char cli=getLine(li);
			String cco=getcol(co);
			s+="\\gobansymbol{"+cli+cco+"}{K}\n";
		}
		
		System.out.println(s);

		Wait.waitUser();
		
		System.out.println("\\showgoban");
	}

	void tolatexFull() {
		System.out.println("nmoves "+nlocmv);
		goban.allback();
		String s="\\gobansize{"+goban.getboardsize()+"}\n";

		String handicap="";
		for (int li=0;li<goban.getboardsize();li++) {
			for (int co	=0;co<goban.getboardsize();co++) {
				if (getStone(li, co)!=0) {
					char cli=getLine(li);
					String cco=getcol(co);
					handicap+=""+cli+cco+",";
				}
			}
		}
		if (handicap.length()>0) {
			handicap=handicap.substring(0,handicap.length()-1);
			s+="\\black{"+handicap+"}\n";
		}
		
		if (handicap.length()>0) s+="\\white[1]{";
		else s+="\\black[1]{";
		int[] res = goban.showNextMove();
		if (res!=null) {
			int li=res[1];
			int co=res[2];
			char cli=getLine(li);
			String cco=getcol(co);
			s+=""+cli+cco;
		}
		for (int i=0;i<nmv;i++) {
			if (!goban.goforward()) break;
			res = goban.showNextMove();
			if (res!=null) {
				int li=res[1];
				int co=res[2];
				char cli=getLine(li);
				String cco=getcol(co);
				s+=","+cli+cco;
			}
		}
		System.out.println(s+"}");

		Wait.waitUser();
		
		System.out.println("\\showgoban");
	}

	public static void main(String args[]) throws Exception {
		countKos();
	}
	
	public static void countKos() {
		IsKo m = new IsKo();
		SgfLoad.parseDGSarchive(m);
	}
	
	public int getnko() {return nko;}
	public int getnmv() {return nmv;}
	
	int nmv=0,nko=0,nlocmv=0;
	Board goban;
	MoveBuffer buf;
	GoFrame bi;
	@Override
	public void parse(final String sgf) {
		bi = new GoFrame("toto");
		goban = new Board(19, bi);
		BufferedReader f = new BufferedReader(new InputStreamReader(new StringBufferInputStream(sgf)));
		try {
			nlocmv=0;
			goban.load(f);
			buf = new MoveBuffer();
			if (goban.goforward()) {
				int[] mv = goban.showNextMove();
				if (mv!=null) buf.addmove(mv);
				if (goban.goforward()) {
					mv = goban.showNextMove();
					if (mv!=null) buf.addmove(mv);
				}
			}
			while (goban.goforward()) {
				int[] mv = goban.showNextMove();
				if (mv!=null) {
					buf.addmove(mv);
					if (buf.isko()) {
//						System.out.println("sgf "+sgf);
						System.out.println("goban loaded "+goban.getboardsize());
						tolatexBig();
						nko++;
					}
					nlocmv++;
					nmv++;
				}
			}
			f.close();
		} catch (IOException e) {e.printStackTrace();}
	}
}
