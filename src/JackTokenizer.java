import java.io.*;
public class JackTokenizer 
{
	File input;
	FileReader freader;
	BufferedReader breader;
	StreamTokenizer stk;
	String token="";
	String type="";
	int stkresult;
	
	JackTokenizer(File input)
	{
		this.input=input;
		try 
		{
			freader=new FileReader(input);
			breader=new BufferedReader(freader);
			stk=new StreamTokenizer(breader);
		} 
		catch (FileNotFoundException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stk.eolIsSignificant(false);
		//symbol
		stk.ordinaryChar('+');
		stk.ordinaryChar('-');
		stk.ordinaryChar('~');
		stk.ordinaryChar('*');
		stk.ordinaryChar('/');
		stk.ordinaryChar('=');
		stk.ordinaryChar('<');
		stk.ordinaryChar('>');
		stk.ordinaryChar('(');
		stk.ordinaryChar(')');
		stk.ordinaryChar('{');
		stk.ordinaryChar('}');
		stk.ordinaryChar('[');
		stk.ordinaryChar(']');
		stk.ordinaryChar('.');
		stk.ordinaryChar(',');
		stk.ordinaryChar('&');
		stk.ordinaryChar('|');
		stk.ordinaryChar(';');
		//comment
		stk.slashSlashComments(true);
		stk.slashStarComments(true);
		//string const
		stk.quoteChar('"');
		//key word identifier
		stk.wordChars('A', 'Z');
		stk.wordChars('a', 'z');
		stk.wordChars('_', '_');	
	}
	
	Boolean hasMoreTokens()
	{
		try 
		{
			if(stk.nextToken()!=StreamTokenizer.TT_EOF)
			{
				stk.pushBack();
				return true;
			}
			else 
				return false;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	void advance()
	{
		try
		{
			stkresult=stk.nextToken();
			if(stkresult==StreamTokenizer.TT_NUMBER)
			{
				token=Integer.toString((int)stk.nval);
				type="INT_CONST";
			}
			else if(stkresult==StreamTokenizer.TT_WORD)
			{
				token=stk.sval;
				type="WORD";
			}
			else if(stkresult=='"')
			{
				token=stk.sval;
				type="STRING_CONST";
			}
			else
			{
				token=String.valueOf((char)stk.ttype);
				type="SYMBOL";
			}
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void goback()
	{
		stk.pushBack();
	}
	
	String tokenType()
	{
		if(type=="INT_CONST") return "INT_CONST";
		else if(type=="STRING_CONST") return "STRING_CONST";
		else if(type=="SYMBOL") return "SYMBOL";
		else if(type=="WORD")
		{
			if(token.equals("class")||token.equals("method")||token.equals("function")||token.equals("constructor")||token.equals("int")
			||token.equals("boolean")||token.equals("char")||token.equals("void")||token.equals("var")||token.equals("static")
			||token.equals("field")||token.equals("let")||token.equals("do")||token.equals("if")||token.equals("else")
			||token.equals("while")||token.equals("return")||token.equals("true")||token.equals("false")||token.equals("null")||token.equals("this"))
				return "KEYWORD";
			else
				return "IDENTIFIER";
		}
		else 
			return "!!Unknown!!";
	}
	
	String keyWord()
	{
		if(tokenType().equals("KEYWORD"))
			return token;
		else
			return "!!NotKeyword!!";
	}
	
	String symbol()
	{
		if(tokenType().equals("SYMBOL"))
			return token;
		else
			return "!!NotSymbol!!";
	}
	
	String identifier()
	{
		if(tokenType().equals("IDENTIFIER"))
			return token;
		else
			return "!!NotIdentifier!!";
	}
	
	String intVal()
	{
		if(tokenType().equals("INT_CONST"))
			return token;
		else
			return "!!NotInt!!";
	}
	
	String stringVal()
	{
		if(tokenType().equals("STRING_CONST"))
			return token;
		else
			return "!!NotString!!";
	}
	
	/////////////////////test////////////////////////////
	public static void main(String[] Args)
	{
		File test=new File("D:\\nand2tetris\\nand2tetris\\projects\\10\\els","Main.jack");
		File testout=new File("D:\\nand2tetris\\nand2tetris\\projects\\10\\els","MainT.xml");
		WriteXml wxml=new WriteXml(testout);
		JackTokenizer testt=new JackTokenizer(test);
		
		wxml.WriteStart("tokens");
		while(testt.hasMoreTokens())
		{
			testt.advance();
			switch(testt.tokenType())
			{
			case "KEYWORD": wxml.WriteString("keyword", testt.keyWord()); break;
			case "SYMBOL": wxml.WriteString("symbol", testt.symbol()); break;
			case "IDENTIFIER": wxml.WriteString("identifier", testt.identifier()); break;
			case "INT_CONST": wxml.WriteString("integerConstant", testt.intVal()); break;
			case "STRING_CONST": wxml.WriteString("stringConstant", testt.stringVal()); break;
			}
		}
		wxml.WriteEnd("tokens");
		wxml.closeWrite();
	}
}
