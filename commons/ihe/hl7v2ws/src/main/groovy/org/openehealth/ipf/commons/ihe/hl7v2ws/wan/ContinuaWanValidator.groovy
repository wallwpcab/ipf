/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.commons.ihe.hl7v2ws.wan


import java.util.Collection

import org.openehealth.ipf.commons.ihe.hl7v2ws.pcd01.Pcd01Validator
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.CWERule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.CXRule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.EIRule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.NARule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.SNRule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.XADRule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.XPNRule;
import org.openehealth.ipf.commons.ihe.hl7v2ws.wan.rules.XTNRule;
import org.openehealth.ipf.modules.hl7.validation.DefaultValidationContext
/**
 * Implements more strict validaton for PCD-01
 * 
 * There must be at least one ORDER_OBSERVATION in the message
 * There must be at least one PATIENT_RESULT
 * 
 * @author Mitko Kolev
 *
 */
class ContinuaWanValidator extends  Pcd01Validator {
    
    static DefaultValidationContext CONTINUA_CONTEXT = new DefaultValidationContext()
    static {
        CONTINUA_CONTEXT.configure()
        .forVersion('2.6')
           .message('ORU', 'R01').abstractSyntax(
        'MSH',
        [  {  'SFT'  }  ],
        {PATIENT_RESULT(
           [PATIENT(
              'PID',
              [  'PD1'  ],
              [  {  'NTE'  }  ],
              [  {  'NK1'  }  ],
              [VISIT(
                 'PV1',
                 [  'PV2'  ]
             )]
           )],
           {ORDER_OBSERVATION(
              [  'ORC'  ],
               'OBR',
               [{  'NTE'  }],
               [{TIMING_QTY(
                  'TQ1',
                  [{  'TQ2'  }]
               )}],
 
               [  'CTD'  ],
               [{OBSERVATION(
                  'OBX',
                  [  {  'NTE'  }  ]
               )}],
 
               [{  'FT1'  }],
               [{  'CTI'  }],
               [{SPECIMEN(
                  'SPM',
                  [{  'OBX'  }]
               )}]
            )}
         )},
         [ 'DSC' ]).checkCompositesWith(new CWERule())
                   .checkCompositesWith(new CXRule())
                   .checkCompositesWith(new EIRule())
                   .checkCompositesWith(new NARule())
                   .checkCompositesWith(new SNRule())
                   .checkCompositesWith(new XADRule())
                   .checkCompositesWith(new XPNRule())
                   .checkCompositesWith(new XTNRule())
    }
    
    DefaultValidationContext getValidationContext(){
        return CONTINUA_CONTEXT;
    }
    
    void checkMSH(msg, Collection<Exception> violations) {
        checkSegmentStructure(msg, 'MSH', [1, 2, 7, 9, 10, 11, 12, 15, 16, 21], violations)
    }
   
	void checkOBR(ooGroup, int obrIndex, Collection<Exception> violations) {
		checkSegmentValues(ooGroup, 'OBR', [1, 2, 3, 4], [obrIndex, ANY, ANY, ANY, ANY, ANY], violations)
	}
	/*
	 * The <code>checkTime</code> tells if the OBX-14 will be checked. It is requred when OBS-7 is not given
	 */
    void checkOBSERVATION(obs, boolean checkTime, Collection<Exception> violations) {
		checkSegmentStructure(obs, 'OBX', getOBXRequiredFields(obs.OBX, checkTime), violations);
		if (obs.OBX[2].value){
			//OBX [2] must have the same name as OBX[5]. This is guaranteed by the parser.
			checkFieldInAllowedDomain(obs,  'OBX', 2, ['CF', 'CWE', 'DT', 'DTM', 'ED', 'FT', 'NA', 'NM','ST','SN', 'TM', 'TX', 'XAD', 'XCN', 'XON', 'XPN'], violations);
		}
	}

	 /////////////////////// Response //////////////////////////
	 
	 void checkMSA(msg, Collection<Exception> violations) {
		 checkSegmentStructure(msg, 'MSA', [1, 2], violations)
		 checkFieldInAllowedDomain(msg, 'MSA', 1, ['AE', 'AA', 'AR'], violations)
		 
		 String errorCode = msg.MSA[1].toString();
		 if (!('AA'.equals(errorCode))){
			 checkERR(msg,violations)
		 }
	 }
	 
}
