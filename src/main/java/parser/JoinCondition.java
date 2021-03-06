package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang.NotImplementedException;

public class JoinCondition {
	private static Logger log = Logger.getLogger(JoinCondition.class.toString());
	private String[] left;
	private String[] right;
	
	private String operator;
	public String tableR;
	public String tableL;
	public String joinAttribute;
	List<String> tables;
	private boolean isSimple = false;
	
			
	public JoinCondition(SqlBasicCall call) {
		
		operator = call.getOperator().toString();
		SqlNode[] operands = call.operands;
		tables= new ArrayList<>();
		if (operands[0] instanceof SqlIdentifier && operands[1] instanceof SqlIdentifier) {
			SqlIdentifier op1 = (SqlIdentifier) operands[0];
			SqlIdentifier op2 = (SqlIdentifier) operands[1];
			if (op1.isSimple() && op2.isSimple()) {
				left = new String[] { op1.getSimple() };
				right = new String[] { op2.getSimple() };
				joinAttribute = op1.getSimple();
				isSimple = true;
			} else {
				left = new String[] { op1.names.get(0), op1.names.get(1) };
				right = new String[] { op2.names.get(0), op2.names.get(1) };
				tableL = op1.names.get(0);
				tableR = op2.names.get(0);
				tables.add(op1.names.get(0));
				tables.add(op2.names.get(0));
				joinAttribute = op1.names.get(1);
			}
		}
		if (!isSimple) {
			if (!left[1].equals(right[1])) {
				throw new NotImplementedException("Maybe try joining on the same attribute?");
			}
		}
	}

	
	public void reform(){
		//always keep tableR & L
		tables =  new ArrayList<>();
		tables.add(getLeft()[0]);
		tables.add(getRight()[0]);
		tableL = getLeft()[0];
		tableR = getRight()[0];
	}
	
	public boolean hasConnection(JoinCondition neighbor) {
		if (this.left[0].equals(neighbor.left[0]) && this.left[1].equals(neighbor.left[1])
				|| this.left[0].equals(neighbor.right[0]) && this.left[1].equals(neighbor.right[1])) {
			return true;
		}
		if (this.right[0].equals(neighbor.left[0]) && this.right[1].equals(neighbor.left[1])
				|| this.right[0].equals(neighbor.right[0]) && this.right[1].equals(neighbor.right[1])) {
			return true;
		}

		return false;
	}


	public static String findCycleImproved(List<JoinCondition> graph, SqlQueryMeta query) {
		if (query.getFromTables().size() == 2) {
			if (graph.size() >= 1) {
				// so if we have 2 tables and actually the graph is bigger than
				// one we will definitely have a joining attribute.
				return graph.get(0).left[1]; // pick any since left[1] ==
												// right[1]
			}
		} else {
			int count = 1; // I already have a connections //TODO probably zero?
			String directJoin = "";
			for (int i = 0; i < graph.size(); i++) {
				JoinCondition node = graph.get(i);
				for (int j = i; j < graph.size(); j++) {
					JoinCondition checkNode = graph.get(j);
					if (node != checkNode) {
						if (node.hasConnection(checkNode)) {
							count++;
							directJoin = node.left[1];
						}
					}
				}
				if (count == query.getFromTables().size() - 1) {
					return directJoin;
				}
			}
			log.info("count is " + count);
		}
		return "";
	}
	public String[] getLeft() {
		return left;
	}

	public void setLeft(String[] left) {
		this.left = left;
	}

	public String[] getRight() {
		return right;
	}

	public void setRight(String[] right) {
		this.right = right;
	}
	
	public static List<String> tablesIntertwined(JoinCondition a, JoinCondition b){
		List<String> intertwined =  new ArrayList();
		for(String tbl : a.tables){
			if(b.tables.contains(tbl)) intertwined.add(tbl);
		}
		return intertwined;
	}
	@Override
	public String toString(){
		
		if(left.length==2 && right.length==2){
			return new String(left[0]+ "."+left[1] + " " +operator + " " + right[0]+ "."+right[1] + " ");
		}
		 return new String(left[0] + operator + right[0] + " ");
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(left);
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + Arrays.hashCode(right);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JoinCondition other = (JoinCondition) obj;
		if (!Arrays.equals(left, other.left))
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		if (!Arrays.equals(right, other.right))
			return false;
		return true;
	}

	
	
	
	
}
