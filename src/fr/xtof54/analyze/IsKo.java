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
		int[] around;
		int[] played;
		int[] prison;
		
		public int getMoveLine() {return played[1];}
		public int getMoveCol() {return played[2];}
		
		public void addmove(int[] m, int[] neighb, int[] prisoners) {
			around=neighb;
			played=m;
			prison=prisoners;
		}
		public boolean isko() {
			if (prison[2]>prison[0]||prison[3]>prison[1]) {
				// may be a ko
				int opp = played[0]>0?-1:1;
				int nopps=0, nempty=0, nbords=0;
				for (int i=0;i<4;i++) {
					if (around[i]==opp) nopps++;
					else if (around[i]==0) nempty++;
					else if (around[i]==-2) nbords++;
				}
				if (nopps+nbords==3&&nempty==1) {
					return true;
				}
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
	
	String tolatex(int debli, int finli, int debco, int finco) {
		String s="\\cleargoban\n\\gobansize{"+goban.getboardsize()+"}\n";
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
			int li=buf.getMoveLine();
			int co=buf.getMoveCol();
			char cli=getLine(li);
			String cco=getcol(co);
			s+="\\gobansymbol{"+cli+cco+"}{K}\n";
		}
		s+="\\showgoban\n\\vspace{5mm}\n";
		return s;
	}
	
	void tolatexBig() {
		int debli=0;
		int finli=goban.getboardsize()-1;
		int debco=0;
		int finco=goban.getboardsize()-1;
		System.out.println(tolatex(debli, finli, debco, finco));
	}
	void tolatexSmall() {
		int debli=buf.getMoveLine()-4;
		if (debli<0) debli=0;
		int finli=buf.getMoveLine()+4;
		if (finli>goban.getboardsize()-1) finli=goban.getboardsize()-1;
		int debco=buf.getMoveCol()-4;
		if (debco<0) debco=0;
		int finco=buf.getMoveCol()+4;
		if (finco>goban.getboardsize()-1) finco=goban.getboardsize()-1;
		System.out.println(tolatex(debli, finli, debco, finco));
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
			if (goban.getboardsize()>19) return;
			buf = new MoveBuffer();
			// pass 2 premiers moves
			if (goban.goforward()) {
				int[] mv = goban.showNextMove();
				if (mv!=null) {
					int[] neighb = {mv[1]>0?goban.P.color(mv[1]-1, mv[2]):-2,
							mv[2]>0?goban.P.color(mv[1], mv[2]-1):-2,
									mv[1]>=0&&mv[1]<goban.getboardsize()-1?goban.P.color(mv[1]+1, mv[2]):-2,
											mv[2]>=0&&mv[2]<goban.getboardsize()-1?goban.P.color(mv[1], mv[2]+1):-2};
					mv[2]=goban.getboardsize()-mv[2]-1;
					buf.addmove(mv,neighb,prisoners);
					nlocmv++;
				}
				if (goban.goforward()) {
					mv = goban.showNextMove();
					if (mv!=null) {
						int[] neighb = {mv[1]>0?goban.P.color(mv[1]-1, mv[2]):-2,
								mv[2]>0?goban.P.color(mv[1], mv[2]-1):-2,
										mv[1]>=0&&mv[1]<goban.getboardsize()-1?goban.P.color(mv[1]+1, mv[2]):-2,
												mv[2]>=0&&mv[2]<goban.getboardsize()-1?goban.P.color(mv[1], mv[2]+1):-2};
						mv[2]=goban.getboardsize()-mv[2]-1;
						buf.addmove(mv,neighb,prisoners);
						nlocmv++;
					}
				}
			}
			for (;;) {
				int[] mv = goban.showNextMove();
				prisoners[0]=goban.Pb;
				prisoners[1]=goban.Pw;
				if (!goban.goforward()) break;
				prisoners[2]=goban.Pb;
				prisoners[3]=goban.Pw;
				if (mv!=null) {
					if (mv[1]>=goban.getboardsize() ||mv[2]>=goban.getboardsize()) {
						// BUG dans le fichier SGF
						break;
					}
					int[] neighb = {mv[1]>0?goban.P.color(mv[1]-1, mv[2]):-2,
							mv[2]>0?goban.P.color(mv[1], mv[2]-1):-2,
									mv[1]>=0&&mv[1]<goban.getboardsize()-1?goban.P.color(mv[1]+1, mv[2]):-2,
											mv[2]>=0&&mv[2]<goban.getboardsize()-1?goban.P.color(mv[1], mv[2]+1):-2};
					mv[2]=goban.getboardsize()-mv[2]-1;
					buf.addmove(mv,neighb,prisoners);
					if (buf.isko()) {
//						System.out.println(sgf);
//						System.out.println("prison "+Arrays.toString(prisoners));
//						tolatexBig();
						tolatexSmall();
						nko++;
						// pour le moment, je m'arrete au 1er ko dans une partie
						break;
					}
					nlocmv++;
					nmv++;
				}
			}
			f.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	private int[] prisoners = {0,0,0,0};
}
