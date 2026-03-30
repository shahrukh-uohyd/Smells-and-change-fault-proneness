

package mlssdd.antipatterns.detection.repository;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.antipatterns.detection.AbstractAntiPatternDetection;
import mlssdd.antipatterns.detection.IAntiPatternDetection;
import mlssdd.kernel.impl.MLSAntiPattern;
import mlssdd.utils.PropertyGetter;


//for updation
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.sax.SAXSource;
import java.lang.*;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ExcessiveInterLanguageCommunicationDetectionModified
		extends AbstractAntiPatternDetection implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {
		/*
		 * UNTREATED CASE Calls in both ways: Java to C and C to Java
		 */

		 XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		final int minNbOfCallsToSameMethod = PropertyGetter
			.getIntProp(
				"ExcessiveInterLanguageCommunication.MinNbOfCallsToSameMethod",
				5);
		final int minNbOfCallsToNativeMethods = PropertyGetter
			.getIntProp(
				"ExcessiveInterLanguageCommunication.MinNbOfCallsToNativeMethods",
				20);

		final Set<MLSAntiPattern> antiPatternSet = new HashSet<>();
		final Set<MLSAntiPattern> allNativeCalls = new HashSet<>();
		// Key: (argument of a native function, Java method in which this function is called)
		// Value: anti-pattern
		final Map<AbstractMap.SimpleImmutableEntry<String, String>, MLSAntiPattern> variablesAsArguments =
			new HashMap<>();

		int nbOfNativeCalls;

		try {
			final XPathExpression CLASS_EXP = this.xPath
				.compile("ancestor::" + IAntiPatternDetection.CLASS_QUERY);
			final XPathExpression PACKAGE_EXP =
				this.xPath.compile(IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression FILEPATH_EXP =
				this.xPath.compile(IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);
			final XPathExpression JAVA_METHOD_EXP =
				this.xPath.compile("ancestor::function/name");

			final XPathExpression loopExpr =
				this.xPath.compile("ancestor::for | ancestor::while");
			final XPathExpression argExpr =
				this.xPath.compile("argument_list/argument/expr/name");

			final NodeList javaList = (NodeList) this.xPath
				.evaluate(
					IAntiPatternDetection.JAVA_FILES_QUERY,
					xml,
					XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i1 = 0; i1 < javaLength; i1++) {
				final Node javaXml = javaList.item(i1);
				final String thisPackage = PACKAGE_EXP.evaluate(javaXml);
				final String filePath = FILEPATH_EXP.evaluate(javaXml);



				// Resets the count for the third case.
				// The detector assumes there is only one class per file
				// (and forgets about inner classes).
				nbOfNativeCalls = 0;
				allNativeCalls.clear();

				// Native method declaration
				final NodeList nativeDeclList = (NodeList) NATIVE_EXP
					.evaluate(javaXml, XPathConstants.NODESET);
					System.out.println(nativeDeclList.getLength());
				
					methodloop:
				for (int a = 0; a < nativeDeclList.getLength(); a++) {
					XPathExpression name = xpath.compile("name");
        final String thisNativeMethod = name.evaluate(nativeDeclList.item(a));
					
						int NumberOfCallsToThisMethod =0;
        //class
        XPathExpression cn1 = xpath.compile("(ancestor::class)[last()]");
        NodeList thisClass = (NodeList) cn1.evaluate(nativeDeclList.item(a), XPathConstants.NODESET);
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
            String name1 = childName.evaluate(childClasses.item(child));
            System.out.println("child class: "+name1);
            children[child] = name1;
        }
        
        

        //final String thisNativeMethod = nodes.item(a).getTextContent();
        System.out.println("Native method: "+thisNativeMethod);
        
    
        // //count of number of parameters in the native function
        // final String NoOfParams = String.format("count(descendant::parameter_list[1]/parameter)", thisNativeMethod);
        // final Number NumberOfParameters = (Number)xpath.evaluate(NoOfParams, nodes.item(a), XPathConstants.NUMBER);
        // //System.out.println("Number of parameter: "+NumberOfParameters.intValue());


        //list of parameter types for a particular native function
        final String params = String.format("descendant::parameter_list/parameter/decl/type/name", thisNativeMethod);
        final NodeList parameter_list = (NodeList) xpath.evaluate(params, nativeDeclList.item(a), XPathConstants.NODESET);
      
        System.out.println("Parameters type: ");
        for (int j=0; j<parameter_list.getLength();j++)
        {
          System.out.println((j+1)+" "+parameter_list.item(j).getTextContent());
        }

        //list of calls to a particular native function
        //final String callQuery = String.format("descendant::call[name='%s'] | descendant:: call[name/name='this' and name/name='%s']",thisNativeMethod, thisNativeMethod);
        

        final String callQuery = String.format("descendant::call[name='%s'] | descendant:: call[name/name='this' and name/name='%s']",thisNativeMethod, thisNativeMethod);
        final NodeList calls= (NodeList) xpath.evaluate(callQuery, thisClass.item(0), XPathConstants.NODESET);
        //System.out.println(calls.getLength());


        //calls in the same class without any object or class i.e, class.call()
        //first case: number of calls to a method inside a class is > threshold
        //check if the number of calls greater than five then we will match the arguments, basic criteria
        
            int numberOfCallsInsideSameClass=0;
        for(int i=0; i<calls.getLength();i++)
        {
          System.out.println("call: "+calls.item(i).getTextContent());


          //ancestor functions.
          XPathExpression xp = xpath.compile("ancestor::function");
          NodeList AncestorFunctions = (NodeList) xp.evaluate(calls.item(i), XPathConstants.NODESET);
        
        
          final String callQuery2 = String.format("descendant::argument_list[1]/argument");
            final NodeList callList = (NodeList) xpath.evaluate(callQuery2, calls.item(i), XPathConstants.NODESET); 
         

         //  //System.out.println("Number of arguments: "+callList.getLength());

          if(parameter_list.getLength()==callList.getLength())
          {
            int equal=1;
            for (int j=0; j<callList.getLength();j++)
            { 
              
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
    		final String thisClass11 = className1;
						final MLSAntiPattern thisAntiPattern =
							new MLSAntiPattern(
								this.getAntiPatternName(),
								"",
								thisNativeMethod,
								thisClass11,
								thisPackage,
								filePath);
						allNativeCalls.add(thisAntiPattern);
          System.out.println("yes.");

          // Checks whether the method is called in a loop, in which case it is
							// considered as called too many times in a first approximation
							final NodeList loops = (NodeList) loopExpr
								.evaluate(
									calls.item(i),
									XPathConstants.NODESET);
							if (loops.getLength() > 0) {
								System.out.println("third case: called inside loop");
								
								antiPatternSet.add(thisAntiPattern);
								continue methodloop;
							}
          numberOfCallsInsideSameClass+=1;

          /*
						 * SECOND CASE Calls to different native methods with at least one variable in
						 * common inside a Java method
						 */
          final String javaMethod =
							JAVA_METHOD_EXP.evaluate(calls.item(i));
						final NodeList argList = (NodeList) argExpr
							.evaluate(calls.item(i), XPathConstants.NODESET);
						for (int l = 0; l < argList.getLength(); l++) {
							final String var = argList.item(l).getTextContent();
							final MLSAntiPattern oldValue = variablesAsArguments
								.put(
									new AbstractMap.SimpleImmutableEntry<>(
										var,
										javaMethod),
									thisAntiPattern);
							if (oldValue != null
									&& !oldValue.equals(thisAntiPattern)
									&& oldValue
										.getClassName()
										.equals(
											thisAntiPattern.getClassName()) 
                    && oldValue.getFilePath().equals(thisAntiPattern.getFilePath())) {
								System.out.println("Second case: common varibale");
								antiPatternSet.add(oldValue);
								
								antiPatternSet.add(thisAntiPattern);
								continue methodloop;
							}
						}

          //CallingClasses.add(filename+"_"+className1);
          //System.out.println(NumberOfCallsToThisMethod);
          // final NodeList loops = (NodeList) xpath.evaluate("ancestor::for | ancestor::while", thisClass.item(0), XPathConstants.NODESET);
          // if(loops.getLength()>0)
          // {
          //   //System.out.println("Called in loop.");
          //   ExcessiveInterLanguageCommunication=true;
            
          // }
          
        }
      }
      System.out.println("Number of calls inside same class: "+numberOfCallsInsideSameClass);
  if(numberOfCallsInsideSameClass>minNbOfCallsToSameMethod)
  {
    System.out.println("First case.");
    final MLSAntiPattern thisAntiPattern =
              new MLSAntiPattern(
                this.getAntiPatternName(),
                "",
                thisNativeMethod,
                className1,
                thisPackage,
                filePath);
              antiPatternSet.add(thisAntiPattern);
  }
}

  //calls in the importing files.
  for(int iF = 0; iF<importFiles.getLength(); iF++)
  {
    //extract classes in importinf files then search for calls
    XPathExpression cli = xpath.compile("descendant::class");
    NodeList importingClasses = (NodeList) cli.evaluate(importFiles.item(iF), XPathConstants.NODESET);

    for(int icl = 0; icl<importingClasses.getLength(); icl++)
    {
      //list of calls to the native method in the file which import this native method
      final String callQuery3 = String.format("descendant::call[name='%s']", thisNativeMethod);
      final NodeList importingCalls= (NodeList) xpath.evaluate(callQuery3, importingClasses.item(icl), XPathConstants.NODESET);
    
      
        int numberOfCallsInsideSameClass1=0;

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
              final String thisClass11= className1;
                final MLSAntiPattern thisAntiPattern =
                  new MLSAntiPattern(
                    this.getAntiPatternName(),
                    "",
                    thisNativeMethod,
                    thisClass11,
                    thisPackage,
                    filePath);
                allNativeCalls.add(thisAntiPattern);
        
              System.out.println("yes.");
              // Checks whether the method is called in a loop, in which case it is
                  // considered as called too many times in a first approximation
                  final NodeList loops = (NodeList) loopExpr
                    .evaluate(
                      importingCalls.item(ic),
                      XPathConstants.NODESET);
                  if (loops.getLength() > 0) {
                    System.out.println("third case: called inside loop");
                    
                    antiPatternSet.add(thisAntiPattern);
                    continue methodloop;
                  }
              numberOfCallsInsideSameClass1+=1;


               /*
                 * SECOND CASE Calls to different native methods with at least one variable in
                 * common inside a Java method
                 */
              final String javaMethod =
                  JAVA_METHOD_EXP.evaluate(importingCalls.item(ic));
                final NodeList argList = (NodeList) argExpr
                  .evaluate(importingCalls.item(ic), XPathConstants.NODESET);
                for (int l = 0; l < argList.getLength(); l++) {
                  final String var = argList.item(l).getTextContent();
                  final MLSAntiPattern oldValue = variablesAsArguments
                    .put(
                      new AbstractMap.SimpleImmutableEntry<>(
                        var,
                        javaMethod),
                      thisAntiPattern);
                  if (oldValue != null
                      && !oldValue.equals(thisAntiPattern)
                      && oldValue
                        .getClassName()
                        .equals(
                          thisAntiPattern.getClassName())
                      && oldValue.getFilePath().equals(thisAntiPattern.getFilePath())) {
                    System.out.println("second case: common varibale");
                    antiPatternSet.add(oldValue);
                    antiPatternSet.add(thisAntiPattern);
                    continue methodloop;
                  }
                }

            }
          }
        }
        System.out.println("Number of calls inside same class: "+numberOfCallsInsideSameClass1);
        if(numberOfCallsInsideSameClass1>minNbOfCallsToSameMethod)
        {
          System.out.println("First case.");
          final MLSAntiPattern thisAntiPattern =
                    new MLSAntiPattern(
                      this.getAntiPatternName(),
                      "",
                      thisNativeMethod,
                      className1,
                      thisPackage,
                      filePath);
                    antiPatternSet.add(thisAntiPattern);
        }
    }
  }




//call in the child classes without class or object.
for(int cc=0; cc<childClasses.getLength(); cc++)
{
  final String callQuery2 = String.format("descendant::call[name='%s'] | descendant::call[name/name='super' and name/name='%s']",thisNativeMethod, thisNativeMethod);
  final NodeList calls2= (NodeList) xpath.evaluate(callQuery2, childClasses.item(cc), XPathConstants.NODESET);
  

    int numberOfCallsInsideSameClass2=0;
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
        	final String thisClass11 =className1;
						final MLSAntiPattern thisAntiPattern =
							new MLSAntiPattern(
								this.getAntiPatternName(),
								"",
								thisNativeMethod,
								thisClass11,
								thisPackage,
								filePath);
						allNativeCalls.add(thisAntiPattern);
    
          System.out.println("yes.");
          // Checks whether the method is called in a loop, in which case it is
							// considered as called too many times in a first approximation
							final NodeList loops = (NodeList) loopExpr
								.evaluate(
									calls2.item(e),
									XPathConstants.NODESET);
							if (loops.getLength() > 0) {
								System.out.println("third case: called inside loop");
								
								antiPatternSet.add(thisAntiPattern);
								continue methodloop;
							}
          numberOfCallsInsideSameClass2+=1;
          // callsInOtherClasses+=1;
          // CallingClasses.add(filename+"_"+className3);

           /*
						 * SECOND CASE Calls to different native methods with at least one variable in
						 * common inside a Java method
						 */
          final String javaMethod =
							JAVA_METHOD_EXP.evaluate(calls2.item(e));
						final NodeList argList = (NodeList) argExpr
							.evaluate(calls2.item(e), XPathConstants.NODESET);
						for (int l = 0; l < argList.getLength(); l++) {
							final String var = argList.item(l).getTextContent();
							final MLSAntiPattern oldValue = variablesAsArguments
								.put(
									new AbstractMap.SimpleImmutableEntry<>(
										var,
										javaMethod),
									thisAntiPattern);
							if (oldValue != null
									&& !oldValue.equals(thisAntiPattern)
									&& oldValue
										.getClassName()
										.equals(
											thisAntiPattern.getClassName())
                  && oldValue.getFilePath().equals(thisAntiPattern.getFilePath())) {
								System.out.println("second case: common varibale");
								antiPatternSet.add(oldValue);
								
								antiPatternSet.add(thisAntiPattern);
								continue methodloop;	
							}
						}


          }
        }
      }
      System.out.println("Number of calls inside same class: "+numberOfCallsInsideSameClass2);
      if(numberOfCallsInsideSameClass2>minNbOfCallsToSameMethod)
      {
        System.out.println("First case.");
        final MLSAntiPattern thisAntiPattern =
                  new MLSAntiPattern(
                    this.getAntiPatternName(),
                    "",
                    thisNativeMethod,
                    className1,
                    thisPackage,
                    filePath);
                  antiPatternSet.add(thisAntiPattern);
      }
}



  //extract all classes in the project then search for calls
    XPathExpression clp = xpath.compile("//class");
    NodeList allClasses = (NodeList) clp.evaluate(xml, XPathConstants.NODESET);
    System.out.println("number of classes: "+allClasses.getLength());
    for(int ac=0; ac<allClasses.getLength(); ac++)
    {
      final String callQuery1 = String.format("descendant::call[name/name='%s']",thisNativeMethod);
      final NodeList calls1= (NodeList) xpath.evaluate(callQuery1, allClasses.item(ac), XPathConstants.NODESET);  
      
      
        int numberOfCallsInsideSameClass3=0;
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
        XPathExpression cn2 = xpath.compile("(ancestor::class)");
        NodeList thisClass1 = (NodeList) cn2.evaluate(calls1.item(x), XPathConstants.NODESET);
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

        

        //commentring because we are not matching the caller now.
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
          final String thisClass11 =className1;
            final MLSAntiPattern thisAntiPattern =
              new MLSAntiPattern(
                this.getAntiPatternName(),
                "",
                thisNativeMethod,
                thisClass11,
                thisPackage,
                filePath);
            allNativeCalls.add(thisAntiPattern);
    
          // Checks whether the method is called in a loop, in which case it is
              // considered as called too many times in a first approximation
              final NodeList loops = (NodeList) loopExpr
                .evaluate(
                  calls1.item(x),
                  XPathConstants.NODESET);
              if (loops.getLength() > 0) {
                System.out.println("third case: called inside loop");
                
                antiPatternSet.add(thisAntiPattern);
                continue methodloop;
              }
          
          numberOfCallsInsideSameClass3+=1;

          //System.out.println(NumberOfCallsToThisMethod);
          System.out.println("yes.");
          // callsInOtherClasses+=1;
          // CallingClasses.add(filename+"_"+className2);


           /*
             * SECOND CASE Calls to different native methods with at least one variable in
             * common inside a Java method
             */
          final String javaMethod =
              JAVA_METHOD_EXP.evaluate(calls1.item(x));
            final NodeList argList = (NodeList) argExpr
              .evaluate(calls1.item(x), XPathConstants.NODESET);
            for (int l = 0; l < argList.getLength(); l++) {
              final String var = argList.item(l).getTextContent();
              final MLSAntiPattern oldValue = variablesAsArguments
                .put(
                  new AbstractMap.SimpleImmutableEntry<>(
                    var,
                    javaMethod),
                  thisAntiPattern);
              if (oldValue != null
                  && !oldValue.equals(thisAntiPattern)
                  && oldValue
                    .getClassName()
                    .equals(
                      thisAntiPattern.getClassName())
                  && oldValue.getFilePath().equals(thisAntiPattern.getFilePath())) {
                System.out.println("second case: common varibale");
                antiPatternSet.add(oldValue);
                
                antiPatternSet.add(thisAntiPattern);
                continue methodloop;
              }
            }

          
          
        }
      }
  }

else
{
    for(int cn = 0; cn< children.length ; cn++)
    {
        if(callerType.equals(children[cn]))
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


                argType = argType.replace("const","");
                argType = argType.replace("final","");
                argType = argType.replace("static","");
                //argType = argType.replace("short","");
                //argType = argType.replace("long","");
                argType = argType.replace("unsigned","");
                argType = argType.replace("signed","");
                argType = argType.replace(" ","");
               System.out.println(argType);
               argType = argType.split("<")[0];
                        String[] parts = argType.split("::");
                        if(parts.length>1)
                        {
                          argType=parts[parts.length-1];
                        }
                        else
                          argType=parts[0];

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
          final String thisClass11 =className1;
            final MLSAntiPattern thisAntiPattern =
              new MLSAntiPattern(
                this.getAntiPatternName(),
                "",
                thisNativeMethod,
                thisClass11,
                thisPackage,
                filePath);
            allNativeCalls.add(thisAntiPattern);
    
          // Checks whether the method is called in a loop, in which case it is
              // considered as called too many times in a first approximation
              final NodeList loops = (NodeList) loopExpr
                .evaluate(
                  calls1.item(x),
                  XPathConstants.NODESET);
              if (loops.getLength() > 0) {
                System.out.println("third case: called inside loop");
                
                antiPatternSet.add(thisAntiPattern);
                continue methodloop;
              }
          numberOfCallsInsideSameClass3+=1;
          //System.out.println(NumberOfCallsToThisMethod);
          System.out.println("yes.");
            // callsInOtherClasses+=1;
            // CallingClasses.add(filename+"_"+className2);

             /*
             * SECOND CASE Calls to different native methods with at least one variable in
             * common inside a Java method
             */
          final String javaMethod =
              JAVA_METHOD_EXP.evaluate(calls1.item(x));
            final NodeList argList = (NodeList) argExpr
              .evaluate(calls1.item(x), XPathConstants.NODESET);
            for (int l = 0; l < argList.getLength(); l++) {
              final String var = argList.item(l).getTextContent();
              final MLSAntiPattern oldValue = variablesAsArguments
                .put(
                  new AbstractMap.SimpleImmutableEntry<>(
                    var,
                    javaMethod),
                  thisAntiPattern);
              if (oldValue != null
                  && !oldValue.equals(thisAntiPattern)
                  && oldValue
                    .getClassName()
                    .equals(
                      thisAntiPattern.getClassName())
                  && oldValue.getFilePath().equals(thisAntiPattern.getFilePath())) {
                System.out.println("second case: common varibale");
                antiPatternSet.add(oldValue);
                antiPatternSet.add(thisAntiPattern);
                continue methodloop;
              }
            }

        }
      }
  }
        }
    }

}

}
System.out.println("Number of calls inside same class: "+numberOfCallsInsideSameClass3);
  if(numberOfCallsInsideSameClass3>minNbOfCallsToSameMethod)
  {
    System.out.println("First case.");
        final MLSAntiPattern thisAntiPattern =
                  new MLSAntiPattern(
                    this.getAntiPatternName(),
                    "",
                    thisNativeMethod,
                    className1,
                    thisPackage,
                    filePath);
                  antiPatternSet.add(thisAntiPattern);
  }

    }


  System.out.println("Number of calls to "+thisNativeMethod+": "+ NumberOfCallsToThisMethod);
System.out.println("--------------------------------------------");
  

 
					nbOfNativeCalls += NumberOfCallsToThisMethod;				

        }

			}

			this.setSetOfAntiPatterns(antiPatternSet);
		}
  }
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
