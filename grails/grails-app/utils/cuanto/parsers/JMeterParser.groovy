/*
 Copyright (c) 2008 thePlatform, Inc.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto.parsers

import java.text.SimpleDateFormat

class JMeterParser implements CuantoTestParser {


  public List<ParsableTestOutcome> parseFile(File file) {
    parseStream(file.newInputStream())
  }


  public String getTestType() {
    return "JMeter"
  }


  public List<ParsableTestOutcome> parseStream(InputStream stream) {
    def parsableOutcomes = []

    def xml = new XmlParser().parse(stream)

    xml.'*'.each { node ->
      def out = new ParsableTestOutcome()

      def status = node.'@s'
      if (status=="true") { // 'sample' node has no result code, 'httpSample' node has HTTP results code 200 for success
        out.testResult = "Pass"
      } else {
        out.testResult = "Fail"
      }

      out.dateExecuted = new Date(node.'@ts'.toLong())
      out.testCase = new ParsableTestCase()
      out.testCase.packageName = node.'@lb'
      out.testCase.fullName = node.'@tn' + "." + node.'@tn'
      out.testCase.testName = node.'@tn'
      out.testCase.description = node.'@rm'
      out.duration = Integer.valueOf(node.'@t')


      def stackTrace = node.'@lb' + ': ' + node.'@tn' + ' (' + node.'@t' + 'ms)\n' + node.'@rm' +'\n\n' 
      node.'*'.each { innerNode ->
        stackTrace += innerNode.'@rc' + ': ' + innerNode.'@lb' + ' (' + innerNode.'@t' + 'ms)' + '\n'
      }
      out.testOutput = stackTrace

      parsableOutcomes << out
    }

    /*xml.httpSample.each { sample ->
      def out = new ParsableTestOutcome()
      def resultCode = sample.'@rc'
      if (resultCode == "200") {
        out.testResult = "Pass"
      } else {
        out.testResult = "Fail"
      }

      out.dateExecuted = new Date(sample.'@ts'.toLong())
      out.testCase = new ParsableTestCase()
      out.testCase.packageName = sample.'@lb'
      out.testCase.fullName = sample.'@tn' + "." + sample.'@tn'
      out.testCase.testName = sample.'@tn'
      out.testCase.description = sample.'@rm'
      out.duration = Integer.valueOf(sample.'@t')
      out.testOutput = sample.'@rm' + ': ' + sample.'@rm' + '\n' + sample
      parsableOutcomes << out
    }*/


    return parsableOutcomes
  }


}