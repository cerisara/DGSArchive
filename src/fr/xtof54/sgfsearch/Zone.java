package fr.xtof54.sgfsearch;

/**
 * Definit une zone du goban
 * Il y a 3 types de zone: coin, cote et centre

 * 
 * @author xtof
 *
 */
public class Zone {
	enum typet {coin, cote, centre};
	enum coint {no,ne,se,so};
	
	typet type;
	int ncols=4, nlig=4;
	int[][] z;
	
	public void getZoneCoin(Board bd, coint loc) {
		z = new int[ncols][nlig];
		switch(loc) {
		case no:
			for (int i=0;i<ncols;i++)
				for (int j=0;j<nlig;j++) {
					z[i][j]=bd.P.color(i, j);
				}
			break;
		case ne:
			for (int i=0;i<ncols;i++)
				for (int j=0;j<nlig;j++) {
					z[i][j]=bd.P.color(bd.getboardsize()-i, j);
				}
			break;
		case se:
			for (int i=0;i<ncols;i++)
				for (int j=0;j<nlig;j++) {
					z[i][j]=bd.P.color(bd.getboardsize()-i, bd.getboardsize()-j);
				}
			break;
		case so:
			for (int i=0;i<ncols;i++)
				for (int j=0;j<nlig;j++) {
					z[i][j]=bd.P.color(i, bd.getboardsize()-j);
				}
			break;
		}
	}
	
	public int getNstones() {
		int s=0;
		for (int i=0;i<ncols;i++)
			for (int j=0;j<nlig;j++) s+=Math.abs(z[i][j]);
		return s;
	}
	
	@Override
	public int hashCode() {
		int s=0;
		for (int i=0;i<ncols;i++)
			for (int j=0;j<nlig;j++) s+=z[i][j];
		return s;
	}
	@Override
	public boolean equals(Object o) {
		Zone zo = (Zone)o;
		if (type!=zo.type) return false;
		int swapped=1;
		for (int i=0;i<ncols;i++)
			for (int j=0;j<nlig;j++) {
				if (swapped*z[i][j]!=zo.z[i][j]) {
					if (z[i][j]==0||zo.z[i][j]==0) return false;
					if (swapped<0) return false;
					swapped=-1;
				}
			}
		return true;
	}

}
