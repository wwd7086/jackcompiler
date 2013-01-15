import java.io.*;
import java.util.*;
public class CompilationEngine 
{
	File input;
	File xmlOut;
	File vmOut;
	
	JackTokenizer jtk;
	WriteXml wxm;
	VMWriter vmw;
	
	String className=""; //used in the declare of class
	
	String funKind=""; //used in the declare of sub
	String funName="";
	
	int whileCount=0; //used in while //the number help to distinguish the labels needed by while
	LinkedList<Integer> WhileCount=new LinkedList<Integer>(); //recursive in statements
	int ifCount=0;    //used in if //the number help to distinguish the labels needed by if
	LinkedList<Integer> IfCount=new LinkedList<Integer>();   //recursive in statements
	
	String letBuf=""; //used in let
	boolean letArray = false; //whether is array
	
	String callFun="";  //used in Do //name of the function being called 
	String callBuf="";
	
	LinkedList<String> termFun=new LinkedList<String>();  //used in Expression(sub call)  //recursive---xxx.xxx(another sub call,xxxxx)
	String termBuf="";
	LinkedList<String> TermBuf=new LinkedList<String>();
	
	int nArgs=0;  //used in Expression list //count the number of args //recursive---xxx.xxx(another sub call,xxxxx)
	LinkedList<Integer> NArgs=new LinkedList<Integer>();
	
	LinkedList<String> op=new LinkedList<String>(); //used in Expressions  //recursive--term op (term op term)
	                                                //used in term  //recursive--op (op term)
	
	SymbolTable table=new SymbolTable(); //used to define arg var field static
	String type="";
	String kind="";
	
	

	CompilationEngine(File input, File xmlOut, File vmOut)
	{
		this.input=input;
		this.xmlOut=xmlOut;
		this.vmOut=vmOut;
		jtk=new JackTokenizer(input);
		wxm=new WriteXml(xmlOut);
		vmw=new VMWriter(vmOut);
	}
	
	//unfinished
	void CompileClass()
	{
		wxm.WriteStart("class");
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("class"))
				wxm.WriteString("keyword", jtk.keyWord());
			else
				return;
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
			{
				wxm.WriteString("identifier", jtk.identifier());
				className=jtk.identifier(); //record classname
			}
			else
				return;
		}
		//class body
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("{"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		//classvardec*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("static")||jtk.keyWord().equals("field"))
				CompileClassVarDec();
			else
			{
				jtk.goback();
				break;
			}
		}
		//subdec*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("constructor")||jtk.keyWord().equals("function")||jtk.keyWord().equals("method"))
				CompileSubroutineDec();
			else
			{
				jtk.goback();
				break;
			}
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("}"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		wxm.WriteEnd("class");
	}
	
	//done
	void CompileClassVarDec()
	{
		wxm.WriteStart("classVarDec");
		//(field | static) type name
		kind=jtk.keyWord(); //record kind
		wxm.WriteString("keyword", jtk.keyWord());
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
			{
				type=jtk.keyWord(); //record field type
				wxm.WriteString("keyword", jtk.keyWord());
			}
			else if(jtk.tokenType().equals("IDENTIFIER"))
			{
				type=jtk.identifier(); //record field type
				wxm.WriteString("identifier", jtk.identifier());
			}
			else
				return;
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
			{
				table.Define(jtk.identifier(), type, kind);//define
				wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
			}
			else
				return;
		}
		//(,name)*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(","))
				wxm.WriteString("symbol", jtk.symbol());
			else
			{
				jtk.goback();
				break;
			}
			
			if(jtk.hasMoreTokens())
			{
				jtk.advance();
				if(jtk.tokenType().equals("IDENTIFIER"))
				{
					table.Define(jtk.identifier(), type, kind); //define
					wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
				}
				else
					return;
			}
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(";"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		wxm.WriteEnd("classVarDec");
	}

	//unfinished
	void CompileSubroutineDec()
	{
		wxm.WriteStart("subroutineDec");
		
		//function kind
		wxm.WriteString("keyword", jtk.keyWord());   
		funKind=jtk.keyWord(); //record kind
		
		//clean the table
		table.startSubroutine();
		//clean the if and while count
		ifCount=0;
		whileCount=0;
		
		//do something i forgot !!!!!!!!   define a null argument for the method because the arg 0 should be this( i hate debug)
		if(funKind.equals("method"))
			table.Define("this", className, "arg");
		
		//return type
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("void")||jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
				wxm.WriteString("keyword", jtk.keyWord());
			else if(jtk.tokenType().equals("IDENTIFIER"))
				wxm.WriteString("identifier", jtk.identifier());
			else
				return;
		}
		
		//function name
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
			{
				wxm.WriteString("identifier", jtk.identifier());
				funName=jtk.identifier(); //record name
			}
			else
				return;
		}
		
		//function parameters
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("("))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		CompileParameterlist();
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(")"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//function body
		CompileSubroutineBody();
		
		wxm.WriteEnd("subroutineDec");
	}
	
	//need check //done //get the args of the function
	void CompileParameterlist()
	{
		wxm.WriteStart("parameterList");
		//(..........)?
		if(jtk.hasMoreTokens())
		{
			//type xxx
			jtk.advance();
			if(jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
			{
				type=jtk.keyWord(); //record arg type and kind
				kind="arg";
				wxm.WriteString("keyword", jtk.keyWord());
				
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.tokenType().equals("IDENTIFIER"))
					{
						table.Define(jtk.identifier(), type, kind);   //define arg
						wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
					}
					else
						return;
				}
				//(, type name)*
				while(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals(","))
						wxm.WriteString("symbol", jtk.symbol());
					else
					{
						jtk.goback();
						break;
					}
					if(jtk.hasMoreTokens())
					{
						jtk.advance();
						if(jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
						{
							type=jtk.keyWord();  //record arg type and kind
							kind="arg";
							wxm.WriteString("keyword", jtk.keyWord());
						}
						else if(jtk.tokenType().equals("IDENTIFIER"))
						{
							type=jtk.identifier();  //record arg type and kind
							kind="arg";
							wxm.WriteString("identifier", jtk.identifier());
						}
						else
							return;
					}
					if(jtk.hasMoreTokens())
					{
						jtk.advance();
						if(jtk.tokenType().equals("IDENTIFIER"))
						{
							table.Define(jtk.identifier(), type, kind);  //define arg
							wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
						}
						else
							return;
					}
				}
			}
			//type(class) xxxxx
			else if(jtk.tokenType().equals("IDENTIFIER"))
			{
				type=jtk.identifier();   //record arg type and kind
				kind="arg";
				wxm.WriteString("identifier", jtk.identifier());
				
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.tokenType().equals("IDENTIFIER"))
					{
						table.Define(jtk.identifier(), type, kind);   //define arg
						wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
					}
					else
						return;
				}
				//(, type name)*
				while(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals(","))
						wxm.WriteString("symbol", jtk.symbol());
					else
					{
						jtk.goback();
						break;
					}
					if(jtk.hasMoreTokens())
					{
						jtk.advance();
						if(jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
						{
							type=jtk.keyWord();   //record arg type and kind
							kind="arg";
							wxm.WriteString("keyword", jtk.keyWord());
						}
						else if(jtk.tokenType().equals("IDENTIFIER"))
						{
							type=jtk.identifier();  //record arg type and kind
							kind="arg";
							wxm.WriteString("identifier", jtk.identifier());
						}
						else
							return;
					}
					if(jtk.hasMoreTokens())
					{
						jtk.advance();
						if(jtk.tokenType().equals("IDENTIFIER"))
						{
							table.Define(jtk.identifier(), type, kind);    //define arg
							wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
						}
						else
							return;
					}
				}
			}
			else
				jtk.goback();
		}
		
		wxm.WriteEnd("parameterList");
	}
	
	//need check //unfinished
	void CompileSubroutineBody()
	{
		wxm.WriteStart("subroutineBody");
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("{"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		//var*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("var"))
				CompileVarDec();
			else
			{
				jtk.goback();
				break;
			}
		}
		
		//write function 
		vmw.writeFunction(className+"."+funName, table.showCount("var"));
		//set this for method
		if(funKind.equals("method"))
		{
			vmw.writePush("argument", 0);
			vmw.writePop("pointer", 0);
		}
		//alloc memory for constructor
		else if(funKind.equals("constructor"))
		{
			vmw.writePush("constant", table.showCount("field"));
			vmw.writeCall("Memory.alloc", 1);
			vmw.writePop("pointer", 0);  //set this
		}
		//do nothing for function
		else if(funKind.equals("function"))
		{
			
		}
		
		//statements
		CompileStatements();
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("}"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		wxm.WriteEnd("subroutineBody");
	}
	
	//done  //get the vars of the function
	void CompileVarDec()
	{
		wxm.WriteStart("varDec");
		
		//var type name
		kind=jtk.keyWord(); //record var kind
		wxm.WriteString("keyword", jtk.keyWord());
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("int")||jtk.keyWord().equals("char")||jtk.keyWord().equals("boolean"))
			{
				type=jtk.keyWord(); //record var type
				wxm.WriteString("keyword", jtk.keyWord());
			}
			else if(jtk.tokenType().equals("IDENTIFIER"))
			{
				type=jtk.identifier();//record var type
				wxm.WriteString("identifier", jtk.identifier());
			}
			else
				return;
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
			{
				table.Define(jtk.identifier(), type, kind); //define var
				wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
			}
			else
				return;
		}
		
		//(,name)*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(","))
				wxm.WriteString("symbol", jtk.symbol());
			else
			{
				jtk.goback();
				break;
			}
			if(jtk.hasMoreTokens())
			{
				jtk.advance();
				if(jtk.tokenType().equals("IDENTIFIER"))
				{
					table.Define(jtk.identifier(), type, kind);//define var
					wxm.WriteString("identifier", jtk.identifier()+"(define)"+kind);
				}
				else
					return;
			}
		}
		//;
	    if(jtk.hasMoreTokens())
	    {
	    	jtk.advance();
	    	if(jtk.symbol().equals(";"))
	    		wxm.WriteString("symbol",jtk.symbol());
	    	else
	    		return;
	    }
		wxm.WriteEnd("varDec");
	}
	
	//need check  //done
	void CompileStatements()
	{
		wxm.WriteStart("statements");
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.keyWord().equals("let"))
				CompileLet();
			else if(jtk.keyWord().equals("if"))
				CompileIf();
			else if(jtk.keyWord().equals("while"))
				CompileWhile();
			else if(jtk.keyWord().equals("do"))
				CompileDo();
			else if(jtk.keyWord().equals("return"))
				CompileReturn();
			else
			{
				jtk.goback();
				break;
			}
		}
		wxm.WriteEnd("statements");
	}
	
	//done
	void CompileDo()
	{
		wxm.WriteStart("doStatement");
		wxm.WriteString("keyword", jtk.keyWord());
		
		//call function name
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
				callBuf=jtk.identifier();   //record callBuf
			else
				return;
		}
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("."))
			{
				//xxx.xxx(xxxx)
				wxm.WriteString("identifier", callBuf+"(use)"+table.TypeOf(callBuf)+table.kindOf(callBuf)+table.IndexOf(callBuf));
				wxm.WriteString("symbol", jtk.symbol());
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.tokenType().equals("IDENTIFIER"))
					{
						callFun=jtk.identifier(); //record
						
						//write to pass the invisible arg
						if(table.kindOf(callBuf).equals("static"))
							vmw.writePush("static", table.IndexOf(callBuf));
						else if(table.kindOf(callBuf).equals("field"))
							vmw.writePush("this", table.IndexOf(callBuf));
						else if(table.kindOf(callBuf).equals("arg"))
							vmw.writePush("argument", table.IndexOf(callBuf));
						else if(table.kindOf(callBuf).equals("var"))
							vmw.writePush("local", table.IndexOf(callBuf));
						//else 
							//call function no need to pass the arg
						
						wxm.WriteString("identifier", jtk.identifier());
					}
					else
						return;
				}
			}
			else
			{
				//xxx(xxx)
				callFun=callBuf; //record
				
				//pass the invisible arg
				vmw.writePush("pointer", 0);
				
				wxm.WriteString("identifier", callBuf);
				jtk.goback();
			}
		}
		
		//call function parameters
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("("))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		CompileExpressionList();
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(")"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(";"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//write the call
		if(callBuf.equals(callFun))
		{
			vmw.writeCall(className+"."+callBuf, nArgs+1);  //call the stuff in the current class
		}
		else
		{
			if(table.TypeOf(callBuf)!="!!notfind!!")
			{
				vmw.writeCall(table.TypeOf(callBuf)+"."+callFun, nArgs+1);  //call the method
			}
			else
			{
				vmw.writeCall(callBuf+"."+callFun, nArgs);  //call the function or constructor
			}
		}
		
		//return type must be void always ignore the 0
		vmw.writePop("temp", 0);
		
		wxm.WriteEnd("doStatement");
	}
	
	//done
	void CompileLet()
	{
		wxm.WriteStart("letStatement");
		wxm.WriteString("keyword", jtk.keyWord());
		//xxxx
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("IDENTIFIER"))
			{
				letBuf=jtk.identifier();
				wxm.WriteString("identifier", jtk.identifier()+"(use)"+table.TypeOf(jtk.identifier())+table.kindOf(jtk.identifier())+table.IndexOf(jtk.identifier()));
			}
			else
				return;
		}
		
		//([xxxx])?
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("["))
			{
				letArray=true;  //is array
				wxm.WriteString("symbol", jtk.symbol());
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
					   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
					   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
					{
						jtk.goback();
						CompileExpression();
						
						//make the entry of the array
						if(table.kindOf(letBuf).equals("static"))
							vmw.writePush("static", table.IndexOf(letBuf));
						else if(table.kindOf(letBuf).equals("field"))
							vmw.writePush("this", table.IndexOf(letBuf));
						else if(table.kindOf(letBuf).equals("arg"))
							vmw.writePush("argument", table.IndexOf(letBuf));
						else if(table.kindOf(letBuf).equals("var"))
							vmw.writePush("local", table.IndexOf(letBuf));
						
						vmw.writeArithmetic("add");
						vmw.writePop("temp", 1);
					}
					else
						return;
				}
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals("]"))
						wxm.WriteString("symbol", jtk.symbol());
					else
						return;
				}
			}
			else
			{
				letArray=false; //is not array
				jtk.goback();
			}
		}
		
		//=
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("="))
				wxm.WriteString("symbol", jtk.symbol());
			else
				jtk.goback();
		}
		
		//xxxx
		CompileExpression();
		
		//write pop
		if(letArray)
		{
			vmw.writePush("temp", 1);
			vmw.writePop("pointer",1);
			vmw.writePop("that", 0);
		}
		else
		{
			if(table.kindOf(letBuf).equals("static"))
				vmw.writePop("static", table.IndexOf(letBuf));
			else if(table.kindOf(letBuf).equals("field"))
				vmw.writePop("this", table.IndexOf(letBuf));
			else if(table.kindOf(letBuf).equals("arg"))
				vmw.writePop("argument", table.IndexOf(letBuf));
			else if(table.kindOf(letBuf).equals("var"))
				vmw.writePop("local", table.IndexOf(letBuf));
		}
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(";"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				jtk.goback();
		}
		wxm.WriteEnd("letStatement");
	}
	
	//done
	void CompileWhile()
	{
		wxm.WriteStart("whileStatement");
		wxm.WriteString("keyword", jtk.keyWord());
		
		//while(xxxx)
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("("))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//push the whileCount++
		WhileCount.push(whileCount++);
		//set the label for start
		vmw.writeLabel("while_start_"+WhileCount.peekFirst());
		CompileExpression();
		// not the expression result on the stack
		vmw.writeArithmetic("not");
		//if goto end
		vmw.writeIf("while_end_"+WhileCount.peekFirst());
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(")"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//{xxxxxxxx}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("{"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		CompileStatements();
		//go to start
		vmw.writeGoto("while_start_"+WhileCount.peekFirst());
		//label end
		vmw.writeLabel("while_end_"+WhileCount.peekFirst());
		//pop the whilecount
		WhileCount.pop();
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("}"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		wxm.WriteEnd("whileStatement");
	}
	
	//done
	void CompileReturn()
	{
		wxm.WriteStart("returnStatement");
		//return
		wxm.WriteString("keyword", jtk.keyWord());
		//expression?
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
			   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
			   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
			{
				jtk.goback();
				CompileExpression();
				//write return
				vmw.writeReturn();
			}
			else
			{
				jtk.goback();
				//write return
				vmw.writePush("constant", 0);
				vmw.writeReturn();
			}
		}
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(";"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		wxm.WriteEnd("returnStatement");
	}
	
	//done
	void CompileIf()
	{
		wxm.WriteStart("ifStatement");
		wxm.WriteString("keyword", jtk.keyWord());
		
		//if(xxxxxx)
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("("))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		IfCount.push(ifCount++); //push the ifCount++
		CompileExpression();
		vmw.writeArithmetic("not"); //not the expression
		vmw.writeIf("if_L1_"+IfCount.peekFirst()); //if goto L1
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals(")"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//{XXXXXXXX}
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("{"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		CompileStatements();
		
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("}"))
				wxm.WriteString("symbol", jtk.symbol());
			else
				return;
		}
		
		//else
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			//have else 
			if(jtk.keyWord().equals("else"))
			{
				vmw.writeGoto("if_L2_"+IfCount.peekFirst()); //goto l2
				vmw.writeLabel("if_L1_"+IfCount.peekFirst()); //label l1
				
				wxm.WriteString("keyword", jtk.keyWord());
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals("{"))
						wxm.WriteString("symbol", jtk.symbol());
					else
						return;
				}
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.keyWord().equals("let")||jtk.keyWord().equals("if")||jtk.keyWord().equals("while")||jtk.keyWord().equals("do")||jtk.keyWord().equals("return"))
					{
						jtk.goback();
						CompileStatements();
					}
					else
						return;
				}
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals("}"))
						wxm.WriteString("symbol", jtk.symbol());
					else
						return;
				}
				
				vmw.writeLabel("if_L2_"+IfCount.peekFirst()); //label l2
				IfCount.pop(); //pop
			}
			//have no else
			else
			{
				vmw.writeLabel("if_L1_"+IfCount.peekFirst()); //Label L1
				IfCount.pop(); //pop
				jtk.goback();
			}
			
			
		}
		
		wxm.WriteEnd("ifStatement");
	}
	
	//need check //unfinished
	void CompileExpression()
	{
		wxm.WriteStart("expression");
		//term
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
			   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
			   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
			{
				jtk.goback();
				CompileTerm();
			}
			else
				return;
		}
		//(op term)*
		while(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.symbol().equals("+")||jtk.symbol().equals("-")||jtk.symbol().equals("*")||jtk.symbol().equals("/")||jtk.symbol().equals("=")
					||jtk.symbol().equals("&")||jtk.symbol().equals("|")||jtk.symbol().equals("<")||jtk.symbol().equals(">"))
			{
				op.push(jtk.symbol());
				wxm.WriteString("symbol", jtk.symbol());
			}
			else
			{
				jtk.goback();
				break;
			}
			if(jtk.hasMoreTokens())
			{
				jtk.advance();
				if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
				   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
				   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
				{
					jtk.goback();
					CompileTerm();
				}
				else
					return;
			}
			
			
			switch(op.pop())
			{
			case "+": vmw.writeArithmetic("add"); break;
			case "-": vmw.writeArithmetic("sub"); break;
			case "&": vmw.writeArithmetic("and"); break;
			case "|": vmw.writeArithmetic("or"); break;
			case "<": vmw.writeArithmetic("lt"); break;
			case ">": vmw.writeArithmetic("gt"); break;
			case "=": vmw.writeArithmetic("eq"); break;
			case "*": vmw.writeCall("Math.multiply", 2); break;
			case "/": vmw.writeCall("Math.divide", 2); break;
			}
		}
		wxm.WriteEnd("expression");
	}
	
	//need check //unfinshed
	void CompileTerm()
	{
		wxm.WriteStart("term");
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			//int constant
			if(jtk.tokenType().equals("INT_CONST"))
			{
				vmw.writePush("constant", Integer.parseInt(jtk.intVal()));
				wxm.WriteString("integerConstant", jtk.intVal());
			}
			//string constant
			else if(jtk.tokenType().equals("STRING_CONST"))
			{
				//do something
				vmw.writePush("constant", jtk.stringVal().length());
				vmw.writeCall("String.new", 1);
				for(int i=0;i<jtk.stringVal().length();i++)
				{
					vmw.writePush("constant", jtk.stringVal().charAt(i));
					vmw.writeCall("String.appendChar", 2);
				}
				
				wxm.WriteString("stringConstant", jtk.stringVal());
			}
			//other constant
			else if(jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
			{
				if(jtk.keyWord().equals("null")||jtk.keyWord().equals("false"))
					vmw.writePush("constant", 0);
				else if(jtk.keyWord().equals("true"))
				{
					vmw.writePush("constant", 1);
					vmw.writeArithmetic("neg");
				}
				else if(jtk.keyWord().equals("this"))
					vmw.writePush("pointer", 0);
				
				wxm.WriteString("keyword", jtk.keyWord());
			}
			else if(jtk.tokenType().equals("IDENTIFIER"))
			{
				termBuf=jtk.identifier();
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					//varname[exp] 
					if(jtk.symbol().equals("["))
					{
						wxm.WriteString("identifier", termBuf+"(use)"+table.TypeOf(termBuf)+table.kindOf(termBuf)+table.IndexOf(termBuf));
						wxm.WriteString("symbol",jtk.symbol());
						
						TermBuf.push(termBuf);
						
						CompileExpression();
						
						termBuf=TermBuf.pop();
						
						if(table.kindOf(termBuf).equals("static"))
							vmw.writePush("static", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("field"))
							vmw.writePush("this", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("arg"))
							vmw.writePush("argument", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("var"))
							vmw.writePush("local", table.IndexOf(termBuf));
						
						vmw.writeArithmetic("add");
						vmw.writePop("pointer", 1);
						vmw.writePush("that", 0);
						
						if(jtk.hasMoreTokens())
						{
							jtk.advance();
							if(jtk.symbol().equals("]"))
								wxm.WriteString("symbol", jtk.symbol());
							else
								return;
						}
					}
					//subcall()
					else if(jtk.symbol().equals("("))
					{
						wxm.WriteString("identifier", termBuf);
						wxm.WriteString("symbol",jtk.symbol());
						
						//pass the invisible arg
						vmw.writePush("pointer", 0);
						termFun.push(termBuf);
						
						CompileExpressionList();  //!!!!
						if(jtk.hasMoreTokens())
						{
							jtk.advance();
							if(jtk.symbol().equals(")"))
								wxm.WriteString("symbol", jtk.symbol());
							else
								return;
						}
						
						//write the call
						vmw.writeCall(className+"."+termFun.pop(), nArgs+1);
					}
					//xx.subcall()
					else if(jtk.symbol().equals("."))
					{
						wxm.WriteString("identifier", termBuf+"(use)"+table.TypeOf(termBuf)+table.kindOf(termBuf)+table.IndexOf(termBuf));
						wxm.WriteString("symbol",jtk.symbol());
						
						//write to pass the invisible arg
						if(table.kindOf(termBuf).equals("static"))
							vmw.writePush("static", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("field"))
							vmw.writePush("this", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("arg"))
							vmw.writePush("argument", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("var"))
							vmw.writePush("local", table.IndexOf(termBuf));
						//else 
							//call function no need to pass the arg
						
						TermBuf.push(termBuf); //save the caller
						
						if(jtk.hasMoreTokens())
						{
							jtk.advance();
							if(jtk.tokenType().equals("IDENTIFIER"))
							{
								wxm.WriteString("identifier", jtk.identifier());
								termFun.push(jtk.identifier());   //save the fun name
							}
							else
								return;
						}
						if(jtk.hasMoreTokens())
						{
							jtk.advance();
							if(jtk.symbol().equals("("))
								wxm.WriteString("symbol", jtk.symbol());
							else
								return;
						}
		
						CompileExpressionList();   //!!!!
						
						if(jtk.hasMoreTokens())
						{
							jtk.advance();
							if(jtk.symbol().equals(")"))
								wxm.WriteString("symbol", jtk.symbol());
							else
								return;
						}
						
						//write the call
						termBuf=TermBuf.pop();
						if(table.TypeOf(termBuf)!="!!notfind!!")
							vmw.writeCall(table.TypeOf(termBuf)+"."+termFun.pop(), nArgs+1);
						else 
							vmw.writeCall(termBuf+"."+termFun.pop(),nArgs);
					}
					
					//var or arg  
					else
					{
						wxm.WriteString("identifier", termBuf+"(use)"+table.TypeOf(termBuf)+table.kindOf(termBuf)+table.IndexOf(termBuf));
						jtk.goback();
						
						if(table.kindOf(termBuf).equals("static"))
							vmw.writePush("static", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("field"))
							vmw.writePush("this", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("arg"))
							vmw.writePush("argument", table.IndexOf(termBuf));
						else if(table.kindOf(termBuf).equals("var"))
							vmw.writePush("local", table.IndexOf(termBuf));
					}
				}
			}
			//(expression)
			else if(jtk.symbol().equals("("))
			{
				wxm.WriteString("symbol", jtk.symbol());
				CompileExpression();
				if(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals(")"))
						wxm.WriteString("symbol", jtk.symbol());
					else
						return;
				}
			}
			//-~term
			else if(jtk.symbol().equals("-")||jtk.symbol().equals("~"))
			{
				op.push(jtk.symbol());
				wxm.WriteString("symbol", jtk.symbol());
				CompileTerm();
				
				if(op.pop().equals("-"))
					vmw.writeArithmetic("neg");
				else
					vmw.writeArithmetic("not");
			}
			else
				return;
		}
		wxm.WriteEnd("term");
	}
	
	//need check //unfinished
	void CompileExpressionList()
	{
		wxm.WriteStart("expressionList");
		nArgs=0;
		NArgs.push(nArgs);
		//expression
		if(jtk.hasMoreTokens())
		{
			jtk.advance();
			if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
			   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
			   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
			{
				nArgs=NArgs.pop();
				nArgs++;
				NArgs.push(nArgs);
				jtk.goback();
				
				CompileExpression();
				
				//(,expression)*
				while(jtk.hasMoreTokens())
				{
					jtk.advance();
					if(jtk.symbol().equals(","))
						wxm.WriteString("symbol", jtk.symbol());
					else
					{
						jtk.goback();
						break;
					}
						
					if(jtk.hasMoreTokens())
					{
						jtk.advance();
						if(jtk.tokenType().equals("INT_CONST")||jtk.tokenType().equals("STRING_CONST")||jtk.tokenType().equals("IDENTIFIER")
								   ||jtk.symbol().equals("(")||jtk.symbol().equals("~")||jtk.symbol().equals("-")
								   ||jtk.keyWord().equals("true")||jtk.keyWord().equals("false")||jtk.keyWord().equals("null")||jtk.keyWord().equals("this"))
						{
							nArgs=NArgs.pop();
							nArgs++;
							NArgs.push(nArgs);
							jtk.goback();

							CompileExpression();
						}
						else
							return;
					}
				}
			}
			else
				jtk.goback();
			
			//do something
		}
		//return the current nArgs
		nArgs=NArgs.pop();
		
		wxm.WriteEnd("expressionList");
	}
	
	void CompileComplete()
	{
		wxm.closeWrite();
		vmw.close();
	}
	
	///////////////////////////test//////////////////////////////////////////////
	public static void main(String[] Args)
	{
		/*File intest=new File("D:\\nand2tetris\\nand2tetris\\projects\\10\\sq","SquareGame.jack");
		File outtest=new File("D:\\nand2tetris\\nand2tetris\\projects\\10\\sq","SquareGame.xml");
		CompilationEngine ctest=new CompilationEngine(intest, outtest);
		ctest.CompileClass();
		ctest.CompileComplete();*/
	} 
}
