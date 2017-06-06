package com.ensoftcorp.open.java.commons.strings.conversion;

import java.io.IOException;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;

import dk.brics.string.intermediate.Statement;

public class AtlasHotspotInfo {

	private Node node;
	private Statement statement;
	private FormattedSourceCorrespondence fsc;
	
	public AtlasHotspotInfo(Node node, Statement statement){
		this.node = node;
		this.fsc = FormattedSourceCorrespondence.getSourceCorrespondent(node);
		this.statement = statement;
	}
	
	public Node getNode() {
		return node;
	}

	public Statement getStatement() {
		return statement;
	}

	public String getSourcefile() {
		try {
			return fsc.getRelativeFile();
		} catch (Exception e){
			return fsc.getFile().getAbsolutePath();
		}
	}

	public String getClassName() {
		return CommonQueries.getQualifiedTypeName(Common.toQ(node).parent().eval().nodes().one());
	}

	public String getMethodName() {
		return CommonQueries.getQualifiedFunctionName(node);
	}

	public Integer getLineNumber() {
		try {
			return (int) fsc.getStartLineNumber();
		} catch (IOException e) {
			return -1;
		}
	}
	
}