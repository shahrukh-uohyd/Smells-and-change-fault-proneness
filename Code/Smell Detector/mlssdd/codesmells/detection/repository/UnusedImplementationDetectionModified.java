

package mlssdd.codesmells.detection.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.lang.*;
import java.util.*;

public class UnusedImplementationDetectionModified extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {

		 XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		try {
			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();
			final int cLength = cList.getLength();

			final Map<String, MLSCodeSmell> resultMap = new HashMap<>();

			//modification
			//C files
		    XPathExpression cn = xpath.compile("//unit[@language='C'] | //unit[@language='C++']");
		    NodeList Cfiles = (NodeList) cn.evaluate(xml, XPathConstants.NODESET);

		    //list of native methods with static linking
		    final Set<String> cFunctions = new HashSet<>();

		    //list of registered native methods (dynamic linking)
		     List<String> nativeRegisteredList = new ArrayList<String>();


		    for (int c=0; c<Cfiles.getLength();c++)
		    {
		      //dynamic linking of native methods.
		      XPathExpression x = xpath.compile("descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/expr/block |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/expr/call |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/macro |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/argument_list/argument");
		      NodeList registerBlocks = (NodeList) x.evaluate(Cfiles.item(c), XPathConstants.NODESET);

		      for(int r=0; r<registerBlocks.getLength(); r++)
		      {
		        XPathExpression m = xpath.compile("(descendant::expr/literal)[1] | (descendant::expr/block/expr[1]/literal) | argument_list/argument");
		        NodeList methods = (NodeList) m.evaluate(registerBlocks.item(r), XPathConstants.NODESET);
		        for( int met=0; met<methods.getLength(); met++)
		        {
		          //nativeImplemented+=1;
		          nativeRegisteredList.add(methods.item(met).getTextContent().replace("\"",""));
		          //System.out.println(methods.item(met).getTextContent());  
		        }
		        
		      }

		      //static linking of native methods
		    	XPathExpression func = xpath.compile("descendant::function/name");
		    	NodeList functions = (NodeList) func.evaluate(Cfiles.item(c), XPathConstants.NODESET);

		    	for(int fun=0; fun<functions.getLength(); fun++)
		    	{
		    		cFunctions.add(functions.item(fun).getTextContent());
		        //System.out.println(functions.item(fun).getTextContent());
		    	}

		      //if the implementation is done by macros eg.-java-smt-master
		      //extract the definition of the macro whose value contains JNIEXPORT
		      //eg- <cpp:define>#<cpp:directive>define</cpp:directive>
		      // <cpp:macro><name>DEFINE_FUNC</name>
		      //<parameter_list>(<parameter><type><name>jreturn</name></type></parameter>, 
		      //<parameter><type><name>func_escaped</name></type></parameter>)</parameter_list>
		      //</cpp:macro> \
		      //<cpp:value>JNIEXPORT j##jreturn JNICALL 
		      //Java_org_sosy_1lab_java_1smt_solvers_mathsat5_Mathsat5NativeApi_msat_##func_escaped</cpp:value></cpp:define>
		      
		      

		      XPathExpression xp = xpath.compile("descendant::*[name()='cpp:define'][contains(./*[name()='cpp:value'], 'JNICALL')]");
		      NodeList macros = (NodeList) xp.evaluate(Cfiles.item(c), XPathConstants.NODESET);
		      for(int mc=0; mc<macros.getLength();mc++)
		      {
		        //name of the macro
		        XPathExpression xp2 = xpath.compile("descendant::*[name()='cpp:macro']/name");
		        String macroName = xp2.evaluate(macros.item(mc));
		        System.out.println(macroName);

		        //extract the name of first part of the name of the implemeted method which will be inside 
		        // cpp:value tag splitted by space starts at 3rd index then again splitted by # 
		        XPathExpression fn1 = xpath.compile("descendant::*[name()='cpp:value']");
		        String funcPart1 = fn1.evaluate(macros.item(mc));
		        funcPart1 = funcPart1.split(" ")[3].split("#")[0];

		        //get the arguments of the each macroName macro that will be the second part of the implemented method name
		        //XPathExpression n2 = xpath.compile("//name[text() = 'DEFINE_FUNC']/following-sibling::argument_list[1]/argument[2]");
		        String n2 = String.format("//name[text()='%s']/following-sibling::argument_list[1]/argument",macroName);
		        NodeList defineFuncs = (NodeList) xpath.evaluate(n2, xml, XPathConstants.NODESET);
		        
		        for(int df=0; df<defineFuncs.getLength(); df++)
		        {
		          
		          String funcPart2 = defineFuncs.item(df).getTextContent();
		          cFunctions.add(funcPart1+funcPart2);
		          //System.out.println(funcPart1+defineFuncs.item(df).getTextContent());
		        }


		      }



		    }



			for (int i1 = 0; i1 < javaLength; i1++) {
				final Node javaFile = javaList.item(i1);
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaFile);
					System.out.println(javaFilePath);
				// final NodeList declList =
				// 	(NodeList) AbstractCodeSmellDetection.NATIVE_DECL_EXP
				// 		.evaluate(javaFile, XPathConstants.NODESET);
				XPathExpression e5 = xpath.compile("descendant::function_decl[type/specifier='native']");
      			final NodeList nodes = (NodeList) e5.evaluate(javaFile, XPathConstants.NODESET);
				final int declLength = nodes.getLength();
				//System.out.println(declLength);
				methodloop:
				for (int a = 0; a <nodes.getLength() ; a++) {
					boolean implemented= false;
					boolean called = false;

					XPathExpression name1 = xpath.compile("name");
        			final String thisNativeMethod = name1.evaluate(nodes.item(a));
					// final Node thisDecl = declList.item(j);
					// final String thisNativeMethod = thisDecl.getTextContent();
					// System.out.println(thisNativeMethod);
					
					//class
		        	XPathExpression cn1 = xpath.compile("(ancestor::class)[last()]");
		        	NodeList thisClass = (NodeList) cn1.evaluate(nodes.item(a), XPathConstants.NODESET);
		        	//System.out.println(thisClass.item(0).getTextContent());

		        	//class name
		        	XPathExpression cn2 = xpath.compile("name/name | name");
		        	String className = cn2.evaluate(thisClass.item(0));
		          	className = className.split("<")[0];
		        	System.out.println(className);

		        	//fullname
		        	String fullName;
		          String thisNativeMethodModified=thisNativeMethod.replace("_","_1");
		          String classNameModified = className.replace("_","_1");
		          
		          fullName = classNameModified+"_"+thisNativeMethodModified;
		          System.out.println(fullName);
		          	//Iterator name = cFunctions.iterator();
		          	//while(name.hasNext())
		          	for (String name : cFunctions)
		          	{
		              if(name.contains(fullName) | nativeRegisteredList.contains(thisNativeMethod))
		              {
		                implemented=true;
		              }
		            }

		            if(implemented)
		            {
		            	int NumberOfCallsToThisMethod =0;
				        // //class
				        // XPathExpression cn11 = xpath.compile("(ancestor::class)[last()]");
				        // NodeList thisClass = (NodeList) cn11.evaluate(nodes.item(a), XPathConstants.NODESET);
				        //System.out.println("class: "+thisClass.item(0).getTextContent());
				        if(thisClass.item(0)!=null){
				        //class name
				        XPathExpression cn3 = xpath.compile("name/name | name");
				        String className1 = cn3.evaluate(thisClass.item(0));
				        System.out.println("class: "+className1);

				        //interface name which this class implements.
				        XPathExpression in = xpath.compile("descendant::super_list/implements/super/name");
				        String interfaceName = in.evaluate(thisClass.item(0));
				        //System.out.println("interface: "+interfaceName);

				        //parent class which this class inherits.
				        XPathExpression pc = xpath.compile("descendant::super_list/extends/super/name");
				        String parentName = pc.evaluate(thisClass.item(0));
				        //System.out.println("parent: "+parentName);

				        //child classes.
				        final String cc1 = String.format("descendant::class[super_list/extends/super/name='%s']",className1);
				        final NodeList childClasses = (NodeList) xpath.evaluate(cc1, xml, XPathConstants.NODESET);
				        // System.out.println("child classes: ");

				        //java files which imports this method
				        //import name className.methodName
				        String importName = className1+"."+thisNativeMethod;

				        //extracting files which import this method.
				        final String imp = String.format("//unit[import[contains(., '%s')]]", importName);
				        final NodeList importFiles = (NodeList) xpath.evaluate(imp, xml, XPathConstants.NODESET);
				        


				        String[] children = new String[childClasses.getLength()];
				        for(int child = 0; child<childClasses.getLength(); child++)
				        {
				          //child class name
				            XPathExpression childName = xpath.compile("name/name | name");
				            String name11 = childName.evaluate(childClasses.item(child));
				            System.out.println("child class: "+name11);
				            children[child] = name11;
				        }
				        
				        

				        //final String thisNativeMethod = nodes.item(a).getTextContent();
				        System.out.println("non-Native method: "+thisNativeMethod);
				        
				    
				        // //count of number of parameters in the native function
				        // final String NoOfParams = String.format("count(descendant::parameter_list[1]/parameter)", thisNativeMethod);
				        // final Number NumberOfParameters = (Number)xpath.evaluate(NoOfParams, nodes.item(a), XPathConstants.NUMBER);
				        // //System.out.println("Number of parameter: "+NumberOfParameters.intValue());


				        //list of parameter types for a particular native function
				        final String params = String.format("descendant::parameter_list/parameter/decl/type/name", thisNativeMethod);
				        final NodeList parameter_list = (NodeList) xpath.evaluate(params, nodes.item(a), XPathConstants.NODESET);
				      
				        System.out.println("Parameters type: ");
				        for (int j=0; j<parameter_list.getLength();j++)
				        {
				          System.out.println((j+1)+" "+parameter_list.item(j).getTextContent());
				        }

				        //list of calls to a particular native function
				        //final String callQuery = String.format("descendant::call[name='%s'] | descendant:: call[name/name='this' and name/name='%s']",thisNativeMethod, thisNativeMethod);
				        

				        //modified to not match this keyword because we are not matching the caller now.
				        final String callQuery = String.format("descendant::call[name='%s'] | descendant:: call[name/name='this' and name/name='%s']",thisNativeMethod, thisNativeMethod);
				        final NodeList calls= (NodeList) xpath.evaluate(callQuery, thisClass.item(0), XPathConstants.NODESET);
				        //System.out.println(calls.getLength());

				        final String callQuery1 = String.format("descendant::call[name/name='%s']",thisNativeMethod);
				        final NodeList calls1= (NodeList) xpath.evaluate(callQuery1, xml, XPathConstants.NODESET);


				        //calls in the same class without any object or class i.e, class.call()
				        for(int i=0; i<calls.getLength();i++)
				        {
				          System.out.println("call: "+calls.item(i).getTextContent());


				          //ancestor functions.
				          XPathExpression xp = xpath.compile("ancestor::function");
				          NodeList AncestorFunctions = (NodeList) xp.evaluate(calls.item(i), XPathConstants.NODESET);
				        
				        //arguments in the call
				         // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list[1]/argument)",thisNativeMethod, i+1);
				         //    final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET); 
				         
				          final String callQuery2 = String.format("descendant::argument_list[1]/argument");
				            final NodeList callList = (NodeList) xpath.evaluate(callQuery2, calls.item(i), XPathConstants.NODESET); 
				         

				         //  //System.out.println("Number of arguments: "+callList.getLength());

				          if(parameter_list.getLength()==callList.getLength())
				          {
				            //list of argument in a particular call.
				            // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list/argument)",thisNativeMethod, i+1);
				            // final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET);
				            //System.out.println(callList.getLength());
				            int equal=1;
				            for (int j=0; j<callList.getLength();j++)
				            { 
				              //System.out.println(callList.item(j).getTextContent());
				          

				              //type of the argument passed.
				              //final String arg = String.format("(descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::decl_stmt/decl[name='%s']/type/name | descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::parameter_list/parameter/decl[name='%s']/type/name)[last()] | descendant::call[name='%s']/argument_list/argument/expr[literal='%s']/literal/@type", thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(),thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(), thisNativeMethod, callList.item(j).getTextContent());
				              // final String arg = String.format("(descendant::call[name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name)[1] | (descendant::call[name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type | "
				              //     +"(descendant::call[name/name='this' and name/name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name | "
				              //     +"descendant::call[name/name='this' and name/name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name/name='this' and name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type", 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent());
				              
				              String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList.item(j).getTextContent());
				              argType = xpath.evaluate(xp2, calls.item(i));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList.item(j).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList.item(j).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass.item(0));
				                }
				              }


				              //String test = String.format("(descendant::decl_stmt/decl[name='%s']/type/name)", callList.item(j).getTextContent());

				              //final String argType =  xpath.evaluate(test, thisClass.item(0));
				  
				                  argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];

				                 System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(j).getTextContent().equals("int") || parameter_list.item(j).getTextContent().equals("float") || parameter_list.item(j).getTextContent().equals("double") || parameter_list.item(j).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(j).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(j).getTextContent().equals("int"))))
				                  continue;
				                 if(!argType.equalsIgnoreCase(parameter_list.item(j).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          called = true;
				          continue methodloop;
				        }
				      }
				  }

				  //calls in the importing files.
				  for(int iF = 0; iF<importFiles.getLength(); iF++)
				  {
				      //list of calls to the native method in the file which import this native method
				    final String callQuery3 = String.format("descendant::call[name='%s']", thisNativeMethod);
				    final NodeList importingCalls= (NodeList) xpath.evaluate(callQuery3, importFiles.item(iF), XPathConstants.NODESET);
				    
				    for(int ic=0; ic<importingCalls.getLength(); ic++)
				    {
				      System.out.println("call: "+importingCalls.item(ic).getTextContent());


				          //ancestor functions.
				          XPathExpression xp = xpath.compile("ancestor::function");
				          NodeList AncestorFunctions = (NodeList) xp.evaluate(importingCalls.item(ic), XPathConstants.NODESET);
				        
				        //arguments in the call
				         // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list[1]/argument)",thisNativeMethod, i+1);
				         //    final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET); 
				         
				          final String callQuery2 = String.format("descendant::argument_list[1]/argument");
				            final NodeList callList = (NodeList) xpath.evaluate(callQuery2, importingCalls.item(ic), XPathConstants.NODESET); 
				         

				         //  //System.out.println("Number of arguments: "+callList.getLength());

				          if(parameter_list.getLength()==callList.getLength())
				          {
				            //list of argument in a particular call.
				            // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list/argument)",thisNativeMethod, i+1);
				            // final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET);
				            //System.out.println(callList.getLength());
				            int equal=1;
				            for (int j=0; j<callList.getLength();j++)
				            { 
				              //System.out.println(callList.item(j).getTextContent());
				          

				              //type of the argument passed.
				              //final String arg = String.format("(descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::decl_stmt/decl[name='%s']/type/name | descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::parameter_list/parameter/decl[name='%s']/type/name)[last()] | descendant::call[name='%s']/argument_list/argument/expr[literal='%s']/literal/@type", thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(),thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(), thisNativeMethod, callList.item(j).getTextContent());
				              // final String arg = String.format("(descendant::call[name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name)[1] | (descendant::call[name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type | "
				              //     +"(descendant::call[name/name='this' and name/name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name | "
				              //     +"descendant::call[name/name='this' and name/name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name/name='this' and name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type", 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
				              //     thisNativeMethod, i+1, callList.item(j).getTextContent());
				              
				              String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList.item(j).getTextContent());
				              argType = xpath.evaluate(xp2, importingCalls.item(ic));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList.item(j).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList.item(j).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass.item(0));
				                }
				              }


				              //String test = String.format("(descendant::decl_stmt/decl[name='%s']/type/name)", callList.item(j).getTextContent());

				              //final String argType =  xpath.evaluate(test, thisClass.item(0));
				  
				                  argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];

				                 System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(j).getTextContent().equals("int") || parameter_list.item(j).getTextContent().equals("float") || parameter_list.item(j).getTextContent().equals("double") || parameter_list.item(j).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(j).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(j).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(j).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          called = true;
				          continue methodloop;
				          
				        }
				      }
				    }
				  }



				//call in the child classes without class or object.
				for(int cc=0; cc<childClasses.getLength(); cc++)
				{
				  final String callQuery2 = String.format("descendant::call[name='%s'] | descendant::call[name/name='super' and name/name='%s']",thisNativeMethod, thisNativeMethod);

				  // //modified to not match super keyword.
				  // final String callQuery2 = String.format("descendant::call[name='%s'] | descendant::call[name/name='%s']",thisNativeMethod, thisNativeMethod);
				  final NodeList calls2= (NodeList) xpath.evaluate(callQuery2, childClasses.item(cc), XPathConstants.NODESET);
				//System.out.println(calls2.getLength());
				  for(int e=0; e<calls2.getLength();e++)
				        {
				          System.out.println("call: "+calls2.item(e).getTextContent());
				        


				          //ancestor functions.
				          XPathExpression xp = xpath.compile("ancestor::function");
				          NodeList AncestorFunctions = (NodeList) xp.evaluate(calls2.item(e), XPathConstants.NODESET);

				          //class in which this call happend.
				        XPathExpression cn7 = xpath.compile("(ancestor::class)");
				        NodeList thisClass2 = (NodeList) cn7.evaluate(calls2.item(e), XPathConstants.NODESET);
				        //System.out.println("class: "+thisClass1.item(0).getTextContent());

				        XPathExpression cn8 = xpath.compile("name/name | name");
				        String className3="";
				        if(thisClass2.item(0)!=null)
				        className3 = cn8.evaluate(thisClass2.item(0));


				      //arguments in the call
				            final String callQuery3 = String.format("descendant::argument_list[1]/argument");
				            final NodeList callList3 = (NodeList) xpath.evaluate(callQuery3, calls2.item(e), XPathConstants.NODESET); 
				          //System.out.println("Number of arguments: "+callList3.getLength());

				          if(parameter_list.getLength()==callList3.getLength())
				          {
				            //list of argument in a particular call.
				            // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list/argument)",thisNativeMethod, i+1);
				            // final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET);
				            //System.out.println(callList.getLength());
				            int equal=1;
				            for (int l=0; l<callList3.getLength();l++)
				            { 
				              //System.out.println(callList3.item(l).getTextContent());
				          
				              String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList3.item(l).getTextContent());
				              argType = xpath.evaluate(xp2, calls2.item(e));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList3.item(l).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList3.item(l).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass2.item(0));
				                }
				              }
				              



				              //type of the argument passed.
				              // final String arg2 = String.format("(descendant::call[name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name | descendant::call[name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type | "
				              //     +"(descendant::call[name/name='super' and name/name='%s'][%s]//"+
				              //     "preceding::decl_stmt/decl[name='%s']/type/name | "
				              //     +"descendant::call[name/name='super' and name/name='%s'][%s]//"+
				              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //     "descendant::call[name/name='super' and name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type", 
				              //     thisNativeMethod, e+1, callList3.item(l).getTextContent(),
				              //     thisNativeMethod, e+1, callList3.item(l).getTextContent(), 
				              //     thisNativeMethod, e+1, callList3.item(l).getTextContent(),
				              //     thisNativeMethod, e, callList3.item(l).getTextContent(),
				              //     thisNativeMethod, e, callList3.item(l).getTextContent(), 
				              //     thisNativeMethod, e, callList3.item(l).getTextContent());
				              // final String argType2 =  xpath.evaluate(arg2, childClasses.item(cc));
				  
				               argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];

				                 System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(l).getTextContent().equals("int") || parameter_list.item(l).getTextContent().equals("float") || parameter_list.item(l).getTextContent().equals("double") || parameter_list.item(l).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(l).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(l).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(l).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          called = true;
				          continue methodloop;
				          
				        }
				      }
				  }
				}











				//calls in other class with object or class i.e, class.call() or object.call
				for (int x=0; x<calls1.getLength(); x++)
				        {


				          System.out.println("call: "+calls1.item(x).getTextContent());
				          String callName = calls1.item(x).getTextContent();
				          String[] tokens = callName.split("\\.");
				          String callerName;
				          if(!tokens[0].equals("this"))
				            callerName=tokens[0];
				          else callerName = tokens[1];
				          

				        //class in which this call happend.
				        XPathExpression cn22 = xpath.compile("(ancestor::class)");
				        NodeList thisClass1 = (NodeList) cn22.evaluate(calls1.item(x), XPathConstants.NODESET);
				        //System.out.println("class: "+thisClass1.item(0).getTextContent());


				        
				        XPathExpression cn5 = xpath.compile("name/name | name");
				        String className2="";
				        if(thisClass1.item(0)!=null) 
				        {       
				        className2 = cn5.evaluate(thisClass1.item(0));
				         System.out.println("calling class: "+className2);

				         //otherClasses.add(filename+"_"+className2);

				        //ancestor functions.
				          XPathExpression xp = xpath.compile("ancestor::function");
				          NodeList AncestorFunctions = (NodeList) xp.evaluate(calls1.item(x), XPathConstants.NODESET);
				        
				          String callerType="";

				              
				              //check whether this callerName is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callerName);
				                  callerType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!callerType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(callerType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callerName);
				                  if(thisClass1.item(0)!=null)
				                  callerType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				          
				        System.out.println(callerType);

				          //System.out.println(!(callerType=="") && callerType.equals(interfaceName));
				          //if callerName is classname or callertype is either class type or interface type then proceed.

				        

				        
				          if(callerName.equals(className1) || callerType.equals(className1) || (callerType!="" && callerType.equals(interfaceName)))
				          {  
				            
				         

				         //final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				         final String callQuery3 = String.format("descendant:: argument_list[1]/argument");
				            final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, calls1.item(x), XPathConstants.NODESET); 
				          //System.out.println("Number of arguments: "+NumberOfArguments1.intValue());
				           


				            //System.out.println(NumberOfParameters.intValue()+" "+callList1.getLength());
				          if(parameter_list.getLength()==callList1.getLength())
				          {
				            //list of argument in a particular call.
				            // final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				            // final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, doc, XPathConstants.NODESET);
				            //System.out.println(callList1.getLength());
				            int equal=1;
				            for (int c1=0; c1<callList1.getLength();c1++)
				            { 
				              //System.out.println(callList1.item(c1).getTextContent());
				          

				               String argType="";

				              //check whether this argument is passed dirctly as literal.
				              // String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList1.item(c1).getTextContent());
				              // System.out.println("Evaluating XPath: " + xp2);
				              // argType = xpath.evaluate(xp2, calls1.item(x));

				               String value = callList1.item(c1).getTextContent();
								String safeLiteral;

								if (value.contains("'") && value.contains("\"")) {
								    safeLiteral = escapeForXPathUsingConcat(value);
								} else if (value.contains("'")) {
								    safeLiteral = "\"" + value + "\"";
								} else {
								    safeLiteral = "'" + value + "'";
								}

								String xp2 = "descendant::argument_list/argument/expr[literal=" + safeLiteral + "]/literal/@type";
								argType = xpath.evaluate(xp2, calls1.item(x));


				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  //String xp3 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                	String xp3 = "descendant::decl[name=" +safeLiteral + "]/type/name";
				               
				                  argType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				              }






				              
				               argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];


				               System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(c1).getTextContent().equals("int") || parameter_list.item(c1).getTextContent().equals("float") || parameter_list.item(c1).getTextContent().equals("double") || parameter_list.item(c1).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(c1).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(c1).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(c1).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          called = true;
				          continue methodloop;
				          
				        }
				      }
				  }

				else
				{
				    for(int cn11 = 0; cn11< children.length ; cn11++)
				    {
				        if(callerType.equals(children[cn11]))
				        {
				            System.out.println("matched child");

				            //count the number of argement passed.
				         

				         //final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				         final String callQuery3 = String.format("descendant:: argument_list[1]/argument");
				            final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, calls1.item(x), XPathConstants.NODESET); 
				          //System.out.println("Number of arguments: "+NumberOfArguments1.intValue());
				           


				            System.out.println(parameter_list.getLength()+" "+callList1.getLength());
				          if(parameter_list.getLength()==callList1.getLength())
				          {
				            //int temp=0;
				            //list of argument in a particular call.
				            // final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				            // final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, doc, XPathConstants.NODESET);
				            //System.out.println(callList1.getLength());
				            int equal=1;
				            for (int c1=0; c1<callList1.getLength();c1++)
				            { 
				              //System.out.println(callList1.item(c1).getTextContent());
				          

				              //type of the argument passed.
				              String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList1.item(c1).getTextContent());
				              argType = xpath.evaluate(xp2, calls1.item(x));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				              }







				              // final String arg1 = String.format("(descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[name='%s']"+
				              //   "//preceding::decl_stmt/decl[name='%s']/type/name | "+
				              //   "descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[name='%s']"+
				              //   "//preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //   "descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type",thisNativeMethod, x+1, callList1.item(c1).getTextContent(), callList1.item(c1).getTextContent(), thisNativeMethod, x+1, callList1.item(c1).getTextContent(), callList1.item(c1).getTextContent(), thisNativeMethod, x+1, callList1.item(c1).getTextContent());

				              //String argType1 =  xpath.evaluate(arg1, thisClass1.item(0));
				  
				                argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];
				               System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(c1).getTextContent().equals("int") || parameter_list.item(c1).getTextContent().equals("float") || parameter_list.item(c1).getTextContent().equals("double") || parameter_list.item(c1).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(c1).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(c1).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(c1).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          called = true;
				          continue methodloop;
				          
				        }
				      }
				  }
				        }
				    }

				}

				}
				          

				if(!called)
				{
					System.out.println("implemented and unused: "+thisNativeMethod);
				 	final String thisClass1 = className;
						final String thisPackage =
							AbstractCodeSmellDetection.PACKAGE_EXP
								.evaluate(nodes.item(a));
						resultMap
							.put(
								thisNativeMethod,
								new MLSCodeSmell(
									this.getCodeSmellName(),
									"",
									thisNativeMethod,
									thisClass1,
									thisPackage,
									javaFilePath));
				}
				

				  System.out.println("Number of calls to "+thisNativeMethod+": "+ NumberOfCallsToThisMethod);
				System.out.println("--------------------------------------------");
				  

				  // if(NumberOfCallsToThisMethod>5)
				  // {
				  //       ExcessiveInterLanguageCommunication = true;
				  //   }
				  
				 }
				 // TotalNumberOfCallsToNativeMethods+=NumberOfCallsToThisMethod;
				 // TotalNumberOfNativeMethods+=1;
		            }



		          	// if(!implemented)
		          	// {
		          	// 	final String thisClass1 = className;
					// 	final String thisPackage =
					// 		AbstractCodeSmellDetection.PACKAGE_EXP
					// 			.evaluate(nodes.item(a));
					// 	resultMap
					// 		.put(
					// 			thisNativeMethod,
					// 			new MLSCodeSmell(
					// 				this.getCodeSmellName(),
					// 				"",
					// 				thisNativeMethod,
					// 				thisClass1,
					// 				thisPackage,
					// 				javaFilePath));
		          	// }
					
				}
			}
			
			this.setSetOfSmells(new HashSet<>(resultMap.values()));
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public static String escapeForXPathUsingConcat(String value) {
    StringBuilder sb = new StringBuilder("concat(");
    boolean first = true;
    for (int i = 0; i < value.length(); i++) {
        String part = String.valueOf(value.charAt(i));
        if (part.equals("'")) {
            part = "\"'\""; // represents a single quote
        } else if (part.equals("\"")) {
            part = "'\"'"; // represents a double quote
        } else {
            part = "'" + part + "'";
        }
        if (!first) sb.append(", ");
        sb.append(part);
        first = false;
    }
    sb.append(")");
    return sb.toString();
}


}
