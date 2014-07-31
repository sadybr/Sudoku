import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

public class Sudoku {
	
	private enum Rule{
		R1("Unique at group"),
		R2("Unique to cell"),
		R3("Unique at line"),
		R4("Random")
		;
		
		private String value;
		private Rule(String value) {
			this.value = value;
		}
		public String toString() {
			return value;
		}
	}
	
	
	private class Shot {
		int v1[][];
		String v2;
	}
	
	private class Cell {
		int value;
		List<Integer> possibilits;
		int line;
		int column;

		public Cell(int value) {
			this.value = value;
			this.possibilits = new ArrayList<Integer>();
		}
	}
	
	private List<Shot> progress;
	private Cell values[][];

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sudoku s = new Sudoku();
		s.print();
		s.solve(0);
		s.printProgress(4);
		if(s.solved()) {
			System.out.print("OK SOLVED");
		} else {
			System.out.print("NOT SOLVED");
		}
		System.out.println(" (Numbers were placed " + s.progress.size()+ ")");
		
	}
	
	private void convert(int[][] v) {
		this.values = new Cell[9][9];
		
		for (short line = 0; line < 9; line ++) {
			for (short column = 0; column < 9; column ++) {
				this.values[line][column] = new Cell(v[line][column]);
			}
		}
	}
	
/*	private void printLast() {
		Shot v = this.progress.get(this.progress.size()-1);
		for (short line = 0; line < 9; line ++) {
			for (short column = 0; column < 9; column ++) {
				if (column != 0 && column % 3 == 0) {
					System.out.print("  ");
				}
				System.out.print(v.v1[line][column] + " ");
			}
			if (line % 3 == 2) {
				System.out.println();
			}
			System.out.println();
		}
	}*/
	private void print() {
		for (short line = 0; line < 9; line ++) {
			for (short column = 0; column < 9; column ++) {
				if (column != 0 && column % 3 == 0) {
					System.out.print("  ");
				}
				System.out.print(values[line][column].value + " ");
			}
			if (line % 3 == 2) {
				System.out.println();
			}
			System.out.println();
		}
	}

	private boolean canSet(int number, int line, int column) {

		if (values[line][column].value != 0) {
			return false;
		}

		for (short x=0; x < 9; x++) {
			if (values[line][x].value == number) {
				return false;
			}
			if (values[x][column].value == number) {
				return false;
			}
		}
		
		int blockLine   = (line/3)*3;
		int blockColumn = (column/3)*3;
		
		for (int lin = 0; lin < 3; lin++) {
			for (int col = 0; col < 3; col++) {
				if (values[lin+blockLine][col+blockColumn].value == number) {
					return false;
				}
			}
		}

		return true;
	}
	
	private List<Cell> possibilitsToSetAtGroup(int number, int line, int column) {
		int blockLine   = (line/3)*3;
		int blockColumn = (column/3)*3;

		List<Cell> list = new ArrayList<Sudoku.Cell>();
		for (int lin = 0; lin < 3; lin++) {
			for (int col = 0; col < 3; col++) {
				if (values[lin+blockLine][col+blockColumn].possibilits.contains(number)) {
					list.add(values[lin+blockLine][col+blockColumn]);
				}
			}
		}

		return list;
	}
	
	private void solve(int index) {
		if (this.progress == null) {
			progress = new ArrayList<Sudoku.Shot>();
			calculatePossibilits();
		}
		
		boolean modified = false;
		do {
			modified = false;
			modified |= uniqueValueAtCell();
			modified |= uniqueValueAtGroup();
			modified |= uniqueValueAtLine();
		} while(modified);
		
		if (!solved()) {
			for (int i=1; i<=9; i++) {
				int last = this.progress.size();
				boolean ok = this.randomShot(i);
				if (ok) {

				//	System.out.println(StringTools.lPad("", index, ' ') + this.progress.size() + "(" + i + ") {");
					solve(index + 1); 
				//	System.out.println(StringTools.lPad("", index, ' ') + this.progress.size() + "(" + i + ") }");

					if (solved()) {
						return;
					}
//					System.out.println("Last: " + last);
//					System.out.println("Size: " + this.progress.size());
					while (last != this.progress.size()) {
						this.progress.remove(this.progress.size() - 1);
					}
//					System.out.println("Size after clear: " + this.progress.size());
					for (int lin = 0; lin < 9; lin++) {
						for (int col = 0; col < 9; col++) {
							this.values[lin][col].value = this.progress.get(last -1).v1[lin][col];
						}
					}

				}
			}
		}
	}
	

	private boolean solved() {
		if (this.progress.size() == 0) {
			return false;
		}
		Shot v = this.progress.get(this.progress.size()-1);
		for (int lin = 0; lin < 9; lin++) {
			for (int col = 0; col < 9; col++) {
				if (v.v1[lin][col] == 0) {
					return false;
				}
			}
		}

		return true;
	}
	
	private boolean randomShot(int value) {
		for (int column = 0; column < 9; column++) {
			for (int line = 0; line < 9; line++) {
				if (this.canSet(value, line, column)) {
					this.set(value, line, column, Rule.R4);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean set(int number, int line, int column, Rule rule) {
		if (canSet(number, line, column)) {
			values[line][column].value = number;
			calculatePossibilits();
			setProgress(number, line, column, rule);
			return true;
		}
		return false;
	}

	private void calculatePossibilits() {
		for (int line = 0; line < 9; line++) {
			for (int column = 0; column < 9; column++) {
				values[line][column].line = line;
				values[line][column].column = column;
				values[line][column].possibilits.clear();
				for (int x = 1; x <= 9; x++) {
					if (canSet(x, line, column)) {
						values[line][column].possibilits.add(x);
					}
				}
			}
		}
	}
	
	private void printProgress(int size) {
		for(int point = 0; point < progress.size(); point = point + size) {
			for(int point2 = point; point2 < point + size; point2++) {
				if (point2 >= progress.size()) {
					break;
				}
				System.out.print(rPad("  " + progress.get(point2).v2, 25, ' ') + "|");
			}
			System.out.println();
			
			for (short line = 0; line < 9; line ++) {
				for(int point2 = point; point2 < point + size; point2++) {
					if (point2 >= progress.size()) {
						break;
					}

					for (short column = 0; column < 9; column ++) {
						if (column % 3 == 0) {
							System.out.print("  ");
						}
//						System.out.print(progress.get(point2).v1[line][column] + " ");
						System.out.print((progress.get(point2).v1[line][column] == 0 ? " " : progress.get(point2).v1[line][column]) + " ");
					}
					
					System.out.print(" |");
				}
				if (line % 3 == 2) {
					System.out.println();
				}
				
				System.out.println();
			}
			for(int x=0; x<size;x++) {
				System.out.print("--------------------------");
			}
			System.out.println();
		//	break;
		}
	}
	
	private boolean uniqueValueAtCell() {
		for (int line = 0; line < 9; line++) {
			for (int column = 0; column < 9; column++) {
			
				if (values[line][column].possibilits.size() == 1) {
					set(values[line][column].possibilits.get(0), line, column, Rule.R2);
					return true;
				}
			}
		}

		return false;
	}
	
	private boolean uniqueValueAtGroup() {
		boolean modified = false;
		for (int line = 0; line < 9; line+=3) {
			for (int column = 0; column < 9; column+=3) {
				for (int x=1; x <= 9; x++) {
					List<Cell> pos = possibilitsToSetAtGroup(x, line, column); 
					if( pos.size() == 1) {
						modified = true;
						if(!set(x, pos.get(0).line, pos.get(0).column, Rule.R1)) {
							throw new RuntimeException("Cant set: " + x + " at " + (line+1) + ":" + (1+column));
						}
					}
				}
				
			}
		}
		return modified;
	}
	
	private boolean uniqueValueAtLine() {
		boolean modified = false;
		for (int x=1; x <= 9; x++) {
			// Varrendo linhas
			for (int line = 0; line < 9; line++) {
				int count = 0;
				int lastCol = 0;
				for (int column = 0; column < 9; column++) {
					if (canSet(x, line, column)) {
						count++;
						lastCol = column;
					}
				}
				if (count == 1) {
					set(x, line, lastCol, Rule.R3);
					modified = true;
				}
			}
			// varrendo colunas
			for (int column = 0; column < 9; column++) {
				int count = 0;
				int lastLine = 0;
					for (int line = 0; line < 9; line++) {
					if (canSet(x, line, column)) {
						count++;
						lastLine = line;
					}
				}
				if (count == 1) {
					set(x, lastLine, column, Rule.R3);
					modified = true;
				}
			}
		}
		return modified;
	}
	
	private void setProgress(int number, int line, int column, Rule rule) {
		Shot v = new Shot();
		v.v1 = new int[9][9];
		for (int lin = 0; lin < 9; lin++) {
			for (int col = 0; col < 9; col++) {
				v.v1[lin][col] = values[lin][col].value;
			}
		}
		v.v2 = (1+line) + ":" + (1+column) + "=" + values[line][column].value + " " + rule;
		progress.add(v);
	}

	private Sudoku() {
//		convert(new int[][]{{6,5,8, 7,9,0, 2,1,4}, //(SUPER EASY)
//							 {4,3,0, 8,1,2, 5,9,0},
//							 {2,9,1, 6,5,4, 3,8,7},
//							   
//							 {1,2,6, 5,4,9, 8,7,3},
//							 {5,0,4, 1,3,7, 6,2,9},
//							 {3,7,8, 2,0,8, 1,4,0},
//							 
//							 {9,6,3, 4,2,0, 7,5,8},
//							 {0,1,5, 9,7,6, 4,3,2},
//							 {7,4,2, 3,8,5, 9,6,0}
//			               });
/*		convert(new int[][]{{0,5,0, 7,9,0, 2,1,4}, //(EASY)
							 {0,3,0, 0,1,0, 0,9,0},
							 {2,0,0, 0,0,4, 0,0,7},
							   
							 {0,2,6, 5,0,9, 0,0,0},
							 {5,0,0, 0,3,0, 0,0,9},
							 {0,0,0, 2,0,8, 1,4,0},
							 
							 {9,0,0, 4,0,0, 0,0,8},
							 {0,1,0, 0,7,0, 0,3,0},
							 {7,4,2, 0,8,5, 0,6,0}
                            });*/
//		values = new int[][]{{0,0,0, 0,8,0, 0,0,0}, //(MEDIUM)
//							 {6,0,4, 0,0,0, 3,0,8},
//							 {0,0,3, 6,0,5, 9,0,0},
//							   
//							 {4,0,0, 0,1,0, 0,0,5},
//							 {5,0,0, 9,0,8, 0,0,1},
//							 {2,0,0, 0,3,0, 0,0,6},
//							 
//							 {0,0,2, 7,0,9, 4,0,0},
//							 {8,0,7, 0,0,0, 6,0,2},
//							 {0,0,0, 0,6,0, 0,0,0}
//                            };
/*
		convert( new int[][]{{6,0,0, 4,0,0, 0,0,0},// HARD
							 {0,3,7, 0,0,0, 5,0,1},
							 {0,0,0, 0,7,0, 9,0,0},
							                      
							 {0,0,0, 3,0,0, 0,5,0},
							 {0,8,9, 0,6,0, 3,1,0},
							 {0,6,0, 0,0,7, 0,0,0},
							                      
							 {0,0,6, 0,8,0, 0,0,0},
							 {1,0,5, 0,0,0, 4,3,0},
							 {0,0,0, 0,0,4, 0,0,9}
					       }); */
	/*	convert( new int[][]{{0,3,0, 8,6,0, 0,0,9},// EXTREME
							 {0,0,0, 0,7,9, 2,0,0},
							 {0,4,0, 3,0,0, 0,0,0},
							                      
							 {2,0,0, 0,8,0, 7,0,0},
							 {0,1,0, 0,0,0, 0,5,0},
							 {0,0,8, 0,5,0, 0,0,1},
							                      
							 {0,0,0, 0,0,7, 0,9,0},
							 {0,0,6, 5,9,0, 0,0,0},
							 {9,0,0, 0,3,6, 0,4,0}
					       });
*/
		
		int[][] tmp = new int[9][9];
		for (short line = 0; line < 9; line ++) {
			for (short column = 0; column < 9; column ++) {
				try {
					tmp[line][column] = Integer.parseInt(JOptionPane.showInputDialog(line+1 + ":" + (column+1)));
				} catch (Exception e) {
					tmp[line][column] = 0;
				}
			}
		}
		this.convert(tmp);
	}
	
	private String rPad(String input, int size, char conpletationChar) {
        String str = (input == null ? "" : input);
        int strLength = str.length();
        if (strLength < size) {
            char[] chars = new char[size];
            Arrays.fill(chars, strLength, size, conpletationChar);
            if (strLength > 0) {
                str.getChars(0, strLength, chars, 0);
            }
            return new String(chars);
        }
        return str;
    }
}



