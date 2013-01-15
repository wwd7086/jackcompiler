import java.util.*;

class SymbolContent
{
	String name;
	String type;
	String kind;
	int index;
	
	SymbolContent(String name, String type, String kind, int index)
	{
		this.name=name;
		this.type=type;
		this.kind=kind;
		this.index=index;
	}
}

public class SymbolTable 
{
	HashMap<String,SymbolContent> subMap=new HashMap<String, SymbolContent>();
	HashMap<String,SymbolContent> classMap=new HashMap<String, SymbolContent>();
	SymbolContent curContent;
	int stCount=0;
	int fiCount=0;
	int arCount=0;
	int vaCount=0;
	
	void startSubroutine()
	{
		subMap.clear();
		arCount=0;
		vaCount=0;
	}
	
	void Define(String name, String type, String kind)
	{
		if(kind.equals("static")||kind.equals("field"))
		{
			classMap.put(name, new SymbolContent(name,type,kind,VarCount(kind)));
		}
		else if(kind.equals("arg")||kind.equals("var"))
		{
			subMap.put(name, new SymbolContent(name,type,kind,VarCount(kind)));
		}
	}
	
	int VarCount(String kind)
	{
		switch(kind)
		{
		case "static": return stCount++;
		case "field":  return fiCount++;
		case "arg":    return arCount++;
		case "var":    return vaCount++;
		default:       return 0;
		}
	}
	
	int showCount(String kind)
	{
		switch(kind)
		{
		case "static": return stCount;
		case "field":  return fiCount;
		case "arg":    return arCount;
		case "var":    return vaCount;
		default:       return 0;
		}
	}
	
	String kindOf(String name)
	{
		if(subMap.containsKey(name))
		{
			curContent=subMap.get(name);
			return curContent.kind;
		}
		else if(classMap.containsKey(name))
		{
			curContent=classMap.get(name);
			return curContent.kind;
		}
		else
			return "!!notfind!!";
	}
	
	String TypeOf(String name)
	{
		if(subMap.containsKey(name))
		{
			curContent=subMap.get(name);
			return curContent.type;
		}
		else if(classMap.containsKey(name))
		{
			curContent=classMap.get(name);
			return curContent.type;
		}
		else
			return "!!notfind!!";
	}
	
	int IndexOf(String name)
	{
		if(subMap.containsKey(name))
		{
			curContent=subMap.get(name);
			return curContent.index;
		}
		else if(classMap.containsKey(name))
		{
			curContent=classMap.get(name);
			return curContent.index;
		}
		else
			return 0;
	}
	
	String contains(String name)
	{
		if(subMap.containsKey(name)&&classMap.containsKey(name))
			return "s+c";
		else if(subMap.containsKey(name)&& !classMap.containsKey(name))
			return "s";
		else if(!subMap.containsKey(name)&&classMap.containsKey(name))
			return "c";
		else 
			return "!!notfind!!";
	}
	
	/////////////////////test///////////////////
	public static void main(String[] Args)
	{
		SymbolTable test=new SymbolTable();
		test.Define("a", "int", "var");
	}
}


