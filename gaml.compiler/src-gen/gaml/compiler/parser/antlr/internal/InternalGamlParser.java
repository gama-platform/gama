package gaml.compiler.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import gaml.compiler.services.GamlGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
@SuppressWarnings("all")
public class InternalGamlParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_STRING", "RULE_INTEGER", "RULE_DOUBLE", "RULE_BOOLEAN", "RULE_KEYWORD", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'__synthetic__'", "'<-'", "'model'", "'import'", "'as'", "'@'", "'['", "']'", "'name:'", "'model:'", "';'", "'global'", "'action:'", "'loop'", "'if'", "'condition:'", "'else'", "'try'", "'catch'", "'return'", "'value:'", "'when'", "':'", "'('", "')'", "'action'", "'{'", "'}'", "'='", "'equation:'", "'display'", "'equation'", "'solve'", "'species'", "'grid'", "'image'", "'graphics'", "'event'", "'overlay'", "'experiment'", "'ask'", "'release'", "'capture'", "'create'", "'write'", "'error'", "'warn'", "'exception'", "'save'", "'assert'", "'inspect'", "'browse'", "'restore'", "'draw'", "'using'", "'switch'", "'put'", "'add'", "'remove'", "'match'", "'match_between'", "'match_one'", "'parameter'", "'status'", "'highlight'", "'focus_on'", "'layout'", "'light'", "'camera'", "'text'", "'image_layer'", "'data'", "'chart'", "'agents'", "'display_population'", "'display_grid'", "'datalist'", "'mesh'", "'rotation'", "'do'", "'invoke'", "'var'", "'const'", "'let'", "'arg'", "'init'", "'reflex'", "'aspect'", "'<<'", "'>'", "'<<+'", "'>-'", "'+<-'", "'<+'", "','", "'returns:'", "'as:'", "'of:'", "'parent:'", "'species:'", "'type:'", "'camera:'", "'data:'", "'const:'", "'topology:'", "'item:'", "'init:'", "'message:'", "'control:'", "'layout:'", "'environment:'", "'text:'", "'image:'", "'using:'", "'parameter:'", "'aspect:'", "'light:'", "'on_change:'", "'var:'", "'->'", "'::'", "'?'", "'or'", "'and'", "'!='", "'>='", "'<='", "'<'", "'+'", "'-'", "'*'", "'/'", "'^'", "'#'", "'!'", "'not'", "'.'", "'**unit*'", "'**type*'", "'**action*'", "'**skill*'", "'**var*'", "'**equation*'"
    };
    public static final int T__144=144;
    public static final int T__143=143;
    public static final int T__146=146;
    public static final int T__50=50;
    public static final int T__145=145;
    public static final int T__140=140;
    public static final int T__142=142;
    public static final int T__141=141;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__137=137;
    public static final int T__52=52;
    public static final int T__136=136;
    public static final int T__53=53;
    public static final int T__139=139;
    public static final int T__54=54;
    public static final int T__138=138;
    public static final int T__133=133;
    public static final int T__132=132;
    public static final int T__60=60;
    public static final int T__135=135;
    public static final int T__61=61;
    public static final int T__134=134;
    public static final int RULE_ID=4;
    public static final int T__131=131;
    public static final int T__130=130;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=10;
    public static final int T__67=67;
    public static final int T__129=129;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__126=126;
    public static final int T__63=63;
    public static final int T__125=125;
    public static final int T__64=64;
    public static final int T__128=128;
    public static final int T__65=65;
    public static final int T__127=127;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__155=155;
    public static final int RULE_KEYWORD=9;
    public static final int T__154=154;
    public static final int T__156=156;
    public static final int T__151=151;
    public static final int T__150=150;
    public static final int T__153=153;
    public static final int T__152=152;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__148=148;
    public static final int T__41=41;
    public static final int T__147=147;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__149=149;
    public static final int T__91=91;
    public static final int T__100=100;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__102=102;
    public static final int T__94=94;
    public static final int T__101=101;
    public static final int T__90=90;
    public static final int RULE_BOOLEAN=8;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__99=99;
    public static final int T__14=14;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__98=98;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__122=122;
    public static final int T__70=70;
    public static final int T__121=121;
    public static final int T__71=71;
    public static final int T__124=124;
    public static final int T__72=72;
    public static final int T__123=123;
    public static final int T__120=120;
    public static final int RULE_STRING=5;
    public static final int RULE_SL_COMMENT=11;
    public static final int RULE_DOUBLE=7;
    public static final int T__77=77;
    public static final int T__119=119;
    public static final int T__78=78;
    public static final int T__118=118;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int T__115=115;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__114=114;
    public static final int T__75=75;
    public static final int T__117=117;
    public static final int T__76=76;
    public static final int T__116=116;
    public static final int T__80=80;
    public static final int T__111=111;
    public static final int T__81=81;
    public static final int T__110=110;
    public static final int T__82=82;
    public static final int T__113=113;
    public static final int T__83=83;
    public static final int T__112=112;
    public static final int RULE_WS=12;
    public static final int RULE_ANY_OTHER=13;
    public static final int T__88=88;
    public static final int T__108=108;
    public static final int T__89=89;
    public static final int T__107=107;
    public static final int T__109=109;
    public static final int T__84=84;
    public static final int T__104=104;
    public static final int T__85=85;
    public static final int T__103=103;
    public static final int RULE_INTEGER=6;
    public static final int T__86=86;
    public static final int T__106=106;
    public static final int T__87=87;
    public static final int T__105=105;

    // delegates
    // delegators


        public InternalGamlParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalGamlParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalGamlParser.tokenNames; }
    public String getGrammarFileName() { return "InternalGaml.g"; }



     	private GamlGrammarAccess grammarAccess;

        public InternalGamlParser(TokenStream input, GamlGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "Entry";
       	}

       	@Override
       	protected GamlGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleEntry"
    // InternalGaml.g:64:1: entryRuleEntry returns [EObject current=null] : iv_ruleEntry= ruleEntry EOF ;
    public final EObject entryRuleEntry() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEntry = null;


        try {
            // InternalGaml.g:64:46: (iv_ruleEntry= ruleEntry EOF )
            // InternalGaml.g:65:2: iv_ruleEntry= ruleEntry EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getEntryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleEntry=ruleEntry();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleEntry; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleEntry"


    // $ANTLR start "ruleEntry"
    // InternalGaml.g:71:1: ruleEntry returns [EObject current=null] : ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StringEvaluator_1= ruleStringEvaluator | this_StandaloneBlock_2= ruleStandaloneBlock | this_ExperimentFileStructure_3= ruleExperimentFileStructure ) ;
    public final EObject ruleEntry() throws RecognitionException {
        EObject current = null;

        EObject this_Model_0 = null;

        EObject this_StringEvaluator_1 = null;

        EObject this_StandaloneBlock_2 = null;

        EObject this_ExperimentFileStructure_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:77:2: ( ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StringEvaluator_1= ruleStringEvaluator | this_StandaloneBlock_2= ruleStandaloneBlock | this_ExperimentFileStructure_3= ruleExperimentFileStructure ) )
            // InternalGaml.g:78:2: ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StringEvaluator_1= ruleStringEvaluator | this_StandaloneBlock_2= ruleStandaloneBlock | this_ExperimentFileStructure_3= ruleExperimentFileStructure )
            {
            // InternalGaml.g:78:2: ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StringEvaluator_1= ruleStringEvaluator | this_StandaloneBlock_2= ruleStandaloneBlock | this_ExperimentFileStructure_3= ruleExperimentFileStructure )
            int alt1=4;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==19) && (synpred1_InternalGaml())) {
                alt1=1;
            }
            else if ( (LA1_0==16) && (synpred1_InternalGaml())) {
                alt1=1;
            }
            else if ( (LA1_0==RULE_ID) ) {
                alt1=2;
            }
            else if ( (LA1_0==14) ) {
                alt1=3;
            }
            else if ( (LA1_0==53) ) {
                alt1=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // InternalGaml.g:79:3: ( ( '@' | 'model' )=>this_Model_0= ruleModel )
                    {
                    // InternalGaml.g:79:3: ( ( '@' | 'model' )=>this_Model_0= ruleModel )
                    // InternalGaml.g:80:4: ( '@' | 'model' )=>this_Model_0= ruleModel
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getEntryAccess().getModelParserRuleCall_0());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_Model_0=ruleModel();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_Model_0;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:91:3: this_StringEvaluator_1= ruleStringEvaluator
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEntryAccess().getStringEvaluatorParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StringEvaluator_1=ruleStringEvaluator();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StringEvaluator_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:100:3: this_StandaloneBlock_2= ruleStandaloneBlock
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEntryAccess().getStandaloneBlockParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StandaloneBlock_2=ruleStandaloneBlock();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StandaloneBlock_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:109:3: this_ExperimentFileStructure_3= ruleExperimentFileStructure
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEntryAccess().getExperimentFileStructureParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ExperimentFileStructure_3=ruleExperimentFileStructure();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ExperimentFileStructure_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleEntry"


    // $ANTLR start "entryRuleStandaloneBlock"
    // InternalGaml.g:121:1: entryRuleStandaloneBlock returns [EObject current=null] : iv_ruleStandaloneBlock= ruleStandaloneBlock EOF ;
    public final EObject entryRuleStandaloneBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStandaloneBlock = null;


        try {
            // InternalGaml.g:121:56: (iv_ruleStandaloneBlock= ruleStandaloneBlock EOF )
            // InternalGaml.g:122:2: iv_ruleStandaloneBlock= ruleStandaloneBlock EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStandaloneBlockRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStandaloneBlock=ruleStandaloneBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStandaloneBlock; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStandaloneBlock"


    // $ANTLR start "ruleStandaloneBlock"
    // InternalGaml.g:128:1: ruleStandaloneBlock returns [EObject current=null] : (otherlv_0= '__synthetic__' ( (lv_block_1_0= ruleBlock ) ) ) ;
    public final EObject ruleStandaloneBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:134:2: ( (otherlv_0= '__synthetic__' ( (lv_block_1_0= ruleBlock ) ) ) )
            // InternalGaml.g:135:2: (otherlv_0= '__synthetic__' ( (lv_block_1_0= ruleBlock ) ) )
            {
            // InternalGaml.g:135:2: (otherlv_0= '__synthetic__' ( (lv_block_1_0= ruleBlock ) ) )
            // InternalGaml.g:136:3: otherlv_0= '__synthetic__' ( (lv_block_1_0= ruleBlock ) )
            {
            otherlv_0=(Token)match(input,14,FOLLOW_3); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getStandaloneBlockAccess().get__synthetic__Keyword_0());
              		
            }
            // InternalGaml.g:140:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:141:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:141:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:142:5: lv_block_1_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStandaloneBlockAccess().getBlockBlockParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_1_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getStandaloneBlockRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_1_0,
              						"gaml.compiler.Gaml.Block");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStandaloneBlock"


    // $ANTLR start "entryRuleStringEvaluator"
    // InternalGaml.g:163:1: entryRuleStringEvaluator returns [EObject current=null] : iv_ruleStringEvaluator= ruleStringEvaluator EOF ;
    public final EObject entryRuleStringEvaluator() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStringEvaluator = null;


        try {
            // InternalGaml.g:163:56: (iv_ruleStringEvaluator= ruleStringEvaluator EOF )
            // InternalGaml.g:164:2: iv_ruleStringEvaluator= ruleStringEvaluator EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStringEvaluatorRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStringEvaluator=ruleStringEvaluator();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStringEvaluator; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStringEvaluator"


    // $ANTLR start "ruleStringEvaluator"
    // InternalGaml.g:170:1: ruleStringEvaluator returns [EObject current=null] : ( ( (lv_toto_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) ) ;
    public final EObject ruleStringEvaluator() throws RecognitionException {
        EObject current = null;

        Token lv_toto_0_0=null;
        Token otherlv_1=null;
        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:176:2: ( ( ( (lv_toto_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:177:2: ( ( (lv_toto_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:177:2: ( ( (lv_toto_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) )
            // InternalGaml.g:178:3: ( (lv_toto_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) )
            {
            // InternalGaml.g:178:3: ( (lv_toto_0_0= RULE_ID ) )
            // InternalGaml.g:179:4: (lv_toto_0_0= RULE_ID )
            {
            // InternalGaml.g:179:4: (lv_toto_0_0= RULE_ID )
            // InternalGaml.g:180:5: lv_toto_0_0= RULE_ID
            {
            lv_toto_0_0=(Token)match(input,RULE_ID,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_toto_0_0, grammarAccess.getStringEvaluatorAccess().getTotoIDTerminalRuleCall_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getStringEvaluatorRule());
              					}
              					setWithLastConsumed(
              						current,
              						"toto",
              						lv_toto_0_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }

            otherlv_1=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getStringEvaluatorAccess().getLessThanSignHyphenMinusKeyword_1());
              		
            }
            // InternalGaml.g:200:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:201:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:201:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:202:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStringEvaluatorAccess().getExprExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getStringEvaluatorRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStringEvaluator"


    // $ANTLR start "entryRuleModel"
    // InternalGaml.g:223:1: entryRuleModel returns [EObject current=null] : iv_ruleModel= ruleModel EOF ;
    public final EObject entryRuleModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModel = null;


        try {
            // InternalGaml.g:223:46: (iv_ruleModel= ruleModel EOF )
            // InternalGaml.g:224:2: iv_ruleModel= ruleModel EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getModelRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleModel=ruleModel();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleModel; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // InternalGaml.g:230:1: ruleModel returns [EObject current=null] : ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) ;
    public final EObject ruleModel() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_pragmas_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject lv_imports_3_0 = null;

        EObject lv_block_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:236:2: ( ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) )
            // InternalGaml.g:237:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            {
            // InternalGaml.g:237:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            // InternalGaml.g:238:3: ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) )
            {
            // InternalGaml.g:238:3: ( (lv_pragmas_0_0= rulePragma ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==19) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // InternalGaml.g:239:4: (lv_pragmas_0_0= rulePragma )
            	    {
            	    // InternalGaml.g:239:4: (lv_pragmas_0_0= rulePragma )
            	    // InternalGaml.g:240:5: lv_pragmas_0_0= rulePragma
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getPragmasPragmaParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_6);
            	    lv_pragmas_0_0=rulePragma();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getModelRule());
            	      					}
            	      					add(
            	      						current,
            	      						"pragmas",
            	      						lv_pragmas_0_0,
            	      						"gaml.compiler.Gaml.Pragma");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            otherlv_1=(Token)match(input,16,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getModelAccess().getModelKeyword_1());
              		
            }
            // InternalGaml.g:261:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:262:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:262:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:263:5: lv_name_2_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getModelAccess().getNameValid_IDParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_8);
            lv_name_2_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getModelRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_2_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:280:3: ( (lv_imports_3_0= ruleImport ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==17) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // InternalGaml.g:281:4: (lv_imports_3_0= ruleImport )
            	    {
            	    // InternalGaml.g:281:4: (lv_imports_3_0= ruleImport )
            	    // InternalGaml.g:282:5: lv_imports_3_0= ruleImport
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getImportsImportParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_imports_3_0=ruleImport();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getModelRule());
            	      					}
            	      					add(
            	      						current,
            	      						"imports",
            	      						lv_imports_3_0,
            	      						"gaml.compiler.Gaml.Import");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // InternalGaml.g:299:3: ( (lv_block_4_0= ruleModelBlock ) )
            // InternalGaml.g:300:4: (lv_block_4_0= ruleModelBlock )
            {
            // InternalGaml.g:300:4: (lv_block_4_0= ruleModelBlock )
            // InternalGaml.g:301:5: lv_block_4_0= ruleModelBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getModelAccess().getBlockModelBlockParserRuleCall_4_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_4_0=ruleModelBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getModelRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_4_0,
              						"gaml.compiler.Gaml.ModelBlock");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleModelBlock"
    // InternalGaml.g:322:1: entryRuleModelBlock returns [EObject current=null] : iv_ruleModelBlock= ruleModelBlock EOF ;
    public final EObject entryRuleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModelBlock = null;


        try {
            // InternalGaml.g:322:51: (iv_ruleModelBlock= ruleModelBlock EOF )
            // InternalGaml.g:323:2: iv_ruleModelBlock= ruleModelBlock EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getModelBlockRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleModelBlock=ruleModelBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleModelBlock; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModelBlock"


    // $ANTLR start "ruleModelBlock"
    // InternalGaml.g:329:1: ruleModelBlock returns [EObject current=null] : ( () ( (lv_statements_1_0= ruleS_Section ) )* ) ;
    public final EObject ruleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:335:2: ( ( () ( (lv_statements_1_0= ruleS_Section ) )* ) )
            // InternalGaml.g:336:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            {
            // InternalGaml.g:336:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            // InternalGaml.g:337:3: () ( (lv_statements_1_0= ruleS_Section ) )*
            {
            // InternalGaml.g:337:3: ()
            // InternalGaml.g:338:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getModelBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:344:3: ( (lv_statements_1_0= ruleS_Section ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==25||(LA4_0>=47 && LA4_0<=48)||LA4_0==53) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalGaml.g:345:4: (lv_statements_1_0= ruleS_Section )
            	    {
            	    // InternalGaml.g:345:4: (lv_statements_1_0= ruleS_Section )
            	    // InternalGaml.g:346:5: lv_statements_1_0= ruleS_Section
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelBlockAccess().getStatementsS_SectionParserRuleCall_1_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_9);
            	    lv_statements_1_0=ruleS_Section();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getModelBlockRule());
            	      					}
            	      					add(
            	      						current,
            	      						"statements",
            	      						lv_statements_1_0,
            	      						"gaml.compiler.Gaml.S_Section");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModelBlock"


    // $ANTLR start "entryRuleImport"
    // InternalGaml.g:367:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // InternalGaml.g:367:47: (iv_ruleImport= ruleImport EOF )
            // InternalGaml.g:368:2: iv_ruleImport= ruleImport EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getImportRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleImport=ruleImport();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleImport; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleImport"


    // $ANTLR start "ruleImport"
    // InternalGaml.g:374:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_importURI_1_0=null;
        Token otherlv_2=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:380:2: ( (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) )
            // InternalGaml.g:381:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            {
            // InternalGaml.g:381:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            // InternalGaml.g:382:3: otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            {
            otherlv_0=(Token)match(input,17,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
              		
            }
            // InternalGaml.g:386:3: ( (lv_importURI_1_0= RULE_STRING ) )
            // InternalGaml.g:387:4: (lv_importURI_1_0= RULE_STRING )
            {
            // InternalGaml.g:387:4: (lv_importURI_1_0= RULE_STRING )
            // InternalGaml.g:388:5: lv_importURI_1_0= RULE_STRING
            {
            lv_importURI_1_0=(Token)match(input,RULE_STRING,FOLLOW_11); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_importURI_1_0, grammarAccess.getImportAccess().getImportURISTRINGTerminalRuleCall_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getImportRule());
              					}
              					setWithLastConsumed(
              						current,
              						"importURI",
              						lv_importURI_1_0,
              						"gaml.compiler.Gaml.STRING");
              				
            }

            }


            }

            // InternalGaml.g:404:3: (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==18) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // InternalGaml.g:405:4: otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) )
                    {
                    otherlv_2=(Token)match(input,18,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getImportAccess().getAsKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:409:4: ( (lv_name_3_0= ruleValid_ID ) )
                    // InternalGaml.g:410:5: (lv_name_3_0= ruleValid_ID )
                    {
                    // InternalGaml.g:410:5: (lv_name_3_0= ruleValid_ID )
                    // InternalGaml.g:411:6: lv_name_3_0= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getImportAccess().getNameValid_IDParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_3_0=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getImportRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_3_0,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleImport"


    // $ANTLR start "entryRulePragma"
    // InternalGaml.g:433:1: entryRulePragma returns [EObject current=null] : iv_rulePragma= rulePragma EOF ;
    public final EObject entryRulePragma() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePragma = null;


        try {
            // InternalGaml.g:433:47: (iv_rulePragma= rulePragma EOF )
            // InternalGaml.g:434:2: iv_rulePragma= rulePragma EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPragmaRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePragma=rulePragma();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePragma; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePragma"


    // $ANTLR start "rulePragma"
    // InternalGaml.g:440:1: rulePragma returns [EObject current=null] : (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) ;
    public final EObject rulePragma() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_plugins_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:446:2: ( (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) )
            // InternalGaml.g:447:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            {
            // InternalGaml.g:447:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            // InternalGaml.g:448:3: otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            {
            otherlv_0=(Token)match(input,19,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getPragmaAccess().getCommercialAtKeyword_0());
              		
            }
            // InternalGaml.g:452:3: ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            // InternalGaml.g:453:4: ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            {
            // InternalGaml.g:453:4: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:454:5: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:454:5: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:455:6: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              						newLeafNode(lv_name_1_0, grammarAccess.getPragmaAccess().getNameIDTerminalRuleCall_1_0_0());
              					
            }
            if ( state.backtracking==0 ) {

              						if (current==null) {
              							current = createModelElement(grammarAccess.getPragmaRule());
              						}
              						setWithLastConsumed(
              							current,
              							"name",
              							lv_name_1_0,
              							"gaml.compiler.Gaml.ID");
              					
            }

            }


            }

            // InternalGaml.g:471:4: (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==20) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // InternalGaml.g:472:5: otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']'
                    {
                    otherlv_2=(Token)match(input,20,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getPragmaAccess().getLeftSquareBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:476:5: ( (lv_plugins_3_0= ruleExpressionList ) )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( ((LA6_0>=RULE_ID && LA6_0<=RULE_KEYWORD)||LA6_0==20||LA6_0==22||LA6_0==26||(LA6_0>=34 && LA6_0<=35)||LA6_0==37||LA6_0==40||LA6_0==45||(LA6_0>=47 && LA6_0<=49)||(LA6_0>=53 && LA6_0<=101)||(LA6_0>=109 && LA6_0<=132)||LA6_0==143||(LA6_0>=147 && LA6_0<=149)) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // InternalGaml.g:477:6: (lv_plugins_3_0= ruleExpressionList )
                            {
                            // InternalGaml.g:477:6: (lv_plugins_3_0= ruleExpressionList )
                            // InternalGaml.g:478:7: lv_plugins_3_0= ruleExpressionList
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPragmaAccess().getPluginsExpressionListParserRuleCall_1_1_1_0());
                              						
                            }
                            pushFollow(FOLLOW_15);
                            lv_plugins_3_0=ruleExpressionList();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getPragmaRule());
                              							}
                              							set(
                              								current,
                              								"plugins",
                              								lv_plugins_3_0,
                              								"gaml.compiler.Gaml.ExpressionList");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }
                            break;

                    }

                    otherlv_4=(Token)match(input,21,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getPragmaAccess().getRightSquareBracketKeyword_1_1_2());
                      				
                    }

                    }
                    break;

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePragma"


    // $ANTLR start "entryRuleExperimentFileStructure"
    // InternalGaml.g:505:1: entryRuleExperimentFileStructure returns [EObject current=null] : iv_ruleExperimentFileStructure= ruleExperimentFileStructure EOF ;
    public final EObject entryRuleExperimentFileStructure() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExperimentFileStructure = null;


        try {
            // InternalGaml.g:505:64: (iv_ruleExperimentFileStructure= ruleExperimentFileStructure EOF )
            // InternalGaml.g:506:2: iv_ruleExperimentFileStructure= ruleExperimentFileStructure EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getExperimentFileStructureRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleExperimentFileStructure=ruleExperimentFileStructure();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleExperimentFileStructure; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExperimentFileStructure"


    // $ANTLR start "ruleExperimentFileStructure"
    // InternalGaml.g:512:1: ruleExperimentFileStructure returns [EObject current=null] : ( (lv_exp_0_0= ruleHeadlessExperiment ) ) ;
    public final EObject ruleExperimentFileStructure() throws RecognitionException {
        EObject current = null;

        EObject lv_exp_0_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:518:2: ( ( (lv_exp_0_0= ruleHeadlessExperiment ) ) )
            // InternalGaml.g:519:2: ( (lv_exp_0_0= ruleHeadlessExperiment ) )
            {
            // InternalGaml.g:519:2: ( (lv_exp_0_0= ruleHeadlessExperiment ) )
            // InternalGaml.g:520:3: (lv_exp_0_0= ruleHeadlessExperiment )
            {
            // InternalGaml.g:520:3: (lv_exp_0_0= ruleHeadlessExperiment )
            // InternalGaml.g:521:4: lv_exp_0_0= ruleHeadlessExperiment
            {
            if ( state.backtracking==0 ) {

              				newCompositeNode(grammarAccess.getExperimentFileStructureAccess().getExpHeadlessExperimentParserRuleCall_0());
              			
            }
            pushFollow(FOLLOW_2);
            lv_exp_0_0=ruleHeadlessExperiment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              				if (current==null) {
              					current = createModelElementForParent(grammarAccess.getExperimentFileStructureRule());
              				}
              				set(
              					current,
              					"exp",
              					lv_exp_0_0,
              					"gaml.compiler.Gaml.HeadlessExperiment");
              				afterParserOrEnumRuleCall();
              			
            }

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExperimentFileStructure"


    // $ANTLR start "entryRuleHeadlessExperiment"
    // InternalGaml.g:541:1: entryRuleHeadlessExperiment returns [EObject current=null] : iv_ruleHeadlessExperiment= ruleHeadlessExperiment EOF ;
    public final EObject entryRuleHeadlessExperiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleHeadlessExperiment = null;


        try {
            // InternalGaml.g:541:59: (iv_ruleHeadlessExperiment= ruleHeadlessExperiment EOF )
            // InternalGaml.g:542:2: iv_ruleHeadlessExperiment= ruleHeadlessExperiment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getHeadlessExperimentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleHeadlessExperiment=ruleHeadlessExperiment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleHeadlessExperiment; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleHeadlessExperiment"


    // $ANTLR start "ruleHeadlessExperiment"
    // InternalGaml.g:548:1: ruleHeadlessExperiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleHeadlessExperiment() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        Token lv_name_2_2=null;
        Token otherlv_3=null;
        Token lv_importURI_4_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_1 = null;

        EObject this_FacetsAndBlock_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:554:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:555:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:555:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:556:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:556:3: ( (lv_key_0_0= rule_ExperimentKey ) )
            // InternalGaml.g:557:4: (lv_key_0_0= rule_ExperimentKey )
            {
            // InternalGaml.g:557:4: (lv_key_0_0= rule_ExperimentKey )
            // InternalGaml.g:558:5: lv_key_0_0= rule_ExperimentKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getHeadlessExperimentAccess().getKey_ExperimentKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_16);
            lv_key_0_0=rule_ExperimentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getHeadlessExperimentRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._ExperimentKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:575:3: ( (lv_firstFacet_1_0= 'name:' ) )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==22) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // InternalGaml.g:576:4: (lv_firstFacet_1_0= 'name:' )
                    {
                    // InternalGaml.g:576:4: (lv_firstFacet_1_0= 'name:' )
                    // InternalGaml.g:577:5: lv_firstFacet_1_0= 'name:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getHeadlessExperimentAccess().getFirstFacetNameKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getHeadlessExperimentRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:589:3: ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) )
            // InternalGaml.g:590:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            {
            // InternalGaml.g:590:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            // InternalGaml.g:591:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            {
            // InternalGaml.g:591:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==RULE_ID||LA9_0==45||(LA9_0>=47 && LA9_0<=49)||(LA9_0>=53 && LA9_0<=101)) ) {
                alt9=1;
            }
            else if ( (LA9_0==RULE_STRING) ) {
                alt9=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // InternalGaml.g:592:6: lv_name_2_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getHeadlessExperimentAccess().getNameValid_IDParserRuleCall_2_0_0());
                      					
                    }
                    pushFollow(FOLLOW_18);
                    lv_name_2_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getHeadlessExperimentRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_2_1,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:608:6: lv_name_2_2= RULE_STRING
                    {
                    lv_name_2_2=(Token)match(input,RULE_STRING,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_2_2, grammarAccess.getHeadlessExperimentAccess().getNameSTRINGTerminalRuleCall_2_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getHeadlessExperimentRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"name",
                      							lv_name_2_2,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:625:3: (otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) ) )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==23) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // InternalGaml.g:626:4: otherlv_3= 'model:' ( (lv_importURI_4_0= RULE_STRING ) )
                    {
                    otherlv_3=(Token)match(input,23,FOLLOW_10); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getHeadlessExperimentAccess().getModelKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:630:4: ( (lv_importURI_4_0= RULE_STRING ) )
                    // InternalGaml.g:631:5: (lv_importURI_4_0= RULE_STRING )
                    {
                    // InternalGaml.g:631:5: (lv_importURI_4_0= RULE_STRING )
                    // InternalGaml.g:632:6: lv_importURI_4_0= RULE_STRING
                    {
                    lv_importURI_4_0=(Token)match(input,RULE_STRING,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_importURI_4_0, grammarAccess.getHeadlessExperimentAccess().getImportURISTRINGTerminalRuleCall_3_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getHeadlessExperimentRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"importURI",
                      							lv_importURI_4_0,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getHeadlessExperimentRule());
              			}
              			newCompositeNode(grammarAccess.getHeadlessExperimentAccess().getFacetsAndBlockParserRuleCall_4());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_5=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_5;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleHeadlessExperiment"


    // $ANTLR start "ruleFacetsAndBlock"
    // InternalGaml.g:665:1: ruleFacetsAndBlock[EObject in_current] returns [EObject current=in_current] : ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) ;
    public final EObject ruleFacetsAndBlock(EObject in_current) throws RecognitionException {
        EObject current = in_current;

        Token otherlv_2=null;
        EObject lv_facets_0_0 = null;

        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:671:2: ( ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) )
            // InternalGaml.g:672:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            {
            // InternalGaml.g:672:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            // InternalGaml.g:673:3: ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            {
            // InternalGaml.g:673:3: ( (lv_facets_0_0= ruleFacet ) )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==RULE_ID||LA11_0==15||LA11_0==22||LA11_0==26||(LA11_0>=34 && LA11_0<=35)||(LA11_0>=109 && LA11_0<=133)) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // InternalGaml.g:674:4: (lv_facets_0_0= ruleFacet )
            	    {
            	    // InternalGaml.g:674:4: (lv_facets_0_0= ruleFacet )
            	    // InternalGaml.g:675:5: lv_facets_0_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getFacetsAndBlockAccess().getFacetsFacetParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
            	    lv_facets_0_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getFacetsAndBlockRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_0_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            // InternalGaml.g:692:3: ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==40) ) {
                alt12=1;
            }
            else if ( (LA12_0==24) ) {
                alt12=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // InternalGaml.g:693:4: ( (lv_block_1_0= ruleBlock ) )
                    {
                    // InternalGaml.g:693:4: ( (lv_block_1_0= ruleBlock ) )
                    // InternalGaml.g:694:5: (lv_block_1_0= ruleBlock )
                    {
                    // InternalGaml.g:694:5: (lv_block_1_0= ruleBlock )
                    // InternalGaml.g:695:6: lv_block_1_0= ruleBlock
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getFacetsAndBlockAccess().getBlockBlockParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_block_1_0=ruleBlock();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getFacetsAndBlockRule());
                      						}
                      						set(
                      							current,
                      							"block",
                      							lv_block_1_0,
                      							"gaml.compiler.Gaml.Block");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:713:4: otherlv_2= ';'
                    {
                    otherlv_2=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getFacetsAndBlockAccess().getSemicolonKeyword_1_1());
                      			
                    }

                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFacetsAndBlock"


    // $ANTLR start "entryRuleS_Section"
    // InternalGaml.g:722:1: entryRuleS_Section returns [EObject current=null] : iv_ruleS_Section= ruleS_Section EOF ;
    public final EObject entryRuleS_Section() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Section = null;


        try {
            // InternalGaml.g:722:50: (iv_ruleS_Section= ruleS_Section EOF )
            // InternalGaml.g:723:2: iv_ruleS_Section= ruleS_Section EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_SectionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Section=ruleS_Section();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Section; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Section"


    // $ANTLR start "ruleS_Section"
    // InternalGaml.g:729:1: ruleS_Section returns [EObject current=null] : (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) ;
    public final EObject ruleS_Section() throws RecognitionException {
        EObject current = null;

        EObject this_S_Global_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Experiment_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:735:2: ( (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) )
            // InternalGaml.g:736:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            {
            // InternalGaml.g:736:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            int alt13=3;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt13=1;
                }
                break;
            case 47:
            case 48:
                {
                alt13=2;
                }
                break;
            case 53:
                {
                alt13=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // InternalGaml.g:737:3: this_S_Global_0= ruleS_Global
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_SectionAccess().getS_GlobalParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Global_0=ruleS_Global();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Global_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:746:3: this_S_Species_1= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_SectionAccess().getS_SpeciesParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Species_1=ruleS_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Species_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:755:3: this_S_Experiment_2= ruleS_Experiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_SectionAccess().getS_ExperimentParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Experiment_2=ruleS_Experiment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Experiment_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Section"


    // $ANTLR start "entryRuleS_Global"
    // InternalGaml.g:767:1: entryRuleS_Global returns [EObject current=null] : iv_ruleS_Global= ruleS_Global EOF ;
    public final EObject entryRuleS_Global() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Global = null;


        try {
            // InternalGaml.g:767:49: (iv_ruleS_Global= ruleS_Global EOF )
            // InternalGaml.g:768:2: iv_ruleS_Global= ruleS_Global EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_GlobalRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Global=ruleS_Global();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Global; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Global"


    // $ANTLR start "ruleS_Global"
    // InternalGaml.g:774:1: ruleS_Global returns [EObject current=null] : ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Global() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:780:2: ( ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:781:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:781:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:782:3: ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:782:3: ( (lv_key_0_0= 'global' ) )
            // InternalGaml.g:783:4: (lv_key_0_0= 'global' )
            {
            // InternalGaml.g:783:4: (lv_key_0_0= 'global' )
            // InternalGaml.g:784:5: lv_key_0_0= 'global'
            {
            lv_key_0_0=(Token)match(input,25,FOLLOW_18); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_GlobalAccess().getKeyGlobalKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_GlobalRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "global");
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_GlobalRule());
              			}
              			newCompositeNode(grammarAccess.getS_GlobalAccess().getFacetsAndBlockParserRuleCall_1());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_1=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_1;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Global"


    // $ANTLR start "entryRuleS_Species"
    // InternalGaml.g:811:1: entryRuleS_Species returns [EObject current=null] : iv_ruleS_Species= ruleS_Species EOF ;
    public final EObject entryRuleS_Species() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Species = null;


        try {
            // InternalGaml.g:811:50: (iv_ruleS_Species= ruleS_Species EOF )
            // InternalGaml.g:812:2: iv_ruleS_Species= ruleS_Species EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_SpeciesRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Species=ruleS_Species();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Species; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Species"


    // $ANTLR start "ruleS_Species"
    // InternalGaml.g:818:1: ruleS_Species returns [EObject current=null] : ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= RULE_ID ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Species() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        Token lv_name_2_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:824:2: ( ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= RULE_ID ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:825:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= RULE_ID ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:825:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= RULE_ID ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:826:3: ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= RULE_ID ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:826:3: ( (lv_key_0_0= rule_SpeciesKey ) )
            // InternalGaml.g:827:4: (lv_key_0_0= rule_SpeciesKey )
            {
            // InternalGaml.g:827:4: (lv_key_0_0= rule_SpeciesKey )
            // InternalGaml.g:828:5: lv_key_0_0= rule_SpeciesKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesAccess().getKey_SpeciesKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_20);
            lv_key_0_0=rule_SpeciesKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SpeciesRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._SpeciesKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:845:3: ( (lv_firstFacet_1_0= 'name:' ) )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==22) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // InternalGaml.g:846:4: (lv_firstFacet_1_0= 'name:' )
                    {
                    // InternalGaml.g:846:4: (lv_firstFacet_1_0= 'name:' )
                    // InternalGaml.g:847:5: lv_firstFacet_1_0= 'name:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_SpeciesAccess().getFirstFacetNameKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_SpeciesRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:859:3: ( (lv_name_2_0= RULE_ID ) )
            // InternalGaml.g:860:4: (lv_name_2_0= RULE_ID )
            {
            // InternalGaml.g:860:4: (lv_name_2_0= RULE_ID )
            // InternalGaml.g:861:5: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_18); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_name_2_0, grammarAccess.getS_SpeciesAccess().getNameIDTerminalRuleCall_2_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_SpeciesRule());
              					}
              					setWithLastConsumed(
              						current,
              						"name",
              						lv_name_2_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_SpeciesRule());
              			}
              			newCompositeNode(grammarAccess.getS_SpeciesAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Species"


    // $ANTLR start "entryRuleS_Experiment"
    // InternalGaml.g:892:1: entryRuleS_Experiment returns [EObject current=null] : iv_ruleS_Experiment= ruleS_Experiment EOF ;
    public final EObject entryRuleS_Experiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Experiment = null;


        try {
            // InternalGaml.g:892:53: (iv_ruleS_Experiment= ruleS_Experiment EOF )
            // InternalGaml.g:893:2: iv_ruleS_Experiment= ruleS_Experiment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_ExperimentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Experiment=ruleS_Experiment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Experiment; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Experiment"


    // $ANTLR start "ruleS_Experiment"
    // InternalGaml.g:899:1: ruleS_Experiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Experiment() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        Token lv_name_2_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_1 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:905:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:906:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:906:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:907:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:907:3: ( (lv_key_0_0= rule_ExperimentKey ) )
            // InternalGaml.g:908:4: (lv_key_0_0= rule_ExperimentKey )
            {
            // InternalGaml.g:908:4: (lv_key_0_0= rule_ExperimentKey )
            // InternalGaml.g:909:5: lv_key_0_0= rule_ExperimentKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ExperimentAccess().getKey_ExperimentKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_16);
            lv_key_0_0=rule_ExperimentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ExperimentRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._ExperimentKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:926:3: ( (lv_firstFacet_1_0= 'name:' ) )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==22) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // InternalGaml.g:927:4: (lv_firstFacet_1_0= 'name:' )
                    {
                    // InternalGaml.g:927:4: (lv_firstFacet_1_0= 'name:' )
                    // InternalGaml.g:928:5: lv_firstFacet_1_0= 'name:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_ExperimentAccess().getFirstFacetNameKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_ExperimentRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:940:3: ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) )
            // InternalGaml.g:941:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            {
            // InternalGaml.g:941:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            // InternalGaml.g:942:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            {
            // InternalGaml.g:942:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==RULE_ID||LA16_0==45||(LA16_0>=47 && LA16_0<=49)||(LA16_0>=53 && LA16_0<=101)) ) {
                alt16=1;
            }
            else if ( (LA16_0==RULE_STRING) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // InternalGaml.g:943:6: lv_name_2_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ExperimentAccess().getNameValid_IDParserRuleCall_2_0_0());
                      					
                    }
                    pushFollow(FOLLOW_18);
                    lv_name_2_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ExperimentRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_2_1,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:959:6: lv_name_2_2= RULE_STRING
                    {
                    lv_name_2_2=(Token)match(input,RULE_STRING,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_2_2, grammarAccess.getS_ExperimentAccess().getNameSTRINGTerminalRuleCall_2_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_ExperimentRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"name",
                      							lv_name_2_2,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_ExperimentRule());
              			}
              			newCompositeNode(grammarAccess.getS_ExperimentAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Experiment"


    // $ANTLR start "entryRuleStatement"
    // InternalGaml.g:991:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalGaml.g:991:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalGaml.g:992:2: iv_ruleStatement= ruleStatement EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatement=ruleStatement();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatement; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatement"


    // $ANTLR start "ruleStatement"
    // InternalGaml.g:998:1: ruleStatement returns [EObject current=null] : (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations ) | ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do ) | ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var ) | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_13= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition ) | this_S_Other_16= ruleS_Other ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_S_Display_0 = null;

        EObject this_S_Return_1 = null;

        EObject this_S_Solve_2 = null;

        EObject this_S_If_3 = null;

        EObject this_S_Try_4 = null;

        EObject this_S_Equations_5 = null;

        EObject this_S_Do_6 = null;

        EObject this_S_Loop_7 = null;

        EObject this_S_Action_8 = null;

        EObject this_S_Var_9 = null;

        EObject this_S_Species_10 = null;

        EObject this_S_Reflex_11 = null;

        EObject this_S_Assignment_12 = null;

        EObject this_S_General_13 = null;

        EObject this_S_Declaration_14 = null;

        EObject this_S_Definition_15 = null;

        EObject this_S_Other_16 = null;



        	enterRule();

        try {
            // InternalGaml.g:1004:2: ( (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations ) | ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do ) | ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var ) | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_13= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition ) | this_S_Other_16= ruleS_Other ) )
            // InternalGaml.g:1005:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations ) | ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do ) | ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var ) | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_13= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition ) | this_S_Other_16= ruleS_Other )
            {
            // InternalGaml.g:1005:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations ) | ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do ) | ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var ) | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_13= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition ) | this_S_Other_16= ruleS_Other )
            int alt17=17;
            alt17 = dfa17.predict(input);
            switch (alt17) {
                case 1 :
                    // InternalGaml.g:1006:3: this_S_Display_0= ruleS_Display
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_DisplayParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Display_0=ruleS_Display();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Display_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:1015:3: this_S_Return_1= ruleS_Return
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_ReturnParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Return_1=ruleS_Return();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Return_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:1024:3: this_S_Solve_2= ruleS_Solve
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_SolveParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Solve_2=ruleS_Solve();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Solve_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:1033:3: this_S_If_3= ruleS_If
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_IfParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_If_3=ruleS_If();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_If_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:1042:3: this_S_Try_4= ruleS_Try
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_TryParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Try_4=ruleS_Try();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Try_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:1051:3: ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations )
                    {
                    // InternalGaml.g:1051:3: ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations )
                    // InternalGaml.g:1052:4: ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_EquationsParserRuleCall_5());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Equations_5=ruleS_Equations();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Equations_5;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalGaml.g:1063:3: ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do )
                    {
                    // InternalGaml.g:1063:3: ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do )
                    // InternalGaml.g:1064:4: ( ruleS_Do )=>this_S_Do_6= ruleS_Do
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DoParserRuleCall_6());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Do_6=ruleS_Do();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Do_6;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalGaml.g:1075:3: ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop )
                    {
                    // InternalGaml.g:1075:3: ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop )
                    // InternalGaml.g:1076:4: ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_LoopParserRuleCall_7());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Loop_7=ruleS_Loop();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Loop_7;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalGaml.g:1087:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    {
                    // InternalGaml.g:1087:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    // InternalGaml.g:1088:4: ( ruleS_Action )=>this_S_Action_8= ruleS_Action
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_ActionParserRuleCall_8());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Action_8=ruleS_Action();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Action_8;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 10 :
                    // InternalGaml.g:1099:3: ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var )
                    {
                    // InternalGaml.g:1099:3: ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var )
                    // InternalGaml.g:1100:4: ( ruleS_Var )=>this_S_Var_9= ruleS_Var
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_VarParserRuleCall_9());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Var_9=ruleS_Var();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Var_9;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalGaml.g:1111:3: ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species )
                    {
                    // InternalGaml.g:1111:3: ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species )
                    // InternalGaml.g:1112:4: ( ruleS_Species )=>this_S_Species_10= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_SpeciesParserRuleCall_10());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Species_10=ruleS_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Species_10;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 12 :
                    // InternalGaml.g:1123:3: ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex )
                    {
                    // InternalGaml.g:1123:3: ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex )
                    // InternalGaml.g:1124:4: ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_ReflexParserRuleCall_11());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Reflex_11=ruleS_Reflex();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Reflex_11;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 13 :
                    // InternalGaml.g:1135:3: ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment )
                    {
                    // InternalGaml.g:1135:3: ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment )
                    // InternalGaml.g:1136:4: ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_AssignmentParserRuleCall_12());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Assignment_12=ruleS_Assignment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Assignment_12;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 14 :
                    // InternalGaml.g:1147:3: ( ( ruleS_General )=>this_S_General_13= ruleS_General )
                    {
                    // InternalGaml.g:1147:3: ( ( ruleS_General )=>this_S_General_13= ruleS_General )
                    // InternalGaml.g:1148:4: ( ruleS_General )=>this_S_General_13= ruleS_General
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_GeneralParserRuleCall_13());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_General_13=ruleS_General();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_General_13;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 15 :
                    // InternalGaml.g:1159:3: ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration )
                    {
                    // InternalGaml.g:1159:3: ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration )
                    // InternalGaml.g:1160:4: ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DeclarationParserRuleCall_14());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Declaration_14=ruleS_Declaration();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Declaration_14;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 16 :
                    // InternalGaml.g:1171:3: ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition )
                    {
                    // InternalGaml.g:1171:3: ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition )
                    // InternalGaml.g:1172:4: ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DefinitionParserRuleCall_15());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_15=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Definition_15;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 17 :
                    // InternalGaml.g:1183:3: this_S_Other_16= ruleS_Other
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_OtherParserRuleCall_16());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Other_16=ruleS_Other();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Other_16;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatement"


    // $ANTLR start "entryRuleS_General"
    // InternalGaml.g:1195:1: entryRuleS_General returns [EObject current=null] : iv_ruleS_General= ruleS_General EOF ;
    public final EObject entryRuleS_General() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_General = null;


        try {
            // InternalGaml.g:1195:50: (iv_ruleS_General= ruleS_General EOF )
            // InternalGaml.g:1196:2: iv_ruleS_General= ruleS_General EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_GeneralRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_General=ruleS_General();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_General; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_General"


    // $ANTLR start "ruleS_General"
    // InternalGaml.g:1202:1: ruleS_General returns [EObject current=null] : ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_General() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_firstFacet_1_0 = null;

        EObject lv_expr_2_0 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:1208:2: ( ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1209:2: ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1209:2: ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1210:3: ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1210:3: ( (lv_key_0_0= rule_GeneralKey ) )
            // InternalGaml.g:1211:4: (lv_key_0_0= rule_GeneralKey )
            {
            // InternalGaml.g:1211:4: (lv_key_0_0= rule_GeneralKey )
            // InternalGaml.g:1212:5: lv_key_0_0= rule_GeneralKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GeneralAccess().getKey_GeneralKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_21);
            lv_key_0_0=rule_GeneralKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_GeneralRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._GeneralKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1229:3: ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==22||LA18_0==26||(LA18_0>=34 && LA18_0<=35)||(LA18_0>=109 && LA18_0<=132)) ) {
                alt18=1;
            }
            else if ( (LA18_0==RULE_ID) ) {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==36) ) {
                    alt18=1;
                }
            }
            switch (alt18) {
                case 1 :
                    // InternalGaml.g:1230:4: (lv_firstFacet_1_0= ruleFirstFacetKey )
                    {
                    // InternalGaml.g:1230:4: (lv_firstFacet_1_0= ruleFirstFacetKey )
                    // InternalGaml.g:1231:5: lv_firstFacet_1_0= ruleFirstFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_GeneralAccess().getFirstFacetFirstFacetKeyParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_5);
                    lv_firstFacet_1_0=ruleFirstFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getS_GeneralRule());
                      					}
                      					set(
                      						current,
                      						"firstFacet",
                      						lv_firstFacet_1_0,
                      						"gaml.compiler.Gaml.FirstFacetKey");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:1248:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:1249:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:1249:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:1250:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GeneralAccess().getExprExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_GeneralRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_GeneralRule());
              			}
              			newCompositeNode(grammarAccess.getS_GeneralAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_General"


    // $ANTLR start "entryRuleS_Do"
    // InternalGaml.g:1282:1: entryRuleS_Do returns [EObject current=null] : iv_ruleS_Do= ruleS_Do EOF ;
    public final EObject entryRuleS_Do() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Do = null;


        try {
            // InternalGaml.g:1282:45: (iv_ruleS_Do= ruleS_Do EOF )
            // InternalGaml.g:1283:2: iv_ruleS_Do= ruleS_Do EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_DoRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Do=ruleS_Do();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Do; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Do"


    // $ANTLR start "ruleS_Do"
    // InternalGaml.g:1289:1: ruleS_Do returns [EObject current=null] : ( ( (lv_key_0_0= rule_DoKey ) ) ( (lv_firstFacet_1_0= 'action:' ) )? ( (lv_expr_2_0= ruleAbstractRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Do() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_2_0 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:1295:2: ( ( ( (lv_key_0_0= rule_DoKey ) ) ( (lv_firstFacet_1_0= 'action:' ) )? ( (lv_expr_2_0= ruleAbstractRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1296:2: ( ( (lv_key_0_0= rule_DoKey ) ) ( (lv_firstFacet_1_0= 'action:' ) )? ( (lv_expr_2_0= ruleAbstractRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1296:2: ( ( (lv_key_0_0= rule_DoKey ) ) ( (lv_firstFacet_1_0= 'action:' ) )? ( (lv_expr_2_0= ruleAbstractRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1297:3: ( (lv_key_0_0= rule_DoKey ) ) ( (lv_firstFacet_1_0= 'action:' ) )? ( (lv_expr_2_0= ruleAbstractRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1297:3: ( (lv_key_0_0= rule_DoKey ) )
            // InternalGaml.g:1298:4: (lv_key_0_0= rule_DoKey )
            {
            // InternalGaml.g:1298:4: (lv_key_0_0= rule_DoKey )
            // InternalGaml.g:1299:5: lv_key_0_0= rule_DoKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DoAccess().getKey_DoKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_22);
            lv_key_0_0=rule_DoKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DoRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._DoKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1316:3: ( (lv_firstFacet_1_0= 'action:' ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==26) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // InternalGaml.g:1317:4: (lv_firstFacet_1_0= 'action:' )
                    {
                    // InternalGaml.g:1317:4: (lv_firstFacet_1_0= 'action:' )
                    // InternalGaml.g:1318:5: lv_firstFacet_1_0= 'action:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,26,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_DoAccess().getFirstFacetActionKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_DoRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "action:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:1330:3: ( (lv_expr_2_0= ruleAbstractRef ) )
            // InternalGaml.g:1331:4: (lv_expr_2_0= ruleAbstractRef )
            {
            // InternalGaml.g:1331:4: (lv_expr_2_0= ruleAbstractRef )
            // InternalGaml.g:1332:5: lv_expr_2_0= ruleAbstractRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DoAccess().getExprAbstractRefParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_2_0=ruleAbstractRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DoRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.AbstractRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_DoRule());
              			}
              			newCompositeNode(grammarAccess.getS_DoAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Do"


    // $ANTLR start "entryRuleS_Loop"
    // InternalGaml.g:1364:1: entryRuleS_Loop returns [EObject current=null] : iv_ruleS_Loop= ruleS_Loop EOF ;
    public final EObject entryRuleS_Loop() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Loop = null;


        try {
            // InternalGaml.g:1364:47: (iv_ruleS_Loop= ruleS_Loop EOF )
            // InternalGaml.g:1365:2: iv_ruleS_Loop= ruleS_Loop EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_LoopRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Loop=ruleS_Loop();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Loop; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Loop"


    // $ANTLR start "ruleS_Loop"
    // InternalGaml.g:1371:1: ruleS_Loop returns [EObject current=null] : ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Loop() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_0=null;
        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1377:2: ( ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) )
            // InternalGaml.g:1378:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1378:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            // InternalGaml.g:1379:3: ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) )
            {
            // InternalGaml.g:1379:3: ( (lv_key_0_0= 'loop' ) )
            // InternalGaml.g:1380:4: (lv_key_0_0= 'loop' )
            {
            // InternalGaml.g:1380:4: (lv_key_0_0= 'loop' )
            // InternalGaml.g:1381:5: lv_key_0_0= 'loop'
            {
            lv_key_0_0=(Token)match(input,27,FOLLOW_23); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_LoopAccess().getKeyLoopKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_LoopRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "loop");
              				
            }

            }


            }

            // InternalGaml.g:1393:3: ( (lv_name_1_0= RULE_ID ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==RULE_ID) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==RULE_ID||LA20_1==15||LA20_1==22||LA20_1==26||(LA20_1>=34 && LA20_1<=35)||LA20_1==40||(LA20_1>=109 && LA20_1<=133)) ) {
                    alt20=1;
                }
            }
            switch (alt20) {
                case 1 :
                    // InternalGaml.g:1394:4: (lv_name_1_0= RULE_ID )
                    {
                    // InternalGaml.g:1394:4: (lv_name_1_0= RULE_ID )
                    // InternalGaml.g:1395:5: lv_name_1_0= RULE_ID
                    {
                    lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_23); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_name_1_0, grammarAccess.getS_LoopAccess().getNameIDTerminalRuleCall_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_LoopRule());
                      					}
                      					setWithLastConsumed(
                      						current,
                      						"name",
                      						lv_name_1_0,
                      						"gaml.compiler.Gaml.ID");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:1411:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==RULE_ID||LA21_0==15||LA21_0==22||LA21_0==26||(LA21_0>=34 && LA21_0<=35)||(LA21_0>=109 && LA21_0<=133)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // InternalGaml.g:1412:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1412:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:1413:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_LoopAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_23);
            	    lv_facets_2_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_LoopRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_2_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            // InternalGaml.g:1430:3: ( (lv_block_3_0= ruleBlock ) )
            // InternalGaml.g:1431:4: (lv_block_3_0= ruleBlock )
            {
            // InternalGaml.g:1431:4: (lv_block_3_0= ruleBlock )
            // InternalGaml.g:1432:5: lv_block_3_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_LoopAccess().getBlockBlockParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_3_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_LoopRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_3_0,
              						"gaml.compiler.Gaml.Block");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Loop"


    // $ANTLR start "entryRuleS_If"
    // InternalGaml.g:1453:1: entryRuleS_If returns [EObject current=null] : iv_ruleS_If= ruleS_If EOF ;
    public final EObject entryRuleS_If() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_If = null;


        try {
            // InternalGaml.g:1453:45: (iv_ruleS_If= ruleS_If EOF )
            // InternalGaml.g:1454:2: iv_ruleS_If= ruleS_If EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_IfRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_If=ruleS_If();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_If; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_If"


    // $ANTLR start "ruleS_If"
    // InternalGaml.g:1460:1: ruleS_If returns [EObject current=null] : ( ( (lv_key_0_0= 'if' ) ) ( (lv_firstFacet_1_0= 'condition:' ) )? ( (lv_expr_2_0= ruleExpression ) ) ( (lv_block_3_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )? ) ;
    public final EObject ruleS_If() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_firstFacet_1_0=null;
        Token otherlv_4=null;
        EObject lv_expr_2_0 = null;

        EObject lv_block_3_0 = null;

        EObject lv_else_5_1 = null;

        EObject lv_else_5_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1466:2: ( ( ( (lv_key_0_0= 'if' ) ) ( (lv_firstFacet_1_0= 'condition:' ) )? ( (lv_expr_2_0= ruleExpression ) ) ( (lv_block_3_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )? ) )
            // InternalGaml.g:1467:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_firstFacet_1_0= 'condition:' ) )? ( (lv_expr_2_0= ruleExpression ) ) ( (lv_block_3_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )? )
            {
            // InternalGaml.g:1467:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_firstFacet_1_0= 'condition:' ) )? ( (lv_expr_2_0= ruleExpression ) ) ( (lv_block_3_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )? )
            // InternalGaml.g:1468:3: ( (lv_key_0_0= 'if' ) ) ( (lv_firstFacet_1_0= 'condition:' ) )? ( (lv_expr_2_0= ruleExpression ) ) ( (lv_block_3_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )?
            {
            // InternalGaml.g:1468:3: ( (lv_key_0_0= 'if' ) )
            // InternalGaml.g:1469:4: (lv_key_0_0= 'if' )
            {
            // InternalGaml.g:1469:4: (lv_key_0_0= 'if' )
            // InternalGaml.g:1470:5: lv_key_0_0= 'if'
            {
            lv_key_0_0=(Token)match(input,28,FOLLOW_24); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_IfAccess().getKeyIfKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_IfRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "if");
              				
            }

            }


            }

            // InternalGaml.g:1482:3: ( (lv_firstFacet_1_0= 'condition:' ) )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==29) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // InternalGaml.g:1483:4: (lv_firstFacet_1_0= 'condition:' )
                    {
                    // InternalGaml.g:1483:4: (lv_firstFacet_1_0= 'condition:' )
                    // InternalGaml.g:1484:5: lv_firstFacet_1_0= 'condition:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,29,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_IfAccess().getFirstFacetConditionKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_IfRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "condition:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:1496:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:1497:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:1497:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:1498:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_IfAccess().getExprExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_3);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_IfRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1515:3: ( (lv_block_3_0= ruleBlock ) )
            // InternalGaml.g:1516:4: (lv_block_3_0= ruleBlock )
            {
            // InternalGaml.g:1516:4: (lv_block_3_0= ruleBlock )
            // InternalGaml.g:1517:5: lv_block_3_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_IfAccess().getBlockBlockParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_25);
            lv_block_3_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_IfRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_3_0,
              						"gaml.compiler.Gaml.Block");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1534:3: ( ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) ) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==30) && (synpred13_InternalGaml())) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalGaml.g:1535:4: ( ( 'else' )=>otherlv_4= 'else' ) ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) )
                    {
                    // InternalGaml.g:1535:4: ( ( 'else' )=>otherlv_4= 'else' )
                    // InternalGaml.g:1536:5: ( 'else' )=>otherlv_4= 'else'
                    {
                    otherlv_4=(Token)match(input,30,FOLLOW_26); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getS_IfAccess().getElseKeyword_4_0());
                      				
                    }

                    }

                    // InternalGaml.g:1542:4: ( ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) ) )
                    // InternalGaml.g:1543:5: ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) )
                    {
                    // InternalGaml.g:1543:5: ( (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock ) )
                    // InternalGaml.g:1544:6: (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock )
                    {
                    // InternalGaml.g:1544:6: (lv_else_5_1= ruleS_If | lv_else_5_2= ruleBlock )
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==28) ) {
                        alt23=1;
                    }
                    else if ( (LA23_0==40) ) {
                        alt23=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 23, 0, input);

                        throw nvae;
                    }
                    switch (alt23) {
                        case 1 :
                            // InternalGaml.g:1545:7: lv_else_5_1= ruleS_If
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getS_IfAccess().getElseS_IfParserRuleCall_4_1_0_0());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_else_5_1=ruleS_If();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getS_IfRule());
                              							}
                              							set(
                              								current,
                              								"else",
                              								lv_else_5_1,
                              								"gaml.compiler.Gaml.S_If");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 2 :
                            // InternalGaml.g:1561:7: lv_else_5_2= ruleBlock
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getS_IfAccess().getElseBlockParserRuleCall_4_1_0_1());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_else_5_2=ruleBlock();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getS_IfRule());
                              							}
                              							set(
                              								current,
                              								"else",
                              								lv_else_5_2,
                              								"gaml.compiler.Gaml.Block");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;

                    }


                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_If"


    // $ANTLR start "entryRuleS_Try"
    // InternalGaml.g:1584:1: entryRuleS_Try returns [EObject current=null] : iv_ruleS_Try= ruleS_Try EOF ;
    public final EObject entryRuleS_Try() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Try = null;


        try {
            // InternalGaml.g:1584:46: (iv_ruleS_Try= ruleS_Try EOF )
            // InternalGaml.g:1585:2: iv_ruleS_Try= ruleS_Try EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_TryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Try=ruleS_Try();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Try; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Try"


    // $ANTLR start "ruleS_Try"
    // InternalGaml.g:1591:1: ruleS_Try returns [EObject current=null] : ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) ;
    public final EObject ruleS_Try() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_block_1_0 = null;

        EObject lv_catch_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1597:2: ( ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) )
            // InternalGaml.g:1598:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            {
            // InternalGaml.g:1598:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            // InternalGaml.g:1599:3: ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            {
            // InternalGaml.g:1599:3: ( (lv_key_0_0= 'try' ) )
            // InternalGaml.g:1600:4: (lv_key_0_0= 'try' )
            {
            // InternalGaml.g:1600:4: (lv_key_0_0= 'try' )
            // InternalGaml.g:1601:5: lv_key_0_0= 'try'
            {
            lv_key_0_0=(Token)match(input,31,FOLLOW_3); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_TryAccess().getKeyTryKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_TryRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "try");
              				
            }

            }


            }

            // InternalGaml.g:1613:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:1614:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:1614:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:1615:5: lv_block_1_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_TryAccess().getBlockBlockParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_27);
            lv_block_1_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_TryRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_1_0,
              						"gaml.compiler.Gaml.Block");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1632:3: ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==32) && (synpred14_InternalGaml())) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // InternalGaml.g:1633:4: ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) )
                    {
                    // InternalGaml.g:1633:4: ( ( 'catch' )=>otherlv_2= 'catch' )
                    // InternalGaml.g:1634:5: ( 'catch' )=>otherlv_2= 'catch'
                    {
                    otherlv_2=(Token)match(input,32,FOLLOW_3); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getS_TryAccess().getCatchKeyword_2_0());
                      				
                    }

                    }

                    // InternalGaml.g:1640:4: ( (lv_catch_3_0= ruleBlock ) )
                    // InternalGaml.g:1641:5: (lv_catch_3_0= ruleBlock )
                    {
                    // InternalGaml.g:1641:5: (lv_catch_3_0= ruleBlock )
                    // InternalGaml.g:1642:6: lv_catch_3_0= ruleBlock
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_TryAccess().getCatchBlockParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_catch_3_0=ruleBlock();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_TryRule());
                      						}
                      						set(
                      							current,
                      							"catch",
                      							lv_catch_3_0,
                      							"gaml.compiler.Gaml.Block");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Try"


    // $ANTLR start "entryRuleS_Other"
    // InternalGaml.g:1664:1: entryRuleS_Other returns [EObject current=null] : iv_ruleS_Other= ruleS_Other EOF ;
    public final EObject entryRuleS_Other() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Other = null;


        try {
            // InternalGaml.g:1664:48: (iv_ruleS_Other= ruleS_Other EOF )
            // InternalGaml.g:1665:2: iv_ruleS_Other= ruleS_Other EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_OtherRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Other=ruleS_Other();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Other; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Other"


    // $ANTLR start "ruleS_Other"
    // InternalGaml.g:1671:1: ruleS_Other returns [EObject current=null] : ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Other() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:1677:2: ( ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1678:2: ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1678:2: ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1679:3: ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1679:3: ( (lv_key_0_0= ruleValid_ID ) )
            // InternalGaml.g:1680:4: (lv_key_0_0= ruleValid_ID )
            {
            // InternalGaml.g:1680:4: (lv_key_0_0= ruleValid_ID )
            // InternalGaml.g:1681:5: lv_key_0_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherAccess().getKeyValid_IDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_key_0_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_OtherRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_OtherRule());
              			}
              			newCompositeNode(grammarAccess.getS_OtherAccess().getFacetsAndBlockParserRuleCall_1());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_1=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_1;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Other"


    // $ANTLR start "entryRuleS_Return"
    // InternalGaml.g:1713:1: entryRuleS_Return returns [EObject current=null] : iv_ruleS_Return= ruleS_Return EOF ;
    public final EObject entryRuleS_Return() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Return = null;


        try {
            // InternalGaml.g:1713:49: (iv_ruleS_Return= ruleS_Return EOF )
            // InternalGaml.g:1714:2: iv_ruleS_Return= ruleS_Return EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_ReturnRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Return=ruleS_Return();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Return; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Return"


    // $ANTLR start "ruleS_Return"
    // InternalGaml.g:1720:1: ruleS_Return returns [EObject current=null] : ( ( (lv_key_0_0= 'return' ) ) ( (lv_firstFacet_1_0= 'value:' ) )? ( (lv_expr_2_0= ruleExpression ) )? otherlv_3= ';' ) ;
    public final EObject ruleS_Return() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_firstFacet_1_0=null;
        Token otherlv_3=null;
        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1726:2: ( ( ( (lv_key_0_0= 'return' ) ) ( (lv_firstFacet_1_0= 'value:' ) )? ( (lv_expr_2_0= ruleExpression ) )? otherlv_3= ';' ) )
            // InternalGaml.g:1727:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_firstFacet_1_0= 'value:' ) )? ( (lv_expr_2_0= ruleExpression ) )? otherlv_3= ';' )
            {
            // InternalGaml.g:1727:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_firstFacet_1_0= 'value:' ) )? ( (lv_expr_2_0= ruleExpression ) )? otherlv_3= ';' )
            // InternalGaml.g:1728:3: ( (lv_key_0_0= 'return' ) ) ( (lv_firstFacet_1_0= 'value:' ) )? ( (lv_expr_2_0= ruleExpression ) )? otherlv_3= ';'
            {
            // InternalGaml.g:1728:3: ( (lv_key_0_0= 'return' ) )
            // InternalGaml.g:1729:4: (lv_key_0_0= 'return' )
            {
            // InternalGaml.g:1729:4: (lv_key_0_0= 'return' )
            // InternalGaml.g:1730:5: lv_key_0_0= 'return'
            {
            lv_key_0_0=(Token)match(input,33,FOLLOW_28); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_ReturnAccess().getKeyReturnKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_ReturnRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "return");
              				
            }

            }


            }

            // InternalGaml.g:1742:3: ( (lv_firstFacet_1_0= 'value:' ) )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==34) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // InternalGaml.g:1743:4: (lv_firstFacet_1_0= 'value:' )
                    {
                    // InternalGaml.g:1743:4: (lv_firstFacet_1_0= 'value:' )
                    // InternalGaml.g:1744:5: lv_firstFacet_1_0= 'value:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,34,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_ReturnAccess().getFirstFacetValueKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_ReturnRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "value:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:1756:3: ( (lv_expr_2_0= ruleExpression ) )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( ((LA27_0>=RULE_ID && LA27_0<=RULE_KEYWORD)||LA27_0==20||LA27_0==37||LA27_0==40||LA27_0==45||(LA27_0>=47 && LA27_0<=49)||(LA27_0>=53 && LA27_0<=101)||LA27_0==143||(LA27_0>=147 && LA27_0<=149)) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalGaml.g:1757:4: (lv_expr_2_0= ruleExpression )
                    {
                    // InternalGaml.g:1757:4: (lv_expr_2_0= ruleExpression )
                    // InternalGaml.g:1758:5: lv_expr_2_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReturnAccess().getExprExpressionParserRuleCall_2_0());
                      				
                    }
                    pushFollow(FOLLOW_30);
                    lv_expr_2_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getS_ReturnRule());
                      					}
                      					set(
                      						current,
                      						"expr",
                      						lv_expr_2_0,
                      						"gaml.compiler.Gaml.Expression");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_3=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getS_ReturnAccess().getSemicolonKeyword_3());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Return"


    // $ANTLR start "entryRuleS_Declaration"
    // InternalGaml.g:1783:1: entryRuleS_Declaration returns [EObject current=null] : iv_ruleS_Declaration= ruleS_Declaration EOF ;
    public final EObject entryRuleS_Declaration() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Declaration = null;


        try {
            // InternalGaml.g:1783:54: (iv_ruleS_Declaration= ruleS_Declaration EOF )
            // InternalGaml.g:1784:2: iv_ruleS_Declaration= ruleS_Declaration EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_DeclarationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Declaration=ruleS_Declaration();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Declaration; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Declaration"


    // $ANTLR start "ruleS_Declaration"
    // InternalGaml.g:1790:1: ruleS_Declaration returns [EObject current=null] : ( ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Var_4= ruleS_Var | this_S_Loop_5= ruleS_Loop ) ;
    public final EObject ruleS_Declaration() throws RecognitionException {
        EObject current = null;

        EObject this_S_Definition_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Reflex_2 = null;

        EObject this_S_Action_3 = null;

        EObject this_S_Var_4 = null;

        EObject this_S_Loop_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:1796:2: ( ( ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Var_4= ruleS_Var | this_S_Loop_5= ruleS_Loop ) )
            // InternalGaml.g:1797:2: ( ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Var_4= ruleS_Var | this_S_Loop_5= ruleS_Loop )
            {
            // InternalGaml.g:1797:2: ( ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Var_4= ruleS_Var | this_S_Loop_5= ruleS_Loop )
            int alt28=6;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1 :
                    // InternalGaml.g:1798:3: ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                    {
                    // InternalGaml.g:1798:3: ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                    // InternalGaml.g:1799:4: ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_DefinitionParserRuleCall_0());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_0=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Definition_0;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:1810:3: this_S_Species_1= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_SpeciesParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Species_1=ruleS_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Species_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:1819:3: this_S_Reflex_2= ruleS_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_ReflexParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Reflex_2=ruleS_Reflex();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Reflex_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:1828:3: this_S_Action_3= ruleS_Action
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_ActionParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Action_3=ruleS_Action();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Action_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:1837:3: this_S_Var_4= ruleS_Var
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_VarParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Var_4=ruleS_Var();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Var_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:1846:3: this_S_Loop_5= ruleS_Loop
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_LoopParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Loop_5=ruleS_Loop();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Loop_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Declaration"


    // $ANTLR start "entryRuleS_Reflex"
    // InternalGaml.g:1858:1: entryRuleS_Reflex returns [EObject current=null] : iv_ruleS_Reflex= ruleS_Reflex EOF ;
    public final EObject entryRuleS_Reflex() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Reflex = null;


        try {
            // InternalGaml.g:1858:49: (iv_ruleS_Reflex= ruleS_Reflex EOF )
            // InternalGaml.g:1859:2: iv_ruleS_Reflex= ruleS_Reflex EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_ReflexRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Reflex=ruleS_Reflex();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Reflex; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Reflex"


    // $ANTLR start "ruleS_Reflex"
    // InternalGaml.g:1865:1: ruleS_Reflex returns [EObject current=null] : ( ( (lv_key_0_0= rule_ReflexKey ) ) ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )? (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )? ( (lv_block_6_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Reflex() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject lv_expr_5_0 = null;

        EObject lv_block_6_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1871:2: ( ( ( (lv_key_0_0= rule_ReflexKey ) ) ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )? (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )? ( (lv_block_6_0= ruleBlock ) ) ) )
            // InternalGaml.g:1872:2: ( ( (lv_key_0_0= rule_ReflexKey ) ) ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )? (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )? ( (lv_block_6_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1872:2: ( ( (lv_key_0_0= rule_ReflexKey ) ) ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )? (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )? ( (lv_block_6_0= ruleBlock ) ) )
            // InternalGaml.g:1873:3: ( (lv_key_0_0= rule_ReflexKey ) ) ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )? (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )? ( (lv_block_6_0= ruleBlock ) )
            {
            // InternalGaml.g:1873:3: ( (lv_key_0_0= rule_ReflexKey ) )
            // InternalGaml.g:1874:4: (lv_key_0_0= rule_ReflexKey )
            {
            // InternalGaml.g:1874:4: (lv_key_0_0= rule_ReflexKey )
            // InternalGaml.g:1875:5: lv_key_0_0= rule_ReflexKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ReflexAccess().getKey_ReflexKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_31);
            lv_key_0_0=rule_ReflexKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ReflexRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._ReflexKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1892:3: ( ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) ) )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==RULE_ID||LA30_0==22||LA30_0==45||(LA30_0>=47 && LA30_0<=49)||(LA30_0>=53 && LA30_0<=101)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // InternalGaml.g:1893:4: ( (lv_firstFacet_1_0= 'name:' ) )? ( (lv_name_2_0= ruleValid_ID ) )
                    {
                    // InternalGaml.g:1893:4: ( (lv_firstFacet_1_0= 'name:' ) )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==22) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // InternalGaml.g:1894:5: (lv_firstFacet_1_0= 'name:' )
                            {
                            // InternalGaml.g:1894:5: (lv_firstFacet_1_0= 'name:' )
                            // InternalGaml.g:1895:6: lv_firstFacet_1_0= 'name:'
                            {
                            lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_7); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_ReflexAccess().getFirstFacetNameKeyword_1_0_0());
                              					
                            }
                            if ( state.backtracking==0 ) {

                              						if (current==null) {
                              							current = createModelElement(grammarAccess.getS_ReflexRule());
                              						}
                              						setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                              					
                            }

                            }


                            }
                            break;

                    }

                    // InternalGaml.g:1907:4: ( (lv_name_2_0= ruleValid_ID ) )
                    // InternalGaml.g:1908:5: (lv_name_2_0= ruleValid_ID )
                    {
                    // InternalGaml.g:1908:5: (lv_name_2_0= ruleValid_ID )
                    // InternalGaml.g:1909:6: lv_name_2_0= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getNameValid_IDParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_32);
                    lv_name_2_0=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_2_0,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalGaml.g:1927:3: (otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) ) )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==35) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // InternalGaml.g:1928:4: otherlv_3= 'when' otherlv_4= ':' ( (lv_expr_5_0= ruleExpression ) )
                    {
                    otherlv_3=(Token)match(input,35,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_ReflexAccess().getWhenKeyword_2_0());
                      			
                    }
                    otherlv_4=(Token)match(input,36,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getS_ReflexAccess().getColonKeyword_2_1());
                      			
                    }
                    // InternalGaml.g:1936:4: ( (lv_expr_5_0= ruleExpression ) )
                    // InternalGaml.g:1937:5: (lv_expr_5_0= ruleExpression )
                    {
                    // InternalGaml.g:1937:5: (lv_expr_5_0= ruleExpression )
                    // InternalGaml.g:1938:6: lv_expr_5_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getExprExpressionParserRuleCall_2_2_0());
                      					
                    }
                    pushFollow(FOLLOW_3);
                    lv_expr_5_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_5_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalGaml.g:1956:3: ( (lv_block_6_0= ruleBlock ) )
            // InternalGaml.g:1957:4: (lv_block_6_0= ruleBlock )
            {
            // InternalGaml.g:1957:4: (lv_block_6_0= ruleBlock )
            // InternalGaml.g:1958:5: lv_block_6_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ReflexAccess().getBlockBlockParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_6_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ReflexRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_6_0,
              						"gaml.compiler.Gaml.Block");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Reflex"


    // $ANTLR start "entryRuleS_Definition"
    // InternalGaml.g:1979:1: entryRuleS_Definition returns [EObject current=null] : iv_ruleS_Definition= ruleS_Definition EOF ;
    public final EObject entryRuleS_Definition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Definition = null;


        try {
            // InternalGaml.g:1979:53: (iv_ruleS_Definition= ruleS_Definition EOF )
            // InternalGaml.g:1980:2: iv_ruleS_Definition= ruleS_Definition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_DefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Definition=ruleS_Definition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Definition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Definition"


    // $ANTLR start "ruleS_Definition"
    // InternalGaml.g:1986:1: ruleS_Definition returns [EObject current=null] : ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Definition() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        Token lv_name_2_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_tkey_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_1 = null;

        EObject lv_args_4_0 = null;

        EObject this_FacetsAndBlock_6 = null;



        	enterRule();

        try {
            // InternalGaml.g:1992:2: ( ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1993:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1993:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1994:3: ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1994:3: ( (lv_tkey_0_0= ruleTypeRef ) )
            // InternalGaml.g:1995:4: (lv_tkey_0_0= ruleTypeRef )
            {
            // InternalGaml.g:1995:4: (lv_tkey_0_0= ruleTypeRef )
            // InternalGaml.g:1996:5: lv_tkey_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DefinitionAccess().getTkeyTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_16);
            lv_tkey_0_0=ruleTypeRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
              					}
              					set(
              						current,
              						"tkey",
              						lv_tkey_0_0,
              						"gaml.compiler.Gaml.TypeRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2013:3: ( (lv_firstFacet_1_0= 'name:' ) )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==22) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // InternalGaml.g:2014:4: (lv_firstFacet_1_0= 'name:' )
                    {
                    // InternalGaml.g:2014:4: (lv_firstFacet_1_0= 'name:' )
                    // InternalGaml.g:2015:5: lv_firstFacet_1_0= 'name:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_DefinitionAccess().getFirstFacetNameKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_DefinitionRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:2027:3: ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) )
            // InternalGaml.g:2028:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            {
            // InternalGaml.g:2028:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            // InternalGaml.g:2029:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            {
            // InternalGaml.g:2029:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==RULE_ID||LA33_0==45||(LA33_0>=47 && LA33_0<=49)||(LA33_0>=53 && LA33_0<=101)) ) {
                alt33=1;
            }
            else if ( (LA33_0==RULE_STRING) ) {
                alt33=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // InternalGaml.g:2030:6: lv_name_2_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DefinitionAccess().getNameValid_IDParserRuleCall_2_0_0());
                      					
                    }
                    pushFollow(FOLLOW_34);
                    lv_name_2_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_2_1,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2046:6: lv_name_2_2= RULE_STRING
                    {
                    lv_name_2_2=(Token)match(input,RULE_STRING,FOLLOW_34); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_2_2, grammarAccess.getS_DefinitionAccess().getNameSTRINGTerminalRuleCall_2_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DefinitionRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"name",
                      							lv_name_2_2,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:2063:3: (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==37) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // InternalGaml.g:2064:4: otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')'
                    {
                    otherlv_3=(Token)match(input,37,FOLLOW_35); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_DefinitionAccess().getLeftParenthesisKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:2068:4: ( (lv_args_4_0= ruleActionArguments ) )
                    // InternalGaml.g:2069:5: (lv_args_4_0= ruleActionArguments )
                    {
                    // InternalGaml.g:2069:5: (lv_args_4_0= ruleActionArguments )
                    // InternalGaml.g:2070:6: lv_args_4_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DefinitionAccess().getArgsActionArgumentsParserRuleCall_3_1_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_args_4_0=ruleActionArguments();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
                      						}
                      						set(
                      							current,
                      							"args",
                      							lv_args_4_0,
                      							"gaml.compiler.Gaml.ActionArguments");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_5=(Token)match(input,38,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getS_DefinitionAccess().getRightParenthesisKeyword_3_2());
                      			
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_DefinitionRule());
              			}
              			newCompositeNode(grammarAccess.getS_DefinitionAccess().getFacetsAndBlockParserRuleCall_4());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_6=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_6;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Definition"


    // $ANTLR start "entryRuleS_Action"
    // InternalGaml.g:2107:1: entryRuleS_Action returns [EObject current=null] : iv_ruleS_Action= ruleS_Action EOF ;
    public final EObject entryRuleS_Action() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Action = null;


        try {
            // InternalGaml.g:2107:49: (iv_ruleS_Action= ruleS_Action EOF )
            // InternalGaml.g:2108:2: iv_ruleS_Action= ruleS_Action EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_ActionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Action=ruleS_Action();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Action; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Action"


    // $ANTLR start "ruleS_Action"
    // InternalGaml.g:2114:1: ruleS_Action returns [EObject current=null] : ( () ( (lv_key_1_0= 'action' ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )? this_FacetsAndBlock_7= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Action() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        Token lv_firstFacet_2_0=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;

        EObject lv_args_5_0 = null;

        EObject this_FacetsAndBlock_7 = null;



        	enterRule();

        try {
            // InternalGaml.g:2120:2: ( ( () ( (lv_key_1_0= 'action' ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )? this_FacetsAndBlock_7= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2121:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )? this_FacetsAndBlock_7= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2121:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )? this_FacetsAndBlock_7= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2122:3: () ( (lv_key_1_0= 'action' ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )? this_FacetsAndBlock_7= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2122:3: ()
            // InternalGaml.g:2123:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getS_ActionAccess().getS_ActionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:2129:3: ( (lv_key_1_0= 'action' ) )
            // InternalGaml.g:2130:4: (lv_key_1_0= 'action' )
            {
            // InternalGaml.g:2130:4: (lv_key_1_0= 'action' )
            // InternalGaml.g:2131:5: lv_key_1_0= 'action'
            {
            lv_key_1_0=(Token)match(input,39,FOLLOW_37); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_1_0, grammarAccess.getS_ActionAccess().getKeyActionKeyword_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_ActionRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_1_0, "action");
              				
            }

            }


            }

            // InternalGaml.g:2143:3: ( (lv_firstFacet_2_0= 'name:' ) )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==22) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // InternalGaml.g:2144:4: (lv_firstFacet_2_0= 'name:' )
                    {
                    // InternalGaml.g:2144:4: (lv_firstFacet_2_0= 'name:' )
                    // InternalGaml.g:2145:5: lv_firstFacet_2_0= 'name:'
                    {
                    lv_firstFacet_2_0=(Token)match(input,22,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_2_0, grammarAccess.getS_ActionAccess().getFirstFacetNameKeyword_2_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_ActionRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_2_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:2157:3: ( (lv_name_3_0= ruleValid_ID ) )
            // InternalGaml.g:2158:4: (lv_name_3_0= ruleValid_ID )
            {
            // InternalGaml.g:2158:4: (lv_name_3_0= ruleValid_ID )
            // InternalGaml.g:2159:5: lv_name_3_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ActionAccess().getNameValid_IDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_34);
            lv_name_3_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ActionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_3_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2176:3: (otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')' )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==37) ) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // InternalGaml.g:2177:4: otherlv_4= '(' ( (lv_args_5_0= ruleActionArguments ) ) otherlv_6= ')'
                    {
                    otherlv_4=(Token)match(input,37,FOLLOW_35); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getS_ActionAccess().getLeftParenthesisKeyword_4_0());
                      			
                    }
                    // InternalGaml.g:2181:4: ( (lv_args_5_0= ruleActionArguments ) )
                    // InternalGaml.g:2182:5: (lv_args_5_0= ruleActionArguments )
                    {
                    // InternalGaml.g:2182:5: (lv_args_5_0= ruleActionArguments )
                    // InternalGaml.g:2183:6: lv_args_5_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ActionAccess().getArgsActionArgumentsParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_args_5_0=ruleActionArguments();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ActionRule());
                      						}
                      						set(
                      							current,
                      							"args",
                      							lv_args_5_0,
                      							"gaml.compiler.Gaml.ActionArguments");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_6=(Token)match(input,38,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getS_ActionAccess().getRightParenthesisKeyword_4_2());
                      			
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_ActionRule());
              			}
              			newCompositeNode(grammarAccess.getS_ActionAccess().getFacetsAndBlockParserRuleCall_5());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_7=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_7;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Action"


    // $ANTLR start "entryRuleS_Var"
    // InternalGaml.g:2220:1: entryRuleS_Var returns [EObject current=null] : iv_ruleS_Var= ruleS_Var EOF ;
    public final EObject entryRuleS_Var() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Var = null;


        try {
            // InternalGaml.g:2220:46: (iv_ruleS_Var= ruleS_Var EOF )
            // InternalGaml.g:2221:2: iv_ruleS_Var= ruleS_Var EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_VarRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Var=ruleS_Var();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Var; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Var"


    // $ANTLR start "ruleS_Var"
    // InternalGaml.g:2227:1: ruleS_Var returns [EObject current=null] : ( () ( (lv_key_1_0= rule_VarOrConstKey ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) ( (lv_facets_4_0= ruleFacet ) )* otherlv_5= ';' ) ;
    public final EObject ruleS_Var() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_2_0=null;
        Token otherlv_5=null;
        AntlrDatatypeRuleToken lv_key_1_0 = null;

        AntlrDatatypeRuleToken lv_name_3_0 = null;

        EObject lv_facets_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2233:2: ( ( () ( (lv_key_1_0= rule_VarOrConstKey ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) ( (lv_facets_4_0= ruleFacet ) )* otherlv_5= ';' ) )
            // InternalGaml.g:2234:2: ( () ( (lv_key_1_0= rule_VarOrConstKey ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) ( (lv_facets_4_0= ruleFacet ) )* otherlv_5= ';' )
            {
            // InternalGaml.g:2234:2: ( () ( (lv_key_1_0= rule_VarOrConstKey ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) ( (lv_facets_4_0= ruleFacet ) )* otherlv_5= ';' )
            // InternalGaml.g:2235:3: () ( (lv_key_1_0= rule_VarOrConstKey ) ) ( (lv_firstFacet_2_0= 'name:' ) )? ( (lv_name_3_0= ruleValid_ID ) ) ( (lv_facets_4_0= ruleFacet ) )* otherlv_5= ';'
            {
            // InternalGaml.g:2235:3: ()
            // InternalGaml.g:2236:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getS_VarAccess().getS_VarAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:2242:3: ( (lv_key_1_0= rule_VarOrConstKey ) )
            // InternalGaml.g:2243:4: (lv_key_1_0= rule_VarOrConstKey )
            {
            // InternalGaml.g:2243:4: (lv_key_1_0= rule_VarOrConstKey )
            // InternalGaml.g:2244:5: lv_key_1_0= rule_VarOrConstKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_VarAccess().getKey_VarOrConstKeyParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_37);
            lv_key_1_0=rule_VarOrConstKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_VarRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_1_0,
              						"gaml.compiler.Gaml._VarOrConstKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2261:3: ( (lv_firstFacet_2_0= 'name:' ) )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==22) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // InternalGaml.g:2262:4: (lv_firstFacet_2_0= 'name:' )
                    {
                    // InternalGaml.g:2262:4: (lv_firstFacet_2_0= 'name:' )
                    // InternalGaml.g:2263:5: lv_firstFacet_2_0= 'name:'
                    {
                    lv_firstFacet_2_0=(Token)match(input,22,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_2_0, grammarAccess.getS_VarAccess().getFirstFacetNameKeyword_2_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_VarRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_2_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:2275:3: ( (lv_name_3_0= ruleValid_ID ) )
            // InternalGaml.g:2276:4: (lv_name_3_0= ruleValid_ID )
            {
            // InternalGaml.g:2276:4: (lv_name_3_0= ruleValid_ID )
            // InternalGaml.g:2277:5: lv_name_3_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_VarAccess().getNameValid_IDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_38);
            lv_name_3_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_VarRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_3_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2294:3: ( (lv_facets_4_0= ruleFacet ) )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==RULE_ID||LA38_0==15||LA38_0==22||LA38_0==26||(LA38_0>=34 && LA38_0<=35)||(LA38_0>=109 && LA38_0<=133)) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // InternalGaml.g:2295:4: (lv_facets_4_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2295:4: (lv_facets_4_0= ruleFacet )
            	    // InternalGaml.g:2296:5: lv_facets_4_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_VarAccess().getFacetsFacetParserRuleCall_4_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_38);
            	    lv_facets_4_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_VarRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_4_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);

            otherlv_5=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_5, grammarAccess.getS_VarAccess().getSemicolonKeyword_5());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Var"


    // $ANTLR start "entryRuleS_Assignment"
    // InternalGaml.g:2321:1: entryRuleS_Assignment returns [EObject current=null] : iv_ruleS_Assignment= ruleS_Assignment EOF ;
    public final EObject entryRuleS_Assignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Assignment = null;


        try {
            // InternalGaml.g:2321:53: (iv_ruleS_Assignment= ruleS_Assignment EOF )
            // InternalGaml.g:2322:2: iv_ruleS_Assignment= ruleS_Assignment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_AssignmentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Assignment=ruleS_Assignment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Assignment; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Assignment"


    // $ANTLR start "ruleS_Assignment"
    // InternalGaml.g:2328:1: ruleS_Assignment returns [EObject current=null] : ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) ;
    public final EObject ruleS_Assignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_4=null;
        EObject lv_expr_0_0 = null;

        AntlrDatatypeRuleToken lv_key_1_0 = null;

        EObject lv_value_2_0 = null;

        EObject lv_facets_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2334:2: ( ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) )
            // InternalGaml.g:2335:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            {
            // InternalGaml.g:2335:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            // InternalGaml.g:2336:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';'
            {
            // InternalGaml.g:2336:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* )
            // InternalGaml.g:2337:4: ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= rule_AssignmentKey ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )*
            {
            // InternalGaml.g:2337:4: ( (lv_expr_0_0= ruleExpression ) )
            // InternalGaml.g:2338:5: (lv_expr_0_0= ruleExpression )
            {
            // InternalGaml.g:2338:5: (lv_expr_0_0= ruleExpression )
            // InternalGaml.g:2339:6: lv_expr_0_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getExprExpressionParserRuleCall_0_0_0());
              					
            }
            pushFollow(FOLLOW_39);
            lv_expr_0_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              						if (current==null) {
              							current = createModelElementForParent(grammarAccess.getS_AssignmentRule());
              						}
              						set(
              							current,
              							"expr",
              							lv_expr_0_0,
              							"gaml.compiler.Gaml.Expression");
              						afterParserOrEnumRuleCall();
              					
            }

            }


            }

            // InternalGaml.g:2356:4: ( (lv_key_1_0= rule_AssignmentKey ) )
            // InternalGaml.g:2357:5: (lv_key_1_0= rule_AssignmentKey )
            {
            // InternalGaml.g:2357:5: (lv_key_1_0= rule_AssignmentKey )
            // InternalGaml.g:2358:6: lv_key_1_0= rule_AssignmentKey
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getKey_AssignmentKeyParserRuleCall_0_1_0());
              					
            }
            pushFollow(FOLLOW_5);
            lv_key_1_0=rule_AssignmentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              						if (current==null) {
              							current = createModelElementForParent(grammarAccess.getS_AssignmentRule());
              						}
              						set(
              							current,
              							"key",
              							lv_key_1_0,
              							"gaml.compiler.Gaml._AssignmentKey");
              						afterParserOrEnumRuleCall();
              					
            }

            }


            }

            // InternalGaml.g:2375:4: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2376:5: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2376:5: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2377:6: lv_value_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getValueExpressionParserRuleCall_0_2_0());
              					
            }
            pushFollow(FOLLOW_38);
            lv_value_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              						if (current==null) {
              							current = createModelElementForParent(grammarAccess.getS_AssignmentRule());
              						}
              						set(
              							current,
              							"value",
              							lv_value_2_0,
              							"gaml.compiler.Gaml.Expression");
              						afterParserOrEnumRuleCall();
              					
            }

            }


            }

            // InternalGaml.g:2394:4: ( (lv_facets_3_0= ruleFacet ) )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==RULE_ID||LA39_0==15||LA39_0==22||LA39_0==26||(LA39_0>=34 && LA39_0<=35)||(LA39_0>=109 && LA39_0<=133)) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // InternalGaml.g:2395:5: (lv_facets_3_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2395:5: (lv_facets_3_0= ruleFacet )
            	    // InternalGaml.g:2396:6: lv_facets_3_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getS_AssignmentAccess().getFacetsFacetParserRuleCall_0_3_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_38);
            	    lv_facets_3_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getS_AssignmentRule());
            	      						}
            	      						add(
            	      							current,
            	      							"facets",
            	      							lv_facets_3_0,
            	      							"gaml.compiler.Gaml.Facet");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);


            }

            otherlv_4=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getS_AssignmentAccess().getSemicolonKeyword_1());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Assignment"


    // $ANTLR start "entryRuleS_Equations"
    // InternalGaml.g:2422:1: entryRuleS_Equations returns [EObject current=null] : iv_ruleS_Equations= ruleS_Equations EOF ;
    public final EObject entryRuleS_Equations() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equations = null;


        try {
            // InternalGaml.g:2422:52: (iv_ruleS_Equations= ruleS_Equations EOF )
            // InternalGaml.g:2423:2: iv_ruleS_Equations= ruleS_Equations EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_EquationsRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Equations=ruleS_Equations();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Equations; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Equations"


    // $ANTLR start "ruleS_Equations"
    // InternalGaml.g:2429:1: ruleS_Equations returns [EObject current=null] : ( ( (lv_key_0_0= rule_EquationsKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) ;
    public final EObject ruleS_Equations() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_facets_2_0 = null;

        EObject lv_equations_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2435:2: ( ( ( (lv_key_0_0= rule_EquationsKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) )
            // InternalGaml.g:2436:2: ( ( (lv_key_0_0= rule_EquationsKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            {
            // InternalGaml.g:2436:2: ( ( (lv_key_0_0= rule_EquationsKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            // InternalGaml.g:2437:3: ( (lv_key_0_0= rule_EquationsKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            {
            // InternalGaml.g:2437:3: ( (lv_key_0_0= rule_EquationsKey ) )
            // InternalGaml.g:2438:4: (lv_key_0_0= rule_EquationsKey )
            {
            // InternalGaml.g:2438:4: (lv_key_0_0= rule_EquationsKey )
            // InternalGaml.g:2439:5: lv_key_0_0= rule_EquationsKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationsAccess().getKey_EquationsKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_key_0_0=rule_EquationsKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_EquationsRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._EquationsKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2456:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:2457:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:2457:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:2458:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationsAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_19);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_EquationsRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2475:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==RULE_ID||LA40_0==15||LA40_0==22||LA40_0==26||(LA40_0>=34 && LA40_0<=35)||(LA40_0>=109 && LA40_0<=133)) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // InternalGaml.g:2476:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2476:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2477:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_EquationsAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
            	    lv_facets_2_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_EquationsRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_2_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);

            // InternalGaml.g:2494:3: ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==40) ) {
                alt42=1;
            }
            else if ( (LA42_0==24) ) {
                alt42=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // InternalGaml.g:2495:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    {
                    // InternalGaml.g:2495:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    // InternalGaml.g:2496:5: otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}'
                    {
                    otherlv_3=(Token)match(input,40,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0());
                      				
                    }
                    // InternalGaml.g:2500:5: ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )*
                    loop41:
                    do {
                        int alt41=2;
                        int LA41_0 = input.LA(1);

                        if ( (LA41_0==RULE_ID||LA41_0==45||(LA41_0>=47 && LA41_0<=49)||(LA41_0>=53 && LA41_0<=101)) ) {
                            alt41=1;
                        }


                        switch (alt41) {
                    	case 1 :
                    	    // InternalGaml.g:2501:6: ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';'
                    	    {
                    	    // InternalGaml.g:2501:6: ( (lv_equations_4_0= ruleS_Equation ) )
                    	    // InternalGaml.g:2502:7: (lv_equations_4_0= ruleS_Equation )
                    	    {
                    	    // InternalGaml.g:2502:7: (lv_equations_4_0= ruleS_Equation )
                    	    // InternalGaml.g:2503:8: lv_equations_4_0= ruleS_Equation
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      								newCompositeNode(grammarAccess.getS_EquationsAccess().getEquationsS_EquationParserRuleCall_3_0_1_0_0());
                    	      							
                    	    }
                    	    pushFollow(FOLLOW_30);
                    	    lv_equations_4_0=ruleS_Equation();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      								if (current==null) {
                    	      									current = createModelElementForParent(grammarAccess.getS_EquationsRule());
                    	      								}
                    	      								add(
                    	      									current,
                    	      									"equations",
                    	      									lv_equations_4_0,
                    	      									"gaml.compiler.Gaml.S_Equation");
                    	      								afterParserOrEnumRuleCall();
                    	      							
                    	    }

                    	    }


                    	    }

                    	    otherlv_5=(Token)match(input,24,FOLLOW_40); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						newLeafNode(otherlv_5, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_0_1_1());
                    	      					
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);

                    otherlv_6=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_6, grammarAccess.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2());
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:2531:4: otherlv_7= ';'
                    {
                    otherlv_7=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_1());
                      			
                    }

                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Equations"


    // $ANTLR start "entryRuleS_Equation"
    // InternalGaml.g:2540:1: entryRuleS_Equation returns [EObject current=null] : iv_ruleS_Equation= ruleS_Equation EOF ;
    public final EObject entryRuleS_Equation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equation = null;


        try {
            // InternalGaml.g:2540:51: (iv_ruleS_Equation= ruleS_Equation EOF )
            // InternalGaml.g:2541:2: iv_ruleS_Equation= ruleS_Equation EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_EquationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Equation=ruleS_Equation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Equation; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Equation"


    // $ANTLR start "ruleS_Equation"
    // InternalGaml.g:2547:1: ruleS_Equation returns [EObject current=null] : ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) ;
    public final EObject ruleS_Equation() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        EObject lv_expr_0_1 = null;

        EObject lv_expr_0_2 = null;

        EObject lv_value_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2553:2: ( ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:2554:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:2554:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            // InternalGaml.g:2555:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) )
            {
            // InternalGaml.g:2555:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) )
            // InternalGaml.g:2556:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            {
            // InternalGaml.g:2556:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            // InternalGaml.g:2557:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            {
            // InternalGaml.g:2557:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            int alt43=2;
            alt43 = dfa43.predict(input);
            switch (alt43) {
                case 1 :
                    // InternalGaml.g:2558:6: lv_expr_0_1= ruleFunction
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprFunctionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_expr_0_1=ruleFunction();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_EquationRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_0_1,
                      							"gaml.compiler.Gaml.Function");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2574:6: lv_expr_0_2= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprVariableRefParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_expr_0_2=ruleVariableRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_EquationRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_0_2,
                      							"gaml.compiler.Gaml.VariableRef");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:2592:3: ( (lv_key_1_0= '=' ) )
            // InternalGaml.g:2593:4: (lv_key_1_0= '=' )
            {
            // InternalGaml.g:2593:4: (lv_key_1_0= '=' )
            // InternalGaml.g:2594:5: lv_key_1_0= '='
            {
            lv_key_1_0=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_1_0, grammarAccess.getS_EquationAccess().getKeyEqualsSignKeyword_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_EquationRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_1_0, "=");
              				
            }

            }


            }

            // InternalGaml.g:2606:3: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2607:4: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2607:4: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2608:5: lv_value_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationAccess().getValueExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_value_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_EquationRule());
              					}
              					set(
              						current,
              						"value",
              						lv_value_2_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Equation"


    // $ANTLR start "entryRuleS_Solve"
    // InternalGaml.g:2629:1: entryRuleS_Solve returns [EObject current=null] : iv_ruleS_Solve= ruleS_Solve EOF ;
    public final EObject entryRuleS_Solve() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Solve = null;


        try {
            // InternalGaml.g:2629:48: (iv_ruleS_Solve= ruleS_Solve EOF )
            // InternalGaml.g:2630:2: iv_ruleS_Solve= ruleS_Solve EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_SolveRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Solve=ruleS_Solve();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Solve; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Solve"


    // $ANTLR start "ruleS_Solve"
    // InternalGaml.g:2636:1: ruleS_Solve returns [EObject current=null] : ( ( (lv_key_0_0= rule_SolveKey ) ) ( (lv_firstFacet_1_0= 'equation:' ) )? ( (lv_expr_2_0= ruleEquationRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Solve() throws RecognitionException {
        EObject current = null;

        Token lv_firstFacet_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_2_0 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:2642:2: ( ( ( (lv_key_0_0= rule_SolveKey ) ) ( (lv_firstFacet_1_0= 'equation:' ) )? ( (lv_expr_2_0= ruleEquationRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2643:2: ( ( (lv_key_0_0= rule_SolveKey ) ) ( (lv_firstFacet_1_0= 'equation:' ) )? ( (lv_expr_2_0= ruleEquationRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2643:2: ( ( (lv_key_0_0= rule_SolveKey ) ) ( (lv_firstFacet_1_0= 'equation:' ) )? ( (lv_expr_2_0= ruleEquationRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2644:3: ( (lv_key_0_0= rule_SolveKey ) ) ( (lv_firstFacet_1_0= 'equation:' ) )? ( (lv_expr_2_0= ruleEquationRef ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2644:3: ( (lv_key_0_0= rule_SolveKey ) )
            // InternalGaml.g:2645:4: (lv_key_0_0= rule_SolveKey )
            {
            // InternalGaml.g:2645:4: (lv_key_0_0= rule_SolveKey )
            // InternalGaml.g:2646:5: lv_key_0_0= rule_SolveKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getKey_SolveKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_42);
            lv_key_0_0=rule_SolveKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SolveRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._SolveKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2663:3: ( (lv_firstFacet_1_0= 'equation:' ) )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==43) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // InternalGaml.g:2664:4: (lv_firstFacet_1_0= 'equation:' )
                    {
                    // InternalGaml.g:2664:4: (lv_firstFacet_1_0= 'equation:' )
                    // InternalGaml.g:2665:5: lv_firstFacet_1_0= 'equation:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,43,FOLLOW_42); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_SolveAccess().getFirstFacetEquationKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_SolveRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "equation:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:2677:3: ( (lv_expr_2_0= ruleEquationRef ) )
            // InternalGaml.g:2678:4: (lv_expr_2_0= ruleEquationRef )
            {
            // InternalGaml.g:2678:4: (lv_expr_2_0= ruleEquationRef )
            // InternalGaml.g:2679:5: lv_expr_2_0= ruleEquationRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getExprEquationRefParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_2_0=ruleEquationRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SolveRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.EquationRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_SolveRule());
              			}
              			newCompositeNode(grammarAccess.getS_SolveAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Solve"


    // $ANTLR start "entryRuleS_Display"
    // InternalGaml.g:2711:1: entryRuleS_Display returns [EObject current=null] : iv_ruleS_Display= ruleS_Display EOF ;
    public final EObject entryRuleS_Display() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Display = null;


        try {
            // InternalGaml.g:2711:50: (iv_ruleS_Display= ruleS_Display EOF )
            // InternalGaml.g:2712:2: iv_ruleS_Display= ruleS_Display EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_DisplayRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Display=ruleS_Display();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Display; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Display"


    // $ANTLR start "ruleS_Display"
    // InternalGaml.g:2718:1: ruleS_Display returns [EObject current=null] : ( ( (lv_key_0_0= 'display' ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) ( (lv_facets_3_0= ruleFacet ) )* ( (lv_block_4_0= ruleDisplayBlock ) ) ) ;
    public final EObject ruleS_Display() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_firstFacet_1_0=null;
        Token lv_name_2_2=null;
        AntlrDatatypeRuleToken lv_name_2_1 = null;

        EObject lv_facets_3_0 = null;

        EObject lv_block_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2724:2: ( ( ( (lv_key_0_0= 'display' ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) ( (lv_facets_3_0= ruleFacet ) )* ( (lv_block_4_0= ruleDisplayBlock ) ) ) )
            // InternalGaml.g:2725:2: ( ( (lv_key_0_0= 'display' ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) ( (lv_facets_3_0= ruleFacet ) )* ( (lv_block_4_0= ruleDisplayBlock ) ) )
            {
            // InternalGaml.g:2725:2: ( ( (lv_key_0_0= 'display' ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) ( (lv_facets_3_0= ruleFacet ) )* ( (lv_block_4_0= ruleDisplayBlock ) ) )
            // InternalGaml.g:2726:3: ( (lv_key_0_0= 'display' ) ) ( (lv_firstFacet_1_0= 'name:' ) )? ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) ) ( (lv_facets_3_0= ruleFacet ) )* ( (lv_block_4_0= ruleDisplayBlock ) )
            {
            // InternalGaml.g:2726:3: ( (lv_key_0_0= 'display' ) )
            // InternalGaml.g:2727:4: (lv_key_0_0= 'display' )
            {
            // InternalGaml.g:2727:4: (lv_key_0_0= 'display' )
            // InternalGaml.g:2728:5: lv_key_0_0= 'display'
            {
            lv_key_0_0=(Token)match(input,44,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_DisplayAccess().getKeyDisplayKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_DisplayRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "display");
              				
            }

            }


            }

            // InternalGaml.g:2740:3: ( (lv_firstFacet_1_0= 'name:' ) )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==22) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // InternalGaml.g:2741:4: (lv_firstFacet_1_0= 'name:' )
                    {
                    // InternalGaml.g:2741:4: (lv_firstFacet_1_0= 'name:' )
                    // InternalGaml.g:2742:5: lv_firstFacet_1_0= 'name:'
                    {
                    lv_firstFacet_1_0=(Token)match(input,22,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_firstFacet_1_0, grammarAccess.getS_DisplayAccess().getFirstFacetNameKeyword_1_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getS_DisplayRule());
                      					}
                      					setWithLastConsumed(current, "firstFacet", lv_firstFacet_1_0, "name:");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:2754:3: ( ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) ) )
            // InternalGaml.g:2755:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            {
            // InternalGaml.g:2755:4: ( (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING ) )
            // InternalGaml.g:2756:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            {
            // InternalGaml.g:2756:5: (lv_name_2_1= ruleValid_ID | lv_name_2_2= RULE_STRING )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==RULE_ID||LA46_0==45||(LA46_0>=47 && LA46_0<=49)||(LA46_0>=53 && LA46_0<=101)) ) {
                alt46=1;
            }
            else if ( (LA46_0==RULE_STRING) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // InternalGaml.g:2757:6: lv_name_2_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DisplayAccess().getNameValid_IDParserRuleCall_2_0_0());
                      					
                    }
                    pushFollow(FOLLOW_23);
                    lv_name_2_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DisplayRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_2_1,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2773:6: lv_name_2_2= RULE_STRING
                    {
                    lv_name_2_2=(Token)match(input,RULE_STRING,FOLLOW_23); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_2_2, grammarAccess.getS_DisplayAccess().getNameSTRINGTerminalRuleCall_2_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DisplayRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"name",
                      							lv_name_2_2,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:2790:3: ( (lv_facets_3_0= ruleFacet ) )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==RULE_ID||LA47_0==15||LA47_0==22||LA47_0==26||(LA47_0>=34 && LA47_0<=35)||(LA47_0>=109 && LA47_0<=133)) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // InternalGaml.g:2791:4: (lv_facets_3_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2791:4: (lv_facets_3_0= ruleFacet )
            	    // InternalGaml.g:2792:5: lv_facets_3_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_DisplayAccess().getFacetsFacetParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_23);
            	    lv_facets_3_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_DisplayRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_3_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);

            // InternalGaml.g:2809:3: ( (lv_block_4_0= ruleDisplayBlock ) )
            // InternalGaml.g:2810:4: (lv_block_4_0= ruleDisplayBlock )
            {
            // InternalGaml.g:2810:4: (lv_block_4_0= ruleDisplayBlock )
            // InternalGaml.g:2811:5: lv_block_4_0= ruleDisplayBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DisplayAccess().getBlockDisplayBlockParserRuleCall_4_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_4_0=ruleDisplayBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DisplayRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_4_0,
              						"gaml.compiler.Gaml.DisplayBlock");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Display"


    // $ANTLR start "entryRuleDisplayBlock"
    // InternalGaml.g:2832:1: entryRuleDisplayBlock returns [EObject current=null] : iv_ruleDisplayBlock= ruleDisplayBlock EOF ;
    public final EObject entryRuleDisplayBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDisplayBlock = null;


        try {
            // InternalGaml.g:2832:53: (iv_ruleDisplayBlock= ruleDisplayBlock EOF )
            // InternalGaml.g:2833:2: iv_ruleDisplayBlock= ruleDisplayBlock EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getDisplayBlockRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleDisplayBlock=ruleDisplayBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleDisplayBlock; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDisplayBlock"


    // $ANTLR start "ruleDisplayBlock"
    // InternalGaml.g:2839:1: ruleDisplayBlock returns [EObject current=null] : ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' ) ;
    public final EObject ruleDisplayBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2845:2: ( ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' ) )
            // InternalGaml.g:2846:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:2846:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' )
            // InternalGaml.g:2847:3: () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}'
            {
            // InternalGaml.g:2847:3: ()
            // InternalGaml.g:2848:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getDisplayBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,40,FOLLOW_43); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getDisplayBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:2858:3: ( (lv_statements_2_0= ruleS_Layer ) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( ((LA48_0>=47 && LA48_0<=52)||(LA48_0>=81 && LA48_0<=92)) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // InternalGaml.g:2859:4: (lv_statements_2_0= ruleS_Layer )
            	    {
            	    // InternalGaml.g:2859:4: (lv_statements_2_0= ruleS_Layer )
            	    // InternalGaml.g:2860:5: lv_statements_2_0= ruleS_Layer
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getDisplayBlockAccess().getStatementsS_LayerParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_43);
            	    lv_statements_2_0=ruleS_Layer();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getDisplayBlockRule());
            	      					}
            	      					add(
            	      						current,
            	      						"statements",
            	      						lv_statements_2_0,
            	      						"gaml.compiler.Gaml.S_Layer");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);

            otherlv_3=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getDisplayBlockAccess().getRightCurlyBracketKeyword_3());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDisplayBlock"


    // $ANTLR start "entryRuleS_Layer"
    // InternalGaml.g:2885:1: entryRuleS_Layer returns [EObject current=null] : iv_ruleS_Layer= ruleS_Layer EOF ;
    public final EObject entryRuleS_Layer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Layer = null;


        try {
            // InternalGaml.g:2885:48: (iv_ruleS_Layer= ruleS_Layer EOF )
            // InternalGaml.g:2886:2: iv_ruleS_Layer= ruleS_Layer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_LayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Layer=ruleS_Layer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Layer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Layer"


    // $ANTLR start "ruleS_Layer"
    // InternalGaml.g:2892:1: ruleS_Layer returns [EObject current=null] : (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer ) ;
    public final EObject ruleS_Layer() throws RecognitionException {
        EObject current = null;

        EObject this_S_SpeciesLayer_0 = null;

        EObject this_S_ImageLayer_1 = null;

        EObject this_S_GraphicsLayer_2 = null;

        EObject this_S_EventLayer_3 = null;

        EObject this_S_OverlayLayer_4 = null;

        EObject this_S_OtherLayer_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:2898:2: ( (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer ) )
            // InternalGaml.g:2899:2: (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer )
            {
            // InternalGaml.g:2899:2: (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer )
            int alt49=6;
            switch ( input.LA(1) ) {
            case 47:
            case 48:
                {
                alt49=1;
                }
                break;
            case 49:
                {
                alt49=2;
                }
                break;
            case 50:
                {
                alt49=3;
                }
                break;
            case 51:
                {
                alt49=4;
                }
                break;
            case 52:
                {
                alt49=5;
                }
                break;
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
                {
                alt49=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }

            switch (alt49) {
                case 1 :
                    // InternalGaml.g:2900:3: this_S_SpeciesLayer_0= ruleS_SpeciesLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_SpeciesLayerParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_SpeciesLayer_0=ruleS_SpeciesLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_SpeciesLayer_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2909:3: this_S_ImageLayer_1= ruleS_ImageLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_ImageLayerParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_ImageLayer_1=ruleS_ImageLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_ImageLayer_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2918:3: this_S_GraphicsLayer_2= ruleS_GraphicsLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_GraphicsLayerParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_GraphicsLayer_2=ruleS_GraphicsLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_GraphicsLayer_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:2927:3: this_S_EventLayer_3= ruleS_EventLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_EventLayerParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_EventLayer_3=ruleS_EventLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_EventLayer_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2936:3: this_S_OverlayLayer_4= ruleS_OverlayLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_OverlayLayerParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_OverlayLayer_4=ruleS_OverlayLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_OverlayLayer_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:2945:3: this_S_OtherLayer_5= ruleS_OtherLayer
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_LayerAccess().getS_OtherLayerParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_OtherLayer_5=ruleS_OtherLayer();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_OtherLayer_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Layer"


    // $ANTLR start "entryRuleS_SpeciesLayer"
    // InternalGaml.g:2957:1: entryRuleS_SpeciesLayer returns [EObject current=null] : iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF ;
    public final EObject entryRuleS_SpeciesLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_SpeciesLayer = null;


        try {
            // InternalGaml.g:2957:55: (iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF )
            // InternalGaml.g:2958:2: iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_SpeciesLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_SpeciesLayer=ruleS_SpeciesLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_SpeciesLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_SpeciesLayer"


    // $ANTLR start "ruleS_SpeciesLayer"
    // InternalGaml.g:2964:1: ruleS_SpeciesLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_SpeciesLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2970:2: ( ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2971:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2971:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2972:3: ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2972:3: ( (lv_key_0_0= rule_SpeciesKey ) )
            // InternalGaml.g:2973:4: (lv_key_0_0= rule_SpeciesKey )
            {
            // InternalGaml.g:2973:4: (lv_key_0_0= rule_SpeciesKey )
            // InternalGaml.g:2974:5: lv_key_0_0= rule_SpeciesKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesLayerAccess().getKey_SpeciesKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=rule_SpeciesKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SpeciesLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._SpeciesKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2991:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:2992:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:2992:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:2993:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SpeciesLayerRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_SpeciesLayerRule());
              			}
              			newCompositeNode(grammarAccess.getS_SpeciesLayerAccess().getFacetsAndBlockParserRuleCall_2());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_2=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_2;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_SpeciesLayer"


    // $ANTLR start "entryRuleS_ImageLayer"
    // InternalGaml.g:3025:1: entryRuleS_ImageLayer returns [EObject current=null] : iv_ruleS_ImageLayer= ruleS_ImageLayer EOF ;
    public final EObject entryRuleS_ImageLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_ImageLayer = null;


        try {
            // InternalGaml.g:3025:53: (iv_ruleS_ImageLayer= ruleS_ImageLayer EOF )
            // InternalGaml.g:3026:2: iv_ruleS_ImageLayer= ruleS_ImageLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_ImageLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_ImageLayer=ruleS_ImageLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_ImageLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_ImageLayer"


    // $ANTLR start "ruleS_ImageLayer"
    // InternalGaml.g:3032:1: ruleS_ImageLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_ImageLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' ) ;
    public final EObject ruleS_ImageLayer() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_facets_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3038:2: ( ( ( (lv_key_0_0= rule_ImageLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' ) )
            // InternalGaml.g:3039:2: ( ( (lv_key_0_0= rule_ImageLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' )
            {
            // InternalGaml.g:3039:2: ( ( (lv_key_0_0= rule_ImageLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' )
            // InternalGaml.g:3040:3: ( (lv_key_0_0= rule_ImageLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';'
            {
            // InternalGaml.g:3040:3: ( (lv_key_0_0= rule_ImageLayerKey ) )
            // InternalGaml.g:3041:4: (lv_key_0_0= rule_ImageLayerKey )
            {
            // InternalGaml.g:3041:4: (lv_key_0_0= rule_ImageLayerKey )
            // InternalGaml.g:3042:5: lv_key_0_0= rule_ImageLayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ImageLayerAccess().getKey_ImageLayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=rule_ImageLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ImageLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._ImageLayerKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:3059:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:3060:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:3060:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:3061:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ImageLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_38);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ImageLayerRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:3078:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==RULE_ID||LA50_0==15||LA50_0==22||LA50_0==26||(LA50_0>=34 && LA50_0<=35)||(LA50_0>=109 && LA50_0<=133)) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // InternalGaml.g:3079:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:3079:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:3080:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_ImageLayerAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_38);
            	    lv_facets_2_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_ImageLayerRule());
            	      					}
            	      					add(
            	      						current,
            	      						"facets",
            	      						lv_facets_2_0,
            	      						"gaml.compiler.Gaml.Facet");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);

            otherlv_3=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getS_ImageLayerAccess().getSemicolonKeyword_3());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_ImageLayer"


    // $ANTLR start "entryRuleS_GraphicsLayer"
    // InternalGaml.g:3105:1: entryRuleS_GraphicsLayer returns [EObject current=null] : iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF ;
    public final EObject entryRuleS_GraphicsLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_GraphicsLayer = null;


        try {
            // InternalGaml.g:3105:56: (iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF )
            // InternalGaml.g:3106:2: iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_GraphicsLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_GraphicsLayer=ruleS_GraphicsLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_GraphicsLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_GraphicsLayer"


    // $ANTLR start "ruleS_GraphicsLayer"
    // InternalGaml.g:3112:1: ruleS_GraphicsLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_GraphicsLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_GraphicsLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:3118:2: ( ( ( (lv_key_0_0= rule_GraphicsLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:3119:2: ( ( (lv_key_0_0= rule_GraphicsLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:3119:2: ( ( (lv_key_0_0= rule_GraphicsLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:3120:3: ( (lv_key_0_0= rule_GraphicsLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:3120:3: ( (lv_key_0_0= rule_GraphicsLayerKey ) )
            // InternalGaml.g:3121:4: (lv_key_0_0= rule_GraphicsLayerKey )
            {
            // InternalGaml.g:3121:4: (lv_key_0_0= rule_GraphicsLayerKey )
            // InternalGaml.g:3122:5: lv_key_0_0= rule_GraphicsLayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GraphicsLayerAccess().getKey_GraphicsLayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=rule_GraphicsLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_GraphicsLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._GraphicsLayerKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:3139:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:3140:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:3140:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:3141:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GraphicsLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_GraphicsLayerRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_GraphicsLayerRule());
              			}
              			newCompositeNode(grammarAccess.getS_GraphicsLayerAccess().getFacetsAndBlockParserRuleCall_2());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_2=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_2;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_GraphicsLayer"


    // $ANTLR start "entryRuleS_EventLayer"
    // InternalGaml.g:3173:1: entryRuleS_EventLayer returns [EObject current=null] : iv_ruleS_EventLayer= ruleS_EventLayer EOF ;
    public final EObject entryRuleS_EventLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_EventLayer = null;


        try {
            // InternalGaml.g:3173:53: (iv_ruleS_EventLayer= ruleS_EventLayer EOF )
            // InternalGaml.g:3174:2: iv_ruleS_EventLayer= ruleS_EventLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_EventLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_EventLayer=ruleS_EventLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_EventLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_EventLayer"


    // $ANTLR start "ruleS_EventLayer"
    // InternalGaml.g:3180:1: ruleS_EventLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_EventLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_EventLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:3186:2: ( ( ( (lv_key_0_0= rule_EventLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:3187:2: ( ( (lv_key_0_0= rule_EventLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:3187:2: ( ( (lv_key_0_0= rule_EventLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:3188:3: ( (lv_key_0_0= rule_EventLayerKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:3188:3: ( (lv_key_0_0= rule_EventLayerKey ) )
            // InternalGaml.g:3189:4: (lv_key_0_0= rule_EventLayerKey )
            {
            // InternalGaml.g:3189:4: (lv_key_0_0= rule_EventLayerKey )
            // InternalGaml.g:3190:5: lv_key_0_0= rule_EventLayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EventLayerAccess().getKey_EventLayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=rule_EventLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_EventLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._EventLayerKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:3207:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:3208:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:3208:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:3209:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EventLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_EventLayerRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_EventLayerRule());
              			}
              			newCompositeNode(grammarAccess.getS_EventLayerAccess().getFacetsAndBlockParserRuleCall_2());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_2=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_2;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_EventLayer"


    // $ANTLR start "entryRuleS_OverlayLayer"
    // InternalGaml.g:3241:1: entryRuleS_OverlayLayer returns [EObject current=null] : iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF ;
    public final EObject entryRuleS_OverlayLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_OverlayLayer = null;


        try {
            // InternalGaml.g:3241:55: (iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF )
            // InternalGaml.g:3242:2: iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_OverlayLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_OverlayLayer=ruleS_OverlayLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_OverlayLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_OverlayLayer"


    // $ANTLR start "ruleS_OverlayLayer"
    // InternalGaml.g:3248:1: ruleS_OverlayLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_OverlayLayerKey ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_OverlayLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:3254:2: ( ( ( (lv_key_0_0= rule_OverlayLayerKey ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:3255:2: ( ( (lv_key_0_0= rule_OverlayLayerKey ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:3255:2: ( ( (lv_key_0_0= rule_OverlayLayerKey ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:3256:3: ( (lv_key_0_0= rule_OverlayLayerKey ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:3256:3: ( (lv_key_0_0= rule_OverlayLayerKey ) )
            // InternalGaml.g:3257:4: (lv_key_0_0= rule_OverlayLayerKey )
            {
            // InternalGaml.g:3257:4: (lv_key_0_0= rule_OverlayLayerKey )
            // InternalGaml.g:3258:5: lv_key_0_0= rule_OverlayLayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OverlayLayerAccess().getKey_OverlayLayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_key_0_0=rule_OverlayLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_OverlayLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._OverlayLayerKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_OverlayLayerRule());
              			}
              			newCompositeNode(grammarAccess.getS_OverlayLayerAccess().getFacetsAndBlockParserRuleCall_1());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_1=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_1;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_OverlayLayer"


    // $ANTLR start "entryRuleS_OtherLayer"
    // InternalGaml.g:3290:1: entryRuleS_OtherLayer returns [EObject current=null] : iv_ruleS_OtherLayer= ruleS_OtherLayer EOF ;
    public final EObject entryRuleS_OtherLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_OtherLayer = null;


        try {
            // InternalGaml.g:3290:53: (iv_ruleS_OtherLayer= ruleS_OtherLayer EOF )
            // InternalGaml.g:3291:2: iv_ruleS_OtherLayer= ruleS_OtherLayer EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_OtherLayerRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_OtherLayer=ruleS_OtherLayer();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_OtherLayer; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_OtherLayer"


    // $ANTLR start "ruleS_OtherLayer"
    // InternalGaml.g:3297:1: ruleS_OtherLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_LayerKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_OtherLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_firstFacet_1_0 = null;

        EObject lv_expr_2_0 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:3303:2: ( ( ( (lv_key_0_0= rule_LayerKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:3304:2: ( ( (lv_key_0_0= rule_LayerKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:3304:2: ( ( (lv_key_0_0= rule_LayerKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:3305:3: ( (lv_key_0_0= rule_LayerKey ) ) ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )? ( (lv_expr_2_0= ruleExpression ) ) this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:3305:3: ( (lv_key_0_0= rule_LayerKey ) )
            // InternalGaml.g:3306:4: (lv_key_0_0= rule_LayerKey )
            {
            // InternalGaml.g:3306:4: (lv_key_0_0= rule_LayerKey )
            // InternalGaml.g:3307:5: lv_key_0_0= rule_LayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherLayerAccess().getKey_LayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_21);
            lv_key_0_0=rule_LayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_OtherLayerRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml._LayerKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:3324:3: ( (lv_firstFacet_1_0= ruleFirstFacetKey ) )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==22||LA51_0==26||(LA51_0>=34 && LA51_0<=35)||(LA51_0>=109 && LA51_0<=132)) ) {
                alt51=1;
            }
            else if ( (LA51_0==RULE_ID) ) {
                int LA51_2 = input.LA(2);

                if ( (LA51_2==36) ) {
                    alt51=1;
                }
            }
            switch (alt51) {
                case 1 :
                    // InternalGaml.g:3325:4: (lv_firstFacet_1_0= ruleFirstFacetKey )
                    {
                    // InternalGaml.g:3325:4: (lv_firstFacet_1_0= ruleFirstFacetKey )
                    // InternalGaml.g:3326:5: lv_firstFacet_1_0= ruleFirstFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_OtherLayerAccess().getFirstFacetFirstFacetKeyParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_5);
                    lv_firstFacet_1_0=ruleFirstFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getS_OtherLayerRule());
                      					}
                      					set(
                      						current,
                      						"firstFacet",
                      						lv_firstFacet_1_0,
                      						"gaml.compiler.Gaml.FirstFacetKey");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:3343:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:3344:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:3344:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:3345:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherLayerAccess().getExprExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_OtherLayerRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_2_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_OtherLayerRule());
              			}
              			newCompositeNode(grammarAccess.getS_OtherLayerAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_3=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_3;
              			afterParserOrEnumRuleCall();
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_OtherLayer"


    // $ANTLR start "entryRule_EquationsKey"
    // InternalGaml.g:3377:1: entryRule_EquationsKey returns [String current=null] : iv_rule_EquationsKey= rule_EquationsKey EOF ;
    public final String entryRule_EquationsKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_EquationsKey = null;


        try {
            // InternalGaml.g:3377:53: (iv_rule_EquationsKey= rule_EquationsKey EOF )
            // InternalGaml.g:3378:2: iv_rule_EquationsKey= rule_EquationsKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_EquationsKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_EquationsKey=rule_EquationsKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_EquationsKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_EquationsKey"


    // $ANTLR start "rule_EquationsKey"
    // InternalGaml.g:3384:1: rule_EquationsKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'equation' ;
    public final AntlrDatatypeRuleToken rule_EquationsKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3390:2: (kw= 'equation' )
            // InternalGaml.g:3391:2: kw= 'equation'
            {
            kw=(Token)match(input,45,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_EquationsKeyAccess().getEquationKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_EquationsKey"


    // $ANTLR start "entryRule_SolveKey"
    // InternalGaml.g:3399:1: entryRule_SolveKey returns [String current=null] : iv_rule_SolveKey= rule_SolveKey EOF ;
    public final String entryRule_SolveKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_SolveKey = null;


        try {
            // InternalGaml.g:3399:49: (iv_rule_SolveKey= rule_SolveKey EOF )
            // InternalGaml.g:3400:2: iv_rule_SolveKey= rule_SolveKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_SolveKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_SolveKey=rule_SolveKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_SolveKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_SolveKey"


    // $ANTLR start "rule_SolveKey"
    // InternalGaml.g:3406:1: rule_SolveKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'solve' ;
    public final AntlrDatatypeRuleToken rule_SolveKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3412:2: (kw= 'solve' )
            // InternalGaml.g:3413:2: kw= 'solve'
            {
            kw=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_SolveKeyAccess().getSolveKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_SolveKey"


    // $ANTLR start "entryRule_SpeciesKey"
    // InternalGaml.g:3421:1: entryRule_SpeciesKey returns [String current=null] : iv_rule_SpeciesKey= rule_SpeciesKey EOF ;
    public final String entryRule_SpeciesKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_SpeciesKey = null;


        try {
            // InternalGaml.g:3421:51: (iv_rule_SpeciesKey= rule_SpeciesKey EOF )
            // InternalGaml.g:3422:2: iv_rule_SpeciesKey= rule_SpeciesKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_SpeciesKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_SpeciesKey=rule_SpeciesKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_SpeciesKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_SpeciesKey"


    // $ANTLR start "rule_SpeciesKey"
    // InternalGaml.g:3428:1: rule_SpeciesKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'species' | kw= 'grid' ) ;
    public final AntlrDatatypeRuleToken rule_SpeciesKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3434:2: ( (kw= 'species' | kw= 'grid' ) )
            // InternalGaml.g:3435:2: (kw= 'species' | kw= 'grid' )
            {
            // InternalGaml.g:3435:2: (kw= 'species' | kw= 'grid' )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==47) ) {
                alt52=1;
            }
            else if ( (LA52_0==48) ) {
                alt52=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }
            switch (alt52) {
                case 1 :
                    // InternalGaml.g:3436:3: kw= 'species'
                    {
                    kw=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_SpeciesKeyAccess().getSpeciesKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3442:3: kw= 'grid'
                    {
                    kw=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_SpeciesKeyAccess().getGridKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_SpeciesKey"


    // $ANTLR start "entryRule_ImageLayerKey"
    // InternalGaml.g:3451:1: entryRule_ImageLayerKey returns [String current=null] : iv_rule_ImageLayerKey= rule_ImageLayerKey EOF ;
    public final String entryRule_ImageLayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_ImageLayerKey = null;


        try {
            // InternalGaml.g:3451:54: (iv_rule_ImageLayerKey= rule_ImageLayerKey EOF )
            // InternalGaml.g:3452:2: iv_rule_ImageLayerKey= rule_ImageLayerKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_ImageLayerKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_ImageLayerKey=rule_ImageLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_ImageLayerKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_ImageLayerKey"


    // $ANTLR start "rule_ImageLayerKey"
    // InternalGaml.g:3458:1: rule_ImageLayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'image' ;
    public final AntlrDatatypeRuleToken rule_ImageLayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3464:2: (kw= 'image' )
            // InternalGaml.g:3465:2: kw= 'image'
            {
            kw=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_ImageLayerKeyAccess().getImageKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_ImageLayerKey"


    // $ANTLR start "entryRule_GraphicsLayerKey"
    // InternalGaml.g:3473:1: entryRule_GraphicsLayerKey returns [String current=null] : iv_rule_GraphicsLayerKey= rule_GraphicsLayerKey EOF ;
    public final String entryRule_GraphicsLayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_GraphicsLayerKey = null;


        try {
            // InternalGaml.g:3473:57: (iv_rule_GraphicsLayerKey= rule_GraphicsLayerKey EOF )
            // InternalGaml.g:3474:2: iv_rule_GraphicsLayerKey= rule_GraphicsLayerKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_GraphicsLayerKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_GraphicsLayerKey=rule_GraphicsLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_GraphicsLayerKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_GraphicsLayerKey"


    // $ANTLR start "rule_GraphicsLayerKey"
    // InternalGaml.g:3480:1: rule_GraphicsLayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'graphics' ;
    public final AntlrDatatypeRuleToken rule_GraphicsLayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3486:2: (kw= 'graphics' )
            // InternalGaml.g:3487:2: kw= 'graphics'
            {
            kw=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_GraphicsLayerKeyAccess().getGraphicsKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_GraphicsLayerKey"


    // $ANTLR start "entryRule_EventLayerKey"
    // InternalGaml.g:3495:1: entryRule_EventLayerKey returns [String current=null] : iv_rule_EventLayerKey= rule_EventLayerKey EOF ;
    public final String entryRule_EventLayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_EventLayerKey = null;


        try {
            // InternalGaml.g:3495:54: (iv_rule_EventLayerKey= rule_EventLayerKey EOF )
            // InternalGaml.g:3496:2: iv_rule_EventLayerKey= rule_EventLayerKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_EventLayerKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_EventLayerKey=rule_EventLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_EventLayerKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_EventLayerKey"


    // $ANTLR start "rule_EventLayerKey"
    // InternalGaml.g:3502:1: rule_EventLayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'event' ;
    public final AntlrDatatypeRuleToken rule_EventLayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3508:2: (kw= 'event' )
            // InternalGaml.g:3509:2: kw= 'event'
            {
            kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_EventLayerKeyAccess().getEventKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_EventLayerKey"


    // $ANTLR start "entryRule_OverlayLayerKey"
    // InternalGaml.g:3517:1: entryRule_OverlayLayerKey returns [String current=null] : iv_rule_OverlayLayerKey= rule_OverlayLayerKey EOF ;
    public final String entryRule_OverlayLayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_OverlayLayerKey = null;


        try {
            // InternalGaml.g:3517:56: (iv_rule_OverlayLayerKey= rule_OverlayLayerKey EOF )
            // InternalGaml.g:3518:2: iv_rule_OverlayLayerKey= rule_OverlayLayerKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_OverlayLayerKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_OverlayLayerKey=rule_OverlayLayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_OverlayLayerKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_OverlayLayerKey"


    // $ANTLR start "rule_OverlayLayerKey"
    // InternalGaml.g:3524:1: rule_OverlayLayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'overlay' ;
    public final AntlrDatatypeRuleToken rule_OverlayLayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3530:2: (kw= 'overlay' )
            // InternalGaml.g:3531:2: kw= 'overlay'
            {
            kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_OverlayLayerKeyAccess().getOverlayKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_OverlayLayerKey"


    // $ANTLR start "entryRule_ExperimentKey"
    // InternalGaml.g:3539:1: entryRule_ExperimentKey returns [String current=null] : iv_rule_ExperimentKey= rule_ExperimentKey EOF ;
    public final String entryRule_ExperimentKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_ExperimentKey = null;


        try {
            // InternalGaml.g:3539:54: (iv_rule_ExperimentKey= rule_ExperimentKey EOF )
            // InternalGaml.g:3540:2: iv_rule_ExperimentKey= rule_ExperimentKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_ExperimentKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_ExperimentKey=rule_ExperimentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_ExperimentKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_ExperimentKey"


    // $ANTLR start "rule_ExperimentKey"
    // InternalGaml.g:3546:1: rule_ExperimentKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'experiment' ;
    public final AntlrDatatypeRuleToken rule_ExperimentKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3552:2: (kw= 'experiment' )
            // InternalGaml.g:3553:2: kw= 'experiment'
            {
            kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.get_ExperimentKeyAccess().getExperimentKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_ExperimentKey"


    // $ANTLR start "entryRule_GeneralKey"
    // InternalGaml.g:3561:1: entryRule_GeneralKey returns [String current=null] : iv_rule_GeneralKey= rule_GeneralKey EOF ;
    public final String entryRule_GeneralKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_GeneralKey = null;


        try {
            // InternalGaml.g:3561:51: (iv_rule_GeneralKey= rule_GeneralKey EOF )
            // InternalGaml.g:3562:2: iv_rule_GeneralKey= rule_GeneralKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_GeneralKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_GeneralKey=rule_GeneralKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_GeneralKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_GeneralKey"


    // $ANTLR start "rule_GeneralKey"
    // InternalGaml.g:3568:1: rule_GeneralKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) ;
    public final AntlrDatatypeRuleToken rule_GeneralKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this__LayerKey_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3574:2: ( (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) )
            // InternalGaml.g:3575:2: (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            {
            // InternalGaml.g:3575:2: (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            int alt53=28;
            switch ( input.LA(1) ) {
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
                {
                alt53=1;
                }
                break;
            case 54:
                {
                alt53=2;
                }
                break;
            case 55:
                {
                alt53=3;
                }
                break;
            case 56:
                {
                alt53=4;
                }
                break;
            case 57:
                {
                alt53=5;
                }
                break;
            case 58:
                {
                alt53=6;
                }
                break;
            case 59:
                {
                alt53=7;
                }
                break;
            case 60:
                {
                alt53=8;
                }
                break;
            case 61:
                {
                alt53=9;
                }
                break;
            case 62:
                {
                alt53=10;
                }
                break;
            case 63:
                {
                alt53=11;
                }
                break;
            case 64:
                {
                alt53=12;
                }
                break;
            case 65:
                {
                alt53=13;
                }
                break;
            case 66:
                {
                alt53=14;
                }
                break;
            case 67:
                {
                alt53=15;
                }
                break;
            case 68:
                {
                alt53=16;
                }
                break;
            case 69:
                {
                alt53=17;
                }
                break;
            case 70:
                {
                alt53=18;
                }
                break;
            case 71:
                {
                alt53=19;
                }
                break;
            case 72:
                {
                alt53=20;
                }
                break;
            case 73:
                {
                alt53=21;
                }
                break;
            case 74:
                {
                alt53=22;
                }
                break;
            case 75:
                {
                alt53=23;
                }
                break;
            case 76:
                {
                alt53=24;
                }
                break;
            case 77:
                {
                alt53=25;
                }
                break;
            case 78:
                {
                alt53=26;
                }
                break;
            case 79:
                {
                alt53=27;
                }
                break;
            case 80:
                {
                alt53=28;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }

            switch (alt53) {
                case 1 :
                    // InternalGaml.g:3576:3: this__LayerKey_0= rule_LayerKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.get_GeneralKeyAccess().get_LayerKeyParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__LayerKey_0=rule_LayerKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__LayerKey_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3587:3: kw= 'ask'
                    {
                    kw=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAskKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3593:3: kw= 'release'
                    {
                    kw=(Token)match(input,55,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getReleaseKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3599:3: kw= 'capture'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCaptureKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3605:3: kw= 'create'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCreateKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3611:3: kw= 'write'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWriteKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3617:3: kw= 'error'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getErrorKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3623:3: kw= 'warn'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWarnKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3629:3: kw= 'exception'
                    {
                    kw=(Token)match(input,61,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getExceptionKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3635:3: kw= 'save'
                    {
                    kw=(Token)match(input,62,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSaveKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3641:3: kw= 'assert'
                    {
                    kw=(Token)match(input,63,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAssertKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:3647:3: kw= 'inspect'
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getInspectKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:3653:3: kw= 'browse'
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getBrowseKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:3659:3: kw= 'restore'
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRestoreKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:3665:3: kw= 'draw'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getDrawKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:3671:3: kw= 'using'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getUsingKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:3677:3: kw= 'switch'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSwitchKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:3683:3: kw= 'put'
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getPutKeyword_17());
                      		
                    }

                    }
                    break;
                case 19 :
                    // InternalGaml.g:3689:3: kw= 'add'
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAddKeyword_18());
                      		
                    }

                    }
                    break;
                case 20 :
                    // InternalGaml.g:3695:3: kw= 'remove'
                    {
                    kw=(Token)match(input,72,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRemoveKeyword_19());
                      		
                    }

                    }
                    break;
                case 21 :
                    // InternalGaml.g:3701:3: kw= 'match'
                    {
                    kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatchKeyword_20());
                      		
                    }

                    }
                    break;
                case 22 :
                    // InternalGaml.g:3707:3: kw= 'match_between'
                    {
                    kw=(Token)match(input,74,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_betweenKeyword_21());
                      		
                    }

                    }
                    break;
                case 23 :
                    // InternalGaml.g:3713:3: kw= 'match_one'
                    {
                    kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_oneKeyword_22());
                      		
                    }

                    }
                    break;
                case 24 :
                    // InternalGaml.g:3719:3: kw= 'parameter'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getParameterKeyword_23());
                      		
                    }

                    }
                    break;
                case 25 :
                    // InternalGaml.g:3725:3: kw= 'status'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getStatusKeyword_24());
                      		
                    }

                    }
                    break;
                case 26 :
                    // InternalGaml.g:3731:3: kw= 'highlight'
                    {
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getHighlightKeyword_25());
                      		
                    }

                    }
                    break;
                case 27 :
                    // InternalGaml.g:3737:3: kw= 'focus_on'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getFocus_onKeyword_26());
                      		
                    }

                    }
                    break;
                case 28 :
                    // InternalGaml.g:3743:3: kw= 'layout'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getLayoutKeyword_27());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_GeneralKey"


    // $ANTLR start "entryRule_LayerKey"
    // InternalGaml.g:3752:1: entryRule_LayerKey returns [String current=null] : iv_rule_LayerKey= rule_LayerKey EOF ;
    public final String entryRule_LayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_LayerKey = null;


        try {
            // InternalGaml.g:3752:49: (iv_rule_LayerKey= rule_LayerKey EOF )
            // InternalGaml.g:3753:2: iv_rule_LayerKey= rule_LayerKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_LayerKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_LayerKey=rule_LayerKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_LayerKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_LayerKey"


    // $ANTLR start "rule_LayerKey"
    // InternalGaml.g:3759:1: rule_LayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'image_layer' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' ) ;
    public final AntlrDatatypeRuleToken rule_LayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3765:2: ( (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'image_layer' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' ) )
            // InternalGaml.g:3766:2: (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'image_layer' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' )
            {
            // InternalGaml.g:3766:2: (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'image_layer' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' )
            int alt54=12;
            switch ( input.LA(1) ) {
            case 81:
                {
                alt54=1;
                }
                break;
            case 82:
                {
                alt54=2;
                }
                break;
            case 83:
                {
                alt54=3;
                }
                break;
            case 84:
                {
                alt54=4;
                }
                break;
            case 85:
                {
                alt54=5;
                }
                break;
            case 86:
                {
                alt54=6;
                }
                break;
            case 87:
                {
                alt54=7;
                }
                break;
            case 88:
                {
                alt54=8;
                }
                break;
            case 89:
                {
                alt54=9;
                }
                break;
            case 90:
                {
                alt54=10;
                }
                break;
            case 91:
                {
                alt54=11;
                }
                break;
            case 92:
                {
                alt54=12;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }

            switch (alt54) {
                case 1 :
                    // InternalGaml.g:3767:3: kw= 'light'
                    {
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getLightKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3773:3: kw= 'camera'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getCameraKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3779:3: kw= 'text'
                    {
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getTextKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3785:3: kw= 'image_layer'
                    {
                    kw=(Token)match(input,84,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getImage_layerKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3791:3: kw= 'data'
                    {
                    kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDataKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3797:3: kw= 'chart'
                    {
                    kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getChartKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3803:3: kw= 'agents'
                    {
                    kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getAgentsKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3809:3: kw= 'display_population'
                    {
                    kw=(Token)match(input,88,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDisplay_populationKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3815:3: kw= 'display_grid'
                    {
                    kw=(Token)match(input,89,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDisplay_gridKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3821:3: kw= 'datalist'
                    {
                    kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDatalistKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3827:3: kw= 'mesh'
                    {
                    kw=(Token)match(input,91,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getMeshKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:3833:3: kw= 'rotation'
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getRotationKeyword_11());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_LayerKey"


    // $ANTLR start "entryRule_DoKey"
    // InternalGaml.g:3842:1: entryRule_DoKey returns [String current=null] : iv_rule_DoKey= rule_DoKey EOF ;
    public final String entryRule_DoKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_DoKey = null;


        try {
            // InternalGaml.g:3842:46: (iv_rule_DoKey= rule_DoKey EOF )
            // InternalGaml.g:3843:2: iv_rule_DoKey= rule_DoKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_DoKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_DoKey=rule_DoKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_DoKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_DoKey"


    // $ANTLR start "rule_DoKey"
    // InternalGaml.g:3849:1: rule_DoKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'do' | kw= 'invoke' ) ;
    public final AntlrDatatypeRuleToken rule_DoKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3855:2: ( (kw= 'do' | kw= 'invoke' ) )
            // InternalGaml.g:3856:2: (kw= 'do' | kw= 'invoke' )
            {
            // InternalGaml.g:3856:2: (kw= 'do' | kw= 'invoke' )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==93) ) {
                alt55=1;
            }
            else if ( (LA55_0==94) ) {
                alt55=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;
            }
            switch (alt55) {
                case 1 :
                    // InternalGaml.g:3857:3: kw= 'do'
                    {
                    kw=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_DoKeyAccess().getDoKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3863:3: kw= 'invoke'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_DoKeyAccess().getInvokeKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_DoKey"


    // $ANTLR start "entryRule_VarOrConstKey"
    // InternalGaml.g:3872:1: entryRule_VarOrConstKey returns [String current=null] : iv_rule_VarOrConstKey= rule_VarOrConstKey EOF ;
    public final String entryRule_VarOrConstKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_VarOrConstKey = null;


        try {
            // InternalGaml.g:3872:54: (iv_rule_VarOrConstKey= rule_VarOrConstKey EOF )
            // InternalGaml.g:3873:2: iv_rule_VarOrConstKey= rule_VarOrConstKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_VarOrConstKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_VarOrConstKey=rule_VarOrConstKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_VarOrConstKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_VarOrConstKey"


    // $ANTLR start "rule_VarOrConstKey"
    // InternalGaml.g:3879:1: rule_VarOrConstKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'var' | kw= 'const' | kw= 'let' | kw= 'arg' ) ;
    public final AntlrDatatypeRuleToken rule_VarOrConstKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3885:2: ( (kw= 'var' | kw= 'const' | kw= 'let' | kw= 'arg' ) )
            // InternalGaml.g:3886:2: (kw= 'var' | kw= 'const' | kw= 'let' | kw= 'arg' )
            {
            // InternalGaml.g:3886:2: (kw= 'var' | kw= 'const' | kw= 'let' | kw= 'arg' )
            int alt56=4;
            switch ( input.LA(1) ) {
            case 95:
                {
                alt56=1;
                }
                break;
            case 96:
                {
                alt56=2;
                }
                break;
            case 97:
                {
                alt56=3;
                }
                break;
            case 98:
                {
                alt56=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;
            }

            switch (alt56) {
                case 1 :
                    // InternalGaml.g:3887:3: kw= 'var'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_VarOrConstKeyAccess().getVarKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3893:3: kw= 'const'
                    {
                    kw=(Token)match(input,96,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_VarOrConstKeyAccess().getConstKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3899:3: kw= 'let'
                    {
                    kw=(Token)match(input,97,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_VarOrConstKeyAccess().getLetKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3905:3: kw= 'arg'
                    {
                    kw=(Token)match(input,98,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_VarOrConstKeyAccess().getArgKeyword_3());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_VarOrConstKey"


    // $ANTLR start "entryRule_ReflexKey"
    // InternalGaml.g:3914:1: entryRule_ReflexKey returns [String current=null] : iv_rule_ReflexKey= rule_ReflexKey EOF ;
    public final String entryRule_ReflexKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_ReflexKey = null;


        try {
            // InternalGaml.g:3914:50: (iv_rule_ReflexKey= rule_ReflexKey EOF )
            // InternalGaml.g:3915:2: iv_rule_ReflexKey= rule_ReflexKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_ReflexKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_ReflexKey=rule_ReflexKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_ReflexKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_ReflexKey"


    // $ANTLR start "rule_ReflexKey"
    // InternalGaml.g:3921:1: rule_ReflexKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'init' | kw= 'reflex' | kw= 'aspect' ) ;
    public final AntlrDatatypeRuleToken rule_ReflexKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3927:2: ( (kw= 'init' | kw= 'reflex' | kw= 'aspect' ) )
            // InternalGaml.g:3928:2: (kw= 'init' | kw= 'reflex' | kw= 'aspect' )
            {
            // InternalGaml.g:3928:2: (kw= 'init' | kw= 'reflex' | kw= 'aspect' )
            int alt57=3;
            switch ( input.LA(1) ) {
            case 99:
                {
                alt57=1;
                }
                break;
            case 100:
                {
                alt57=2;
                }
                break;
            case 101:
                {
                alt57=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }

            switch (alt57) {
                case 1 :
                    // InternalGaml.g:3929:3: kw= 'init'
                    {
                    kw=(Token)match(input,99,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_ReflexKeyAccess().getInitKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3935:3: kw= 'reflex'
                    {
                    kw=(Token)match(input,100,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_ReflexKeyAccess().getReflexKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3941:3: kw= 'aspect'
                    {
                    kw=(Token)match(input,101,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_ReflexKeyAccess().getAspectKeyword_2());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_ReflexKey"


    // $ANTLR start "entryRule_AssignmentKey"
    // InternalGaml.g:3950:1: entryRule_AssignmentKey returns [String current=null] : iv_rule_AssignmentKey= rule_AssignmentKey EOF ;
    public final String entryRule_AssignmentKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_AssignmentKey = null;


        try {
            // InternalGaml.g:3950:54: (iv_rule_AssignmentKey= rule_AssignmentKey EOF )
            // InternalGaml.g:3951:2: iv_rule_AssignmentKey= rule_AssignmentKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.get_AssignmentKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rule_AssignmentKey=rule_AssignmentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rule_AssignmentKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRule_AssignmentKey"


    // $ANTLR start "rule_AssignmentKey"
    // InternalGaml.g:3957:1: rule_AssignmentKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) ;
    public final AntlrDatatypeRuleToken rule_AssignmentKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3963:2: ( (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) )
            // InternalGaml.g:3964:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            {
            // InternalGaml.g:3964:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            int alt58=8;
            alt58 = dfa58.predict(input);
            switch (alt58) {
                case 1 :
                    // InternalGaml.g:3965:3: kw= '<-'
                    {
                    kw=(Token)match(input,15,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getLessThanSignHyphenMinusKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3971:3: kw= '<<'
                    {
                    kw=(Token)match(input,102,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getLessThanSignLessThanSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3977:3: (kw= '>' kw= '>' )
                    {
                    // InternalGaml.g:3977:3: (kw= '>' kw= '>' )
                    // InternalGaml.g:3978:4: kw= '>' kw= '>'
                    {
                    kw=(Token)match(input,103,FOLLOW_44); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getGreaterThanSignKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,103,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getGreaterThanSignKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:3990:3: kw= '<<+'
                    {
                    kw=(Token)match(input,104,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getLessThanSignLessThanSignPlusSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3996:3: (kw= '>' kw= '>-' )
                    {
                    // InternalGaml.g:3996:3: (kw= '>' kw= '>-' )
                    // InternalGaml.g:3997:4: kw= '>' kw= '>-'
                    {
                    kw=(Token)match(input,103,FOLLOW_45); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getGreaterThanSignKeyword_4_0());
                      			
                    }
                    kw=(Token)match(input,105,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getGreaterThanSignHyphenMinusKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalGaml.g:4009:3: kw= '+<-'
                    {
                    kw=(Token)match(input,106,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getPlusSignLessThanSignHyphenMinusKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:4015:3: kw= '<+'
                    {
                    kw=(Token)match(input,107,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getLessThanSignPlusSignKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:4021:3: kw= '>-'
                    {
                    kw=(Token)match(input,105,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_AssignmentKeyAccess().getGreaterThanSignHyphenMinusKeyword_7());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rule_AssignmentKey"


    // $ANTLR start "entryRuleActionArguments"
    // InternalGaml.g:4030:1: entryRuleActionArguments returns [EObject current=null] : iv_ruleActionArguments= ruleActionArguments EOF ;
    public final EObject entryRuleActionArguments() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionArguments = null;


        try {
            // InternalGaml.g:4030:56: (iv_ruleActionArguments= ruleActionArguments EOF )
            // InternalGaml.g:4031:2: iv_ruleActionArguments= ruleActionArguments EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionArgumentsRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionArguments=ruleActionArguments();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionArguments; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionArguments"


    // $ANTLR start "ruleActionArguments"
    // InternalGaml.g:4037:1: ruleActionArguments returns [EObject current=null] : ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) ;
    public final EObject ruleActionArguments() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_args_0_0 = null;

        EObject lv_args_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4043:2: ( ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) )
            // InternalGaml.g:4044:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            {
            // InternalGaml.g:4044:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            // InternalGaml.g:4045:3: ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            {
            // InternalGaml.g:4045:3: ( (lv_args_0_0= ruleArgumentDefinition ) )
            // InternalGaml.g:4046:4: (lv_args_0_0= ruleArgumentDefinition )
            {
            // InternalGaml.g:4046:4: (lv_args_0_0= ruleArgumentDefinition )
            // InternalGaml.g:4047:5: lv_args_0_0= ruleArgumentDefinition
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_46);
            lv_args_0_0=ruleArgumentDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getActionArgumentsRule());
              					}
              					add(
              						current,
              						"args",
              						lv_args_0_0,
              						"gaml.compiler.Gaml.ArgumentDefinition");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:4064:3: (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==108) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // InternalGaml.g:4065:4: otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    {
            	    otherlv_1=(Token)match(input,108,FOLLOW_35); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_1, grammarAccess.getActionArgumentsAccess().getCommaKeyword_1_0());
            	      			
            	    }
            	    // InternalGaml.g:4069:4: ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    // InternalGaml.g:4070:5: (lv_args_2_0= ruleArgumentDefinition )
            	    {
            	    // InternalGaml.g:4070:5: (lv_args_2_0= ruleArgumentDefinition )
            	    // InternalGaml.g:4071:6: lv_args_2_0= ruleArgumentDefinition
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_46);
            	    lv_args_2_0=ruleArgumentDefinition();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getActionArgumentsRule());
            	      						}
            	      						add(
            	      							current,
            	      							"args",
            	      							lv_args_2_0,
            	      							"gaml.compiler.Gaml.ArgumentDefinition");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionArguments"


    // $ANTLR start "entryRuleArgumentDefinition"
    // InternalGaml.g:4093:1: entryRuleArgumentDefinition returns [EObject current=null] : iv_ruleArgumentDefinition= ruleArgumentDefinition EOF ;
    public final EObject entryRuleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArgumentDefinition = null;


        try {
            // InternalGaml.g:4093:59: (iv_ruleArgumentDefinition= ruleArgumentDefinition EOF )
            // InternalGaml.g:4094:2: iv_ruleArgumentDefinition= ruleArgumentDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getArgumentDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleArgumentDefinition=ruleArgumentDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleArgumentDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleArgumentDefinition"


    // $ANTLR start "ruleArgumentDefinition"
    // InternalGaml.g:4100:1: ruleArgumentDefinition returns [EObject current=null] : ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) ;
    public final EObject ruleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject lv_type_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_default_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4106:2: ( ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) )
            // InternalGaml.g:4107:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            {
            // InternalGaml.g:4107:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            // InternalGaml.g:4108:3: ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            {
            // InternalGaml.g:4108:3: ( (lv_type_0_0= ruleTypeRef ) )
            // InternalGaml.g:4109:4: (lv_type_0_0= ruleTypeRef )
            {
            // InternalGaml.g:4109:4: (lv_type_0_0= ruleTypeRef )
            // InternalGaml.g:4110:5: lv_type_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getTypeTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_type_0_0=ruleTypeRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getArgumentDefinitionRule());
              					}
              					set(
              						current,
              						"type",
              						lv_type_0_0,
              						"gaml.compiler.Gaml.TypeRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:4127:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:4128:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:4128:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:4129:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_47);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getArgumentDefinitionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:4146:3: (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==15) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // InternalGaml.g:4147:4: otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) )
                    {
                    otherlv_2=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getArgumentDefinitionAccess().getLessThanSignHyphenMinusKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:4151:4: ( (lv_default_3_0= ruleExpression ) )
                    // InternalGaml.g:4152:5: (lv_default_3_0= ruleExpression )
                    {
                    // InternalGaml.g:4152:5: (lv_default_3_0= ruleExpression )
                    // InternalGaml.g:4153:6: lv_default_3_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getDefaultExpressionParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_default_3_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getArgumentDefinitionRule());
                      						}
                      						set(
                      							current,
                      							"default",
                      							lv_default_3_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleArgumentDefinition"


    // $ANTLR start "entryRuleFacet"
    // InternalGaml.g:4175:1: entryRuleFacet returns [EObject current=null] : iv_ruleFacet= ruleFacet EOF ;
    public final EObject entryRuleFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFacet = null;


        try {
            // InternalGaml.g:4175:46: (iv_ruleFacet= ruleFacet EOF )
            // InternalGaml.g:4176:2: iv_ruleFacet= ruleFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFacet=ruleFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFacet"


    // $ANTLR start "ruleFacet"
    // InternalGaml.g:4182:1: ruleFacet returns [EObject current=null] : (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) ;
    public final EObject ruleFacet() throws RecognitionException {
        EObject current = null;

        EObject this_ActionFacet_0 = null;

        EObject this_DefinitionFacet_1 = null;

        EObject this_ClassicFacet_2 = null;

        EObject this_TypeFacet_3 = null;

        EObject this_VarFacet_4 = null;

        EObject this_FunctionFacet_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:4188:2: ( (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) )
            // InternalGaml.g:4189:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            {
            // InternalGaml.g:4189:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            int alt61=6;
            switch ( input.LA(1) ) {
            case 26:
            case 131:
                {
                alt61=1;
                }
                break;
            case 22:
            case 109:
                {
                alt61=2;
                }
                break;
            case RULE_ID:
            case 15:
            case 34:
            case 35:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
                {
                alt61=3;
                }
                break;
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
                {
                alt61=4;
                }
                break;
            case 132:
                {
                alt61=5;
                }
                break;
            case 133:
                {
                alt61=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }

            switch (alt61) {
                case 1 :
                    // InternalGaml.g:4190:3: this_ActionFacet_0= ruleActionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getActionFacetParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ActionFacet_0=ruleActionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ActionFacet_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4199:3: this_DefinitionFacet_1= ruleDefinitionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getDefinitionFacetParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_DefinitionFacet_1=ruleDefinitionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_DefinitionFacet_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:4208:3: this_ClassicFacet_2= ruleClassicFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getClassicFacetParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ClassicFacet_2=ruleClassicFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ClassicFacet_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:4217:3: this_TypeFacet_3= ruleTypeFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getTypeFacetParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_TypeFacet_3=ruleTypeFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_TypeFacet_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:4226:3: this_VarFacet_4= ruleVarFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getVarFacetParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_VarFacet_4=ruleVarFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_VarFacet_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:4235:3: this_FunctionFacet_5= ruleFunctionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getFunctionFacetParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_FunctionFacet_5=ruleFunctionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_FunctionFacet_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFacet"


    // $ANTLR start "entryRuleFirstFacetKey"
    // InternalGaml.g:4247:1: entryRuleFirstFacetKey returns [String current=null] : iv_ruleFirstFacetKey= ruleFirstFacetKey EOF ;
    public final String entryRuleFirstFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleFirstFacetKey = null;


        try {
            // InternalGaml.g:4247:53: (iv_ruleFirstFacetKey= ruleFirstFacetKey EOF )
            // InternalGaml.g:4248:2: iv_ruleFirstFacetKey= ruleFirstFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFirstFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFirstFacetKey=ruleFirstFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFirstFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFirstFacetKey"


    // $ANTLR start "ruleFirstFacetKey"
    // InternalGaml.g:4254:1: ruleFirstFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DefinitionFacetKey_0= ruleDefinitionFacetKey | this_TypeFacetKey_1= ruleTypeFacetKey | this_SpecialFacetKey_2= ruleSpecialFacetKey | this_VarFacetKey_3= ruleVarFacetKey | this_ActionFacetKey_4= ruleActionFacetKey | this_ClassicFacetKey_5= ruleClassicFacetKey ) ;
    public final AntlrDatatypeRuleToken ruleFirstFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        AntlrDatatypeRuleToken this_DefinitionFacetKey_0 = null;

        AntlrDatatypeRuleToken this_TypeFacetKey_1 = null;

        AntlrDatatypeRuleToken this_SpecialFacetKey_2 = null;

        AntlrDatatypeRuleToken this_VarFacetKey_3 = null;

        AntlrDatatypeRuleToken this_ActionFacetKey_4 = null;

        AntlrDatatypeRuleToken this_ClassicFacetKey_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:4260:2: ( (this_DefinitionFacetKey_0= ruleDefinitionFacetKey | this_TypeFacetKey_1= ruleTypeFacetKey | this_SpecialFacetKey_2= ruleSpecialFacetKey | this_VarFacetKey_3= ruleVarFacetKey | this_ActionFacetKey_4= ruleActionFacetKey | this_ClassicFacetKey_5= ruleClassicFacetKey ) )
            // InternalGaml.g:4261:2: (this_DefinitionFacetKey_0= ruleDefinitionFacetKey | this_TypeFacetKey_1= ruleTypeFacetKey | this_SpecialFacetKey_2= ruleSpecialFacetKey | this_VarFacetKey_3= ruleVarFacetKey | this_ActionFacetKey_4= ruleActionFacetKey | this_ClassicFacetKey_5= ruleClassicFacetKey )
            {
            // InternalGaml.g:4261:2: (this_DefinitionFacetKey_0= ruleDefinitionFacetKey | this_TypeFacetKey_1= ruleTypeFacetKey | this_SpecialFacetKey_2= ruleSpecialFacetKey | this_VarFacetKey_3= ruleVarFacetKey | this_ActionFacetKey_4= ruleActionFacetKey | this_ClassicFacetKey_5= ruleClassicFacetKey )
            int alt62=6;
            switch ( input.LA(1) ) {
            case 22:
            case 109:
                {
                alt62=1;
                }
                break;
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
                {
                alt62=2;
                }
                break;
            case 34:
            case 35:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
                {
                alt62=3;
                }
                break;
            case 132:
                {
                alt62=4;
                }
                break;
            case 26:
            case 131:
                {
                alt62=5;
                }
                break;
            case RULE_ID:
                {
                alt62=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }

            switch (alt62) {
                case 1 :
                    // InternalGaml.g:4262:3: this_DefinitionFacetKey_0= ruleDefinitionFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getDefinitionFacetKeyParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_DefinitionFacetKey_0=ruleDefinitionFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_DefinitionFacetKey_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4273:3: this_TypeFacetKey_1= ruleTypeFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getTypeFacetKeyParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_TypeFacetKey_1=ruleTypeFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_TypeFacetKey_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:4284:3: this_SpecialFacetKey_2= ruleSpecialFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getSpecialFacetKeyParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_SpecialFacetKey_2=ruleSpecialFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_SpecialFacetKey_2);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:4295:3: this_VarFacetKey_3= ruleVarFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getVarFacetKeyParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_VarFacetKey_3=ruleVarFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_VarFacetKey_3);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:4306:3: this_ActionFacetKey_4= ruleActionFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getActionFacetKeyParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ActionFacetKey_4=ruleActionFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ActionFacetKey_4);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:4317:3: this_ClassicFacetKey_5= ruleClassicFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFirstFacetKeyAccess().getClassicFacetKeyParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ClassicFacetKey_5=ruleClassicFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ClassicFacetKey_5);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFirstFacetKey"


    // $ANTLR start "entryRuleClassicFacetKey"
    // InternalGaml.g:4331:1: entryRuleClassicFacetKey returns [String current=null] : iv_ruleClassicFacetKey= ruleClassicFacetKey EOF ;
    public final String entryRuleClassicFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleClassicFacetKey = null;


        try {
            // InternalGaml.g:4331:55: (iv_ruleClassicFacetKey= ruleClassicFacetKey EOF )
            // InternalGaml.g:4332:2: iv_ruleClassicFacetKey= ruleClassicFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getClassicFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleClassicFacetKey=ruleClassicFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleClassicFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleClassicFacetKey"


    // $ANTLR start "ruleClassicFacetKey"
    // InternalGaml.g:4338:1: ruleClassicFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID kw= ':' ) ;
    public final AntlrDatatypeRuleToken ruleClassicFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4344:2: ( (this_ID_0= RULE_ID kw= ':' ) )
            // InternalGaml.g:4345:2: (this_ID_0= RULE_ID kw= ':' )
            {
            // InternalGaml.g:4345:2: (this_ID_0= RULE_ID kw= ':' )
            // InternalGaml.g:4346:3: this_ID_0= RULE_ID kw= ':'
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_33); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(this_ID_0);
              		
            }
            if ( state.backtracking==0 ) {

              			newLeafNode(this_ID_0, grammarAccess.getClassicFacetKeyAccess().getIDTerminalRuleCall_0());
              		
            }
            kw=(Token)match(input,36,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getClassicFacetKeyAccess().getColonKeyword_1());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleClassicFacetKey"


    // $ANTLR start "entryRuleDefinitionFacetKey"
    // InternalGaml.g:4362:1: entryRuleDefinitionFacetKey returns [String current=null] : iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF ;
    public final String entryRuleDefinitionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleDefinitionFacetKey = null;


        try {
            // InternalGaml.g:4362:58: (iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF )
            // InternalGaml.g:4363:2: iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getDefinitionFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleDefinitionFacetKey=ruleDefinitionFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleDefinitionFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDefinitionFacetKey"


    // $ANTLR start "ruleDefinitionFacetKey"
    // InternalGaml.g:4369:1: ruleDefinitionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'name:' | kw= 'returns:' ) ;
    public final AntlrDatatypeRuleToken ruleDefinitionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4375:2: ( (kw= 'name:' | kw= 'returns:' ) )
            // InternalGaml.g:4376:2: (kw= 'name:' | kw= 'returns:' )
            {
            // InternalGaml.g:4376:2: (kw= 'name:' | kw= 'returns:' )
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==22) ) {
                alt63=1;
            }
            else if ( (LA63_0==109) ) {
                alt63=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // InternalGaml.g:4377:3: kw= 'name:'
                    {
                    kw=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getDefinitionFacetKeyAccess().getNameKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4383:3: kw= 'returns:'
                    {
                    kw=(Token)match(input,109,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getDefinitionFacetKeyAccess().getReturnsKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDefinitionFacetKey"


    // $ANTLR start "entryRuleTypeFacetKey"
    // InternalGaml.g:4392:1: entryRuleTypeFacetKey returns [String current=null] : iv_ruleTypeFacetKey= ruleTypeFacetKey EOF ;
    public final String entryRuleTypeFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleTypeFacetKey = null;


        try {
            // InternalGaml.g:4392:52: (iv_ruleTypeFacetKey= ruleTypeFacetKey EOF )
            // InternalGaml.g:4393:2: iv_ruleTypeFacetKey= ruleTypeFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeFacetKey=ruleTypeFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeFacetKey"


    // $ANTLR start "ruleTypeFacetKey"
    // InternalGaml.g:4399:1: ruleTypeFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) ;
    public final AntlrDatatypeRuleToken ruleTypeFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4405:2: ( (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) )
            // InternalGaml.g:4406:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            {
            // InternalGaml.g:4406:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            int alt64=5;
            switch ( input.LA(1) ) {
            case 110:
                {
                alt64=1;
                }
                break;
            case 111:
                {
                alt64=2;
                }
                break;
            case 112:
                {
                alt64=3;
                }
                break;
            case 113:
                {
                alt64=4;
                }
                break;
            case 114:
                {
                alt64=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                throw nvae;
            }

            switch (alt64) {
                case 1 :
                    // InternalGaml.g:4407:3: kw= 'as:'
                    {
                    kw=(Token)match(input,110,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getAsKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4413:3: kw= 'of:'
                    {
                    kw=(Token)match(input,111,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getOfKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:4419:3: kw= 'parent:'
                    {
                    kw=(Token)match(input,112,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getParentKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:4425:3: kw= 'species:'
                    {
                    kw=(Token)match(input,113,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getSpeciesKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:4431:3: kw= 'type:'
                    {
                    kw=(Token)match(input,114,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getTypeKeyword_4());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeFacetKey"


    // $ANTLR start "entryRuleSpecialFacetKey"
    // InternalGaml.g:4440:1: entryRuleSpecialFacetKey returns [String current=null] : iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF ;
    public final String entryRuleSpecialFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSpecialFacetKey = null;


        try {
            // InternalGaml.g:4440:55: (iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF )
            // InternalGaml.g:4441:2: iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSpecialFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSpecialFacetKey=ruleSpecialFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSpecialFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSpecialFacetKey"


    // $ANTLR start "ruleSpecialFacetKey"
    // InternalGaml.g:4447:1: ruleSpecialFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) ;
    public final AntlrDatatypeRuleToken ruleSpecialFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4453:2: ( (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) )
            // InternalGaml.g:4454:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            {
            // InternalGaml.g:4454:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            int alt65=18;
            switch ( input.LA(1) ) {
            case 115:
                {
                alt65=1;
                }
                break;
            case 116:
                {
                alt65=2;
                }
                break;
            case 35:
                {
                alt65=3;
                }
                break;
            case 117:
                {
                alt65=4;
                }
                break;
            case 34:
                {
                alt65=5;
                }
                break;
            case 118:
                {
                alt65=6;
                }
                break;
            case 119:
                {
                alt65=7;
                }
                break;
            case 120:
                {
                alt65=8;
                }
                break;
            case 121:
                {
                alt65=9;
                }
                break;
            case 122:
                {
                alt65=10;
                }
                break;
            case 123:
                {
                alt65=11;
                }
                break;
            case 124:
                {
                alt65=12;
                }
                break;
            case 125:
                {
                alt65=13;
                }
                break;
            case 126:
                {
                alt65=14;
                }
                break;
            case 127:
                {
                alt65=15;
                }
                break;
            case 128:
                {
                alt65=16;
                }
                break;
            case 129:
                {
                alt65=17;
                }
                break;
            case 130:
                {
                alt65=18;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }

            switch (alt65) {
                case 1 :
                    // InternalGaml.g:4455:3: kw= 'camera:'
                    {
                    kw=(Token)match(input,115,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getCameraKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4461:3: kw= 'data:'
                    {
                    kw=(Token)match(input,116,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getDataKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:4467:3: (kw= 'when' kw= ':' )
                    {
                    // InternalGaml.g:4467:3: (kw= 'when' kw= ':' )
                    // InternalGaml.g:4468:4: kw= 'when' kw= ':'
                    {
                    kw=(Token)match(input,35,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getWhenKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,36,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getColonKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:4480:3: kw= 'const:'
                    {
                    kw=(Token)match(input,117,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getConstKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:4486:3: kw= 'value:'
                    {
                    kw=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getValueKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:4492:3: kw= 'topology:'
                    {
                    kw=(Token)match(input,118,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTopologyKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:4498:3: kw= 'item:'
                    {
                    kw=(Token)match(input,119,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getItemKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:4504:3: kw= 'init:'
                    {
                    kw=(Token)match(input,120,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getInitKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:4510:3: kw= 'message:'
                    {
                    kw=(Token)match(input,121,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getMessageKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:4516:3: kw= 'control:'
                    {
                    kw=(Token)match(input,122,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getControlKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:4522:3: kw= 'layout:'
                    {
                    kw=(Token)match(input,123,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getLayoutKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:4528:3: kw= 'environment:'
                    {
                    kw=(Token)match(input,124,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getEnvironmentKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:4534:3: kw= 'text:'
                    {
                    kw=(Token)match(input,125,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTextKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:4540:3: kw= 'image:'
                    {
                    kw=(Token)match(input,126,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getImageKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:4546:3: kw= 'using:'
                    {
                    kw=(Token)match(input,127,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getUsingKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:4552:3: kw= 'parameter:'
                    {
                    kw=(Token)match(input,128,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getParameterKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:4558:3: kw= 'aspect:'
                    {
                    kw=(Token)match(input,129,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getAspectKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:4564:3: kw= 'light:'
                    {
                    kw=(Token)match(input,130,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getLightKeyword_17());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSpecialFacetKey"


    // $ANTLR start "entryRuleActionFacetKey"
    // InternalGaml.g:4573:1: entryRuleActionFacetKey returns [String current=null] : iv_ruleActionFacetKey= ruleActionFacetKey EOF ;
    public final String entryRuleActionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleActionFacetKey = null;


        try {
            // InternalGaml.g:4573:54: (iv_ruleActionFacetKey= ruleActionFacetKey EOF )
            // InternalGaml.g:4574:2: iv_ruleActionFacetKey= ruleActionFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionFacetKey=ruleActionFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionFacetKey"


    // $ANTLR start "ruleActionFacetKey"
    // InternalGaml.g:4580:1: ruleActionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'action:' | kw= 'on_change:' ) ;
    public final AntlrDatatypeRuleToken ruleActionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4586:2: ( (kw= 'action:' | kw= 'on_change:' ) )
            // InternalGaml.g:4587:2: (kw= 'action:' | kw= 'on_change:' )
            {
            // InternalGaml.g:4587:2: (kw= 'action:' | kw= 'on_change:' )
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==26) ) {
                alt66=1;
            }
            else if ( (LA66_0==131) ) {
                alt66=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                throw nvae;
            }
            switch (alt66) {
                case 1 :
                    // InternalGaml.g:4588:3: kw= 'action:'
                    {
                    kw=(Token)match(input,26,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getActionFacetKeyAccess().getActionKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4594:3: kw= 'on_change:'
                    {
                    kw=(Token)match(input,131,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getActionFacetKeyAccess().getOn_changeKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionFacetKey"


    // $ANTLR start "entryRuleVarFacetKey"
    // InternalGaml.g:4603:1: entryRuleVarFacetKey returns [String current=null] : iv_ruleVarFacetKey= ruleVarFacetKey EOF ;
    public final String entryRuleVarFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleVarFacetKey = null;


        try {
            // InternalGaml.g:4603:51: (iv_ruleVarFacetKey= ruleVarFacetKey EOF )
            // InternalGaml.g:4604:2: iv_ruleVarFacetKey= ruleVarFacetKey EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getVarFacetKeyRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleVarFacetKey=ruleVarFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleVarFacetKey.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVarFacetKey"


    // $ANTLR start "ruleVarFacetKey"
    // InternalGaml.g:4610:1: ruleVarFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'var:' ;
    public final AntlrDatatypeRuleToken ruleVarFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4616:2: (kw= 'var:' )
            // InternalGaml.g:4617:2: kw= 'var:'
            {
            kw=(Token)match(input,132,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getVarFacetKeyAccess().getVarKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVarFacetKey"


    // $ANTLR start "entryRuleClassicFacet"
    // InternalGaml.g:4625:1: entryRuleClassicFacet returns [EObject current=null] : iv_ruleClassicFacet= ruleClassicFacet EOF ;
    public final EObject entryRuleClassicFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassicFacet = null;


        try {
            // InternalGaml.g:4625:53: (iv_ruleClassicFacet= ruleClassicFacet EOF )
            // InternalGaml.g:4626:2: iv_ruleClassicFacet= ruleClassicFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getClassicFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleClassicFacet=ruleClassicFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleClassicFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleClassicFacet"


    // $ANTLR start "ruleClassicFacet"
    // InternalGaml.g:4632:1: ruleClassicFacet returns [EObject current=null] : ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) ;
    public final EObject ruleClassicFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_key_2_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4638:2: ( ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) )
            // InternalGaml.g:4639:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            {
            // InternalGaml.g:4639:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            // InternalGaml.g:4640:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) )
            {
            // InternalGaml.g:4640:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) )
            int alt67=3;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt67=1;
                }
                break;
            case 15:
                {
                alt67=2;
                }
                break;
            case 34:
            case 35:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
                {
                alt67=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 67, 0, input);

                throw nvae;
            }

            switch (alt67) {
                case 1 :
                    // InternalGaml.g:4641:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    {
                    // InternalGaml.g:4641:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    // InternalGaml.g:4642:5: (lv_key_0_0= ruleClassicFacetKey )
                    {
                    // InternalGaml.g:4642:5: (lv_key_0_0= ruleClassicFacetKey )
                    // InternalGaml.g:4643:6: lv_key_0_0= ruleClassicFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getClassicFacetAccess().getKeyClassicFacetKeyParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_key_0_0=ruleClassicFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getClassicFacetRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_0,
                      							"gaml.compiler.Gaml.ClassicFacetKey");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:4661:4: ( (lv_key_1_0= '<-' ) )
                    {
                    // InternalGaml.g:4661:4: ( (lv_key_1_0= '<-' ) )
                    // InternalGaml.g:4662:5: (lv_key_1_0= '<-' )
                    {
                    // InternalGaml.g:4662:5: (lv_key_1_0= '<-' )
                    // InternalGaml.g:4663:6: lv_key_1_0= '<-'
                    {
                    lv_key_1_0=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_1_0, grammarAccess.getClassicFacetAccess().getKeyLessThanSignHyphenMinusKeyword_0_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getClassicFacetRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_1_0, "<-");
                      					
                    }

                    }


                    }


                    }
                    break;
                case 3 :
                    // InternalGaml.g:4676:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    {
                    // InternalGaml.g:4676:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    // InternalGaml.g:4677:5: (lv_key_2_0= ruleSpecialFacetKey )
                    {
                    // InternalGaml.g:4677:5: (lv_key_2_0= ruleSpecialFacetKey )
                    // InternalGaml.g:4678:6: lv_key_2_0= ruleSpecialFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getClassicFacetAccess().getKeySpecialFacetKeyParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_key_2_0=ruleSpecialFacetKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getClassicFacetRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_2_0,
                      							"gaml.compiler.Gaml.SpecialFacetKey");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalGaml.g:4696:3: ( (lv_expr_3_0= ruleExpression ) )
            // InternalGaml.g:4697:4: (lv_expr_3_0= ruleExpression )
            {
            // InternalGaml.g:4697:4: (lv_expr_3_0= ruleExpression )
            // InternalGaml.g:4698:5: lv_expr_3_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getClassicFacetAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_3_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getClassicFacetRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_3_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleClassicFacet"


    // $ANTLR start "entryRuleDefinitionFacet"
    // InternalGaml.g:4719:1: entryRuleDefinitionFacet returns [EObject current=null] : iv_ruleDefinitionFacet= ruleDefinitionFacet EOF ;
    public final EObject entryRuleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDefinitionFacet = null;


        try {
            // InternalGaml.g:4719:56: (iv_ruleDefinitionFacet= ruleDefinitionFacet EOF )
            // InternalGaml.g:4720:2: iv_ruleDefinitionFacet= ruleDefinitionFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getDefinitionFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleDefinitionFacet=ruleDefinitionFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleDefinitionFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDefinitionFacet"


    // $ANTLR start "ruleDefinitionFacet"
    // InternalGaml.g:4726:1: ruleDefinitionFacet returns [EObject current=null] : ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) ;
    public final EObject ruleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:4732:2: ( ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) )
            // InternalGaml.g:4733:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            {
            // InternalGaml.g:4733:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            // InternalGaml.g:4734:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            {
            // InternalGaml.g:4734:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) )
            // InternalGaml.g:4735:4: ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey )
            {
            // InternalGaml.g:4736:4: (lv_key_0_0= ruleDefinitionFacetKey )
            // InternalGaml.g:4737:5: lv_key_0_0= ruleDefinitionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getDefinitionFacetAccess().getKeyDefinitionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_17);
            lv_key_0_0=ruleDefinitionFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getDefinitionFacetRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml.DefinitionFacetKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:4754:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:4755:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:4755:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:4756:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:4756:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( (LA68_0==RULE_ID||LA68_0==45||(LA68_0>=47 && LA68_0<=49)||(LA68_0>=53 && LA68_0<=101)) ) {
                alt68=1;
            }
            else if ( (LA68_0==RULE_STRING) ) {
                alt68=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                throw nvae;
            }
            switch (alt68) {
                case 1 :
                    // InternalGaml.g:4757:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getDefinitionFacetAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getDefinitionFacetRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_1_1,
                      							"gaml.compiler.Gaml.Valid_ID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4773:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getDefinitionFacetAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getDefinitionFacetRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"name",
                      							lv_name_1_2,
                      							"gaml.compiler.Gaml.STRING");
                      					
                    }

                    }
                    break;

            }


            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDefinitionFacet"


    // $ANTLR start "entryRuleFunctionFacet"
    // InternalGaml.g:4794:1: entryRuleFunctionFacet returns [EObject current=null] : iv_ruleFunctionFacet= ruleFunctionFacet EOF ;
    public final EObject entryRuleFunctionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunctionFacet = null;


        try {
            // InternalGaml.g:4794:54: (iv_ruleFunctionFacet= ruleFunctionFacet EOF )
            // InternalGaml.g:4795:2: iv_ruleFunctionFacet= ruleFunctionFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFunctionFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFunctionFacet=ruleFunctionFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFunctionFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFunctionFacet"


    // $ANTLR start "ruleFunctionFacet"
    // InternalGaml.g:4801:1: ruleFunctionFacet returns [EObject current=null] : ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) ;
    public final EObject ruleFunctionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_expr_1_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4807:2: ( ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) )
            // InternalGaml.g:4808:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            {
            // InternalGaml.g:4808:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            // InternalGaml.g:4809:3: ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            {
            // InternalGaml.g:4809:3: ( (lv_key_0_0= '->' ) )
            // InternalGaml.g:4810:4: (lv_key_0_0= '->' )
            {
            // InternalGaml.g:4810:4: (lv_key_0_0= '->' )
            // InternalGaml.g:4811:5: lv_key_0_0= '->'
            {
            lv_key_0_0=(Token)match(input,133,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getFunctionFacetAccess().getKeyHyphenMinusGreaterThanSignKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getFunctionFacetRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "->");
              				
            }

            }


            }

            // InternalGaml.g:4823:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            int alt69=2;
            alt69 = dfa69.predict(input);
            switch (alt69) {
                case 1 :
                    // InternalGaml.g:4824:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    {
                    // InternalGaml.g:4824:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    // InternalGaml.g:4825:5: ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) )
                    {
                    // InternalGaml.g:4831:5: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:4832:6: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:4832:6: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:4833:7: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getFunctionFacetAccess().getExprExpressionParserRuleCall_1_0_0_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_expr_1_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getFunctionFacetRule());
                      							}
                      							set(
                      								current,
                      								"expr",
                      								lv_expr_1_0,
                      								"gaml.compiler.Gaml.Expression");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:4852:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    {
                    // InternalGaml.g:4852:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    // InternalGaml.g:4853:5: otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}'
                    {
                    otherlv_2=(Token)match(input,40,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getFunctionFacetAccess().getLeftCurlyBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:4857:5: ( (lv_expr_3_0= ruleExpression ) )
                    // InternalGaml.g:4858:6: (lv_expr_3_0= ruleExpression )
                    {
                    // InternalGaml.g:4858:6: (lv_expr_3_0= ruleExpression )
                    // InternalGaml.g:4859:7: lv_expr_3_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getFunctionFacetAccess().getExprExpressionParserRuleCall_1_1_1_0());
                      						
                    }
                    pushFollow(FOLLOW_48);
                    lv_expr_3_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getFunctionFacetRule());
                      							}
                      							set(
                      								current,
                      								"expr",
                      								lv_expr_3_0,
                      								"gaml.compiler.Gaml.Expression");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getFunctionFacetAccess().getRightCurlyBracketKeyword_1_1_2());
                      				
                    }

                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFunctionFacet"


    // $ANTLR start "entryRuleTypeFacet"
    // InternalGaml.g:4886:1: entryRuleTypeFacet returns [EObject current=null] : iv_ruleTypeFacet= ruleTypeFacet EOF ;
    public final EObject entryRuleTypeFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFacet = null;


        try {
            // InternalGaml.g:4886:50: (iv_ruleTypeFacet= ruleTypeFacet EOF )
            // InternalGaml.g:4887:2: iv_ruleTypeFacet= ruleTypeFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeFacet=ruleTypeFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeFacet"


    // $ANTLR start "ruleTypeFacet"
    // InternalGaml.g:4893:1: ruleTypeFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) ;
    public final EObject ruleTypeFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4899:2: ( ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) )
            // InternalGaml.g:4900:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            {
            // InternalGaml.g:4900:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:4901:3: ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:4901:3: ( (lv_key_0_0= ruleTypeFacetKey ) )
            // InternalGaml.g:4902:4: (lv_key_0_0= ruleTypeFacetKey )
            {
            // InternalGaml.g:4902:4: (lv_key_0_0= ruleTypeFacetKey )
            // InternalGaml.g:4903:5: lv_key_0_0= ruleTypeFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getTypeFacetAccess().getKeyTypeFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=ruleTypeFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getTypeFacetRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml.TypeFacetKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:4920:3: ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            int alt70=2;
            alt70 = dfa70.predict(input);
            switch (alt70) {
                case 1 :
                    // InternalGaml.g:4921:4: ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    {
                    // InternalGaml.g:4921:4: ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    // InternalGaml.g:4922:5: ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) )
                    {
                    // InternalGaml.g:4923:5: ( (lv_expr_1_0= ruleTypeRef ) )
                    // InternalGaml.g:4924:6: (lv_expr_1_0= ruleTypeRef )
                    {
                    // InternalGaml.g:4924:6: (lv_expr_1_0= ruleTypeRef )
                    // InternalGaml.g:4925:7: lv_expr_1_0= ruleTypeRef
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getTypeFacetAccess().getExprTypeRefParserRuleCall_1_0_0_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_expr_1_0=ruleTypeRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getTypeFacetRule());
                      							}
                      							set(
                      								current,
                      								"expr",
                      								lv_expr_1_0,
                      								"gaml.compiler.Gaml.TypeRef");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:4944:4: ( (lv_expr_2_0= ruleExpression ) )
                    {
                    // InternalGaml.g:4944:4: ( (lv_expr_2_0= ruleExpression ) )
                    // InternalGaml.g:4945:5: (lv_expr_2_0= ruleExpression )
                    {
                    // InternalGaml.g:4945:5: (lv_expr_2_0= ruleExpression )
                    // InternalGaml.g:4946:6: lv_expr_2_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getTypeFacetAccess().getExprExpressionParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_expr_2_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getTypeFacetRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_2_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeFacet"


    // $ANTLR start "entryRuleActionFacet"
    // InternalGaml.g:4968:1: entryRuleActionFacet returns [EObject current=null] : iv_ruleActionFacet= ruleActionFacet EOF ;
    public final EObject entryRuleActionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFacet = null;


        try {
            // InternalGaml.g:4968:52: (iv_ruleActionFacet= ruleActionFacet EOF )
            // InternalGaml.g:4969:2: iv_ruleActionFacet= ruleActionFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionFacet=ruleActionFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionFacet"


    // $ANTLR start "ruleActionFacet"
    // InternalGaml.g:4975:1: ruleActionFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) ;
    public final EObject ruleActionFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4981:2: ( ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) )
            // InternalGaml.g:4982:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            {
            // InternalGaml.g:4982:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            // InternalGaml.g:4983:3: ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            {
            // InternalGaml.g:4983:3: ( (lv_key_0_0= ruleActionFacetKey ) )
            // InternalGaml.g:4984:4: (lv_key_0_0= ruleActionFacetKey )
            {
            // InternalGaml.g:4984:4: (lv_key_0_0= ruleActionFacetKey )
            // InternalGaml.g:4985:5: lv_key_0_0= ruleActionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionFacetAccess().getKeyActionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_49);
            lv_key_0_0=ruleActionFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getActionFacetRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml.ActionFacetKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:5002:3: ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0==RULE_ID||LA71_0==45||(LA71_0>=47 && LA71_0<=49)||(LA71_0>=53 && LA71_0<=101)) ) {
                alt71=1;
            }
            else if ( (LA71_0==40) ) {
                alt71=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;
            }
            switch (alt71) {
                case 1 :
                    // InternalGaml.g:5003:4: ( (lv_expr_1_0= ruleActionRef ) )
                    {
                    // InternalGaml.g:5003:4: ( (lv_expr_1_0= ruleActionRef ) )
                    // InternalGaml.g:5004:5: (lv_expr_1_0= ruleActionRef )
                    {
                    // InternalGaml.g:5004:5: (lv_expr_1_0= ruleActionRef )
                    // InternalGaml.g:5005:6: lv_expr_1_0= ruleActionRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getActionFacetAccess().getExprActionRefParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_expr_1_0=ruleActionRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getActionFacetRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_1_0,
                      							"gaml.compiler.Gaml.ActionRef");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:5023:4: ( (lv_block_2_0= ruleBlock ) )
                    {
                    // InternalGaml.g:5023:4: ( (lv_block_2_0= ruleBlock ) )
                    // InternalGaml.g:5024:5: (lv_block_2_0= ruleBlock )
                    {
                    // InternalGaml.g:5024:5: (lv_block_2_0= ruleBlock )
                    // InternalGaml.g:5025:6: lv_block_2_0= ruleBlock
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getActionFacetAccess().getBlockBlockParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_block_2_0=ruleBlock();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getActionFacetRule());
                      						}
                      						set(
                      							current,
                      							"block",
                      							lv_block_2_0,
                      							"gaml.compiler.Gaml.Block");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionFacet"


    // $ANTLR start "entryRuleVarFacet"
    // InternalGaml.g:5047:1: entryRuleVarFacet returns [EObject current=null] : iv_ruleVarFacet= ruleVarFacet EOF ;
    public final EObject entryRuleVarFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFacet = null;


        try {
            // InternalGaml.g:5047:49: (iv_ruleVarFacet= ruleVarFacet EOF )
            // InternalGaml.g:5048:2: iv_ruleVarFacet= ruleVarFacet EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getVarFacetRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleVarFacet=ruleVarFacet();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleVarFacet; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVarFacet"


    // $ANTLR start "ruleVarFacet"
    // InternalGaml.g:5054:1: ruleVarFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) ;
    public final EObject ruleVarFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5060:2: ( ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) )
            // InternalGaml.g:5061:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            {
            // InternalGaml.g:5061:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            // InternalGaml.g:5062:3: ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) )
            {
            // InternalGaml.g:5062:3: ( (lv_key_0_0= ruleVarFacetKey ) )
            // InternalGaml.g:5063:4: (lv_key_0_0= ruleVarFacetKey )
            {
            // InternalGaml.g:5063:4: (lv_key_0_0= ruleVarFacetKey )
            // InternalGaml.g:5064:5: lv_key_0_0= ruleVarFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getVarFacetAccess().getKeyVarFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_key_0_0=ruleVarFacetKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getVarFacetRule());
              					}
              					set(
              						current,
              						"key",
              						lv_key_0_0,
              						"gaml.compiler.Gaml.VarFacetKey");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:5081:3: ( (lv_expr_1_0= ruleVariableRef ) )
            // InternalGaml.g:5082:4: (lv_expr_1_0= ruleVariableRef )
            {
            // InternalGaml.g:5082:4: (lv_expr_1_0= ruleVariableRef )
            // InternalGaml.g:5083:5: lv_expr_1_0= ruleVariableRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getVarFacetAccess().getExprVariableRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_1_0=ruleVariableRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getVarFacetRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.VariableRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVarFacet"


    // $ANTLR start "entryRuleBlock"
    // InternalGaml.g:5104:1: entryRuleBlock returns [EObject current=null] : iv_ruleBlock= ruleBlock EOF ;
    public final EObject entryRuleBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBlock = null;


        try {
            // InternalGaml.g:5104:46: (iv_ruleBlock= ruleBlock EOF )
            // InternalGaml.g:5105:2: iv_ruleBlock= ruleBlock EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getBlockRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleBlock=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleBlock; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleBlock"


    // $ANTLR start "ruleBlock"
    // InternalGaml.g:5111:1: ruleBlock returns [EObject current=null] : ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) ;
    public final EObject ruleBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5117:2: ( ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) )
            // InternalGaml.g:5118:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            {
            // InternalGaml.g:5118:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            // InternalGaml.g:5119:3: () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:5119:3: ()
            // InternalGaml.g:5120:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,40,FOLLOW_50); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:5130:3: ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            // InternalGaml.g:5131:4: ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}'
            {
            // InternalGaml.g:5131:4: ( (lv_statements_2_0= ruleStatement ) )*
            loop72:
            do {
                int alt72=2;
                int LA72_0 = input.LA(1);

                if ( ((LA72_0>=RULE_ID && LA72_0<=RULE_KEYWORD)||LA72_0==20||(LA72_0>=27 && LA72_0<=28)||LA72_0==31||LA72_0==33||LA72_0==37||(LA72_0>=39 && LA72_0<=40)||(LA72_0>=44 && LA72_0<=49)||(LA72_0>=53 && LA72_0<=101)||LA72_0==143||(LA72_0>=147 && LA72_0<=149)) ) {
                    alt72=1;
                }


                switch (alt72) {
            	case 1 :
            	    // InternalGaml.g:5132:5: (lv_statements_2_0= ruleStatement )
            	    {
            	    // InternalGaml.g:5132:5: (lv_statements_2_0= ruleStatement )
            	    // InternalGaml.g:5133:6: lv_statements_2_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBlockAccess().getStatementsStatementParserRuleCall_2_0_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_50);
            	    lv_statements_2_0=ruleStatement();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getBlockRule());
            	      						}
            	      						add(
            	      							current,
            	      							"statements",
            	      							lv_statements_2_0,
            	      							"gaml.compiler.Gaml.Statement");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);

            otherlv_3=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              				newLeafNode(otherlv_3, grammarAccess.getBlockAccess().getRightCurlyBracketKeyword_2_1());
              			
            }

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleBlock"


    // $ANTLR start "entryRuleExpression"
    // InternalGaml.g:5159:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalGaml.g:5159:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalGaml.g:5160:2: iv_ruleExpression= ruleExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleExpression=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExpression"


    // $ANTLR start "ruleExpression"
    // InternalGaml.g:5166:1: ruleExpression returns [EObject current=null] : this_Pair_0= rulePair ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        EObject this_Pair_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5172:2: (this_Pair_0= rulePair )
            // InternalGaml.g:5173:2: this_Pair_0= rulePair
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getExpressionAccess().getPairParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_Pair_0=rulePair();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current = this_Pair_0;
              		afterParserOrEnumRuleCall();
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExpression"


    // $ANTLR start "entryRulePair"
    // InternalGaml.g:5184:1: entryRulePair returns [EObject current=null] : iv_rulePair= rulePair EOF ;
    public final EObject entryRulePair() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePair = null;


        try {
            // InternalGaml.g:5184:45: (iv_rulePair= rulePair EOF )
            // InternalGaml.g:5185:2: iv_rulePair= rulePair EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPairRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePair=rulePair();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePair; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePair"


    // $ANTLR start "rulePair"
    // InternalGaml.g:5191:1: rulePair returns [EObject current=null] : (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) ;
    public final EObject rulePair() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_If_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5197:2: ( (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) )
            // InternalGaml.g:5198:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            {
            // InternalGaml.g:5198:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            // InternalGaml.g:5199:3: this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getPairAccess().getIfParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_If_0=ruleIf();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_If_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5207:3: ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==134) ) {
                alt73=1;
            }
            switch (alt73) {
                case 1 :
                    // InternalGaml.g:5208:4: () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) )
                    {
                    // InternalGaml.g:5208:4: ()
                    // InternalGaml.g:5209:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getPairAccess().getBinaryOperatorLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5215:4: ( (lv_op_2_0= '::' ) )
                    // InternalGaml.g:5216:5: (lv_op_2_0= '::' )
                    {
                    // InternalGaml.g:5216:5: (lv_op_2_0= '::' )
                    // InternalGaml.g:5217:6: lv_op_2_0= '::'
                    {
                    lv_op_2_0=(Token)match(input,134,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_2_0, grammarAccess.getPairAccess().getOpColonColonKeyword_1_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getPairRule());
                      						}
                      						setWithLastConsumed(current, "op", lv_op_2_0, "::");
                      					
                    }

                    }


                    }

                    // InternalGaml.g:5229:4: ( (lv_right_3_0= ruleIf ) )
                    // InternalGaml.g:5230:5: (lv_right_3_0= ruleIf )
                    {
                    // InternalGaml.g:5230:5: (lv_right_3_0= ruleIf )
                    // InternalGaml.g:5231:6: lv_right_3_0= ruleIf
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPairAccess().getRightIfParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_right_3_0=ruleIf();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getPairRule());
                      						}
                      						set(
                      							current,
                      							"right",
                      							lv_right_3_0,
                      							"gaml.compiler.Gaml.If");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePair"


    // $ANTLR start "entryRuleIf"
    // InternalGaml.g:5253:1: entryRuleIf returns [EObject current=null] : iv_ruleIf= ruleIf EOF ;
    public final EObject entryRuleIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIf = null;


        try {
            // InternalGaml.g:5253:43: (iv_ruleIf= ruleIf EOF )
            // InternalGaml.g:5254:2: iv_ruleIf= ruleIf EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getIfRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleIf=ruleIf();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleIf; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleIf"


    // $ANTLR start "ruleIf"
    // InternalGaml.g:5260:1: ruleIf returns [EObject current=null] : (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) ;
    public final EObject ruleIf() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        EObject this_Or_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_ifFalse_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5266:2: ( (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) )
            // InternalGaml.g:5267:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            {
            // InternalGaml.g:5267:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            // InternalGaml.g:5268:3: this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getIfAccess().getOrParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_Or_0=ruleOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Or_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5276:3: ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==135) ) {
                alt74=1;
            }
            switch (alt74) {
                case 1 :
                    // InternalGaml.g:5277:4: () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    {
                    // InternalGaml.g:5277:4: ()
                    // InternalGaml.g:5278:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getIfAccess().getIfLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5284:4: ( (lv_op_2_0= '?' ) )
                    // InternalGaml.g:5285:5: (lv_op_2_0= '?' )
                    {
                    // InternalGaml.g:5285:5: (lv_op_2_0= '?' )
                    // InternalGaml.g:5286:6: lv_op_2_0= '?'
                    {
                    lv_op_2_0=(Token)match(input,135,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_2_0, grammarAccess.getIfAccess().getOpQuestionMarkKeyword_1_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getIfRule());
                      						}
                      						setWithLastConsumed(current, "op", lv_op_2_0, "?");
                      					
                    }

                    }


                    }

                    // InternalGaml.g:5298:4: ( (lv_right_3_0= ruleOr ) )
                    // InternalGaml.g:5299:5: (lv_right_3_0= ruleOr )
                    {
                    // InternalGaml.g:5299:5: (lv_right_3_0= ruleOr )
                    // InternalGaml.g:5300:6: lv_right_3_0= ruleOr
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getIfAccess().getRightOrParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_33);
                    lv_right_3_0=ruleOr();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getIfRule());
                      						}
                      						set(
                      							current,
                      							"right",
                      							lv_right_3_0,
                      							"gaml.compiler.Gaml.Or");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalGaml.g:5317:4: (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    // InternalGaml.g:5318:5: otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) )
                    {
                    otherlv_4=(Token)match(input,36,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getIfAccess().getColonKeyword_1_3_0());
                      				
                    }
                    // InternalGaml.g:5322:5: ( (lv_ifFalse_5_0= ruleOr ) )
                    // InternalGaml.g:5323:6: (lv_ifFalse_5_0= ruleOr )
                    {
                    // InternalGaml.g:5323:6: (lv_ifFalse_5_0= ruleOr )
                    // InternalGaml.g:5324:7: lv_ifFalse_5_0= ruleOr
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getIfAccess().getIfFalseOrParserRuleCall_1_3_1_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_ifFalse_5_0=ruleOr();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getIfRule());
                      							}
                      							set(
                      								current,
                      								"ifFalse",
                      								lv_ifFalse_5_0,
                      								"gaml.compiler.Gaml.Or");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleIf"


    // $ANTLR start "entryRuleOr"
    // InternalGaml.g:5347:1: entryRuleOr returns [EObject current=null] : iv_ruleOr= ruleOr EOF ;
    public final EObject entryRuleOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOr = null;


        try {
            // InternalGaml.g:5347:43: (iv_ruleOr= ruleOr EOF )
            // InternalGaml.g:5348:2: iv_ruleOr= ruleOr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOr=ruleOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOr; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOr"


    // $ANTLR start "ruleOr"
    // InternalGaml.g:5354:1: ruleOr returns [EObject current=null] : (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) ;
    public final EObject ruleOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_And_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5360:2: ( (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) )
            // InternalGaml.g:5361:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            {
            // InternalGaml.g:5361:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            // InternalGaml.g:5362:3: this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getOrAccess().getAndParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_53);
            this_And_0=ruleAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_And_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5370:3: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            loop75:
            do {
                int alt75=2;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==136) ) {
                    alt75=1;
                }


                switch (alt75) {
            	case 1 :
            	    // InternalGaml.g:5371:4: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) )
            	    {
            	    // InternalGaml.g:5371:4: ()
            	    // InternalGaml.g:5372:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getOrAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:5378:4: ( (lv_op_2_0= 'or' ) )
            	    // InternalGaml.g:5379:5: (lv_op_2_0= 'or' )
            	    {
            	    // InternalGaml.g:5379:5: (lv_op_2_0= 'or' )
            	    // InternalGaml.g:5380:6: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,136,FOLLOW_5); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						newLeafNode(lv_op_2_0, grammarAccess.getOrAccess().getOpOrKeyword_1_1_0());
            	      					
            	    }
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElement(grammarAccess.getOrRule());
            	      						}
            	      						setWithLastConsumed(current, "op", lv_op_2_0, "or");
            	      					
            	    }

            	    }


            	    }

            	    // InternalGaml.g:5392:4: ( (lv_right_3_0= ruleAnd ) )
            	    // InternalGaml.g:5393:5: (lv_right_3_0= ruleAnd )
            	    {
            	    // InternalGaml.g:5393:5: (lv_right_3_0= ruleAnd )
            	    // InternalGaml.g:5394:6: lv_right_3_0= ruleAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOrAccess().getRightAndParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_53);
            	    lv_right_3_0=ruleAnd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getOrRule());
            	      						}
            	      						set(
            	      							current,
            	      							"right",
            	      							lv_right_3_0,
            	      							"gaml.compiler.Gaml.And");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOr"


    // $ANTLR start "entryRuleAnd"
    // InternalGaml.g:5416:1: entryRuleAnd returns [EObject current=null] : iv_ruleAnd= ruleAnd EOF ;
    public final EObject entryRuleAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAnd = null;


        try {
            // InternalGaml.g:5416:44: (iv_ruleAnd= ruleAnd EOF )
            // InternalGaml.g:5417:2: iv_ruleAnd= ruleAnd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAndRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAnd=ruleAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAnd; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAnd"


    // $ANTLR start "ruleAnd"
    // InternalGaml.g:5423:1: ruleAnd returns [EObject current=null] : (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) ;
    public final EObject ruleAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Cast_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5429:2: ( (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) )
            // InternalGaml.g:5430:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            {
            // InternalGaml.g:5430:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            // InternalGaml.g:5431:3: this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAndAccess().getCastParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_54);
            this_Cast_0=ruleCast();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Cast_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5439:3: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            loop76:
            do {
                int alt76=2;
                int LA76_0 = input.LA(1);

                if ( (LA76_0==137) ) {
                    alt76=1;
                }


                switch (alt76) {
            	case 1 :
            	    // InternalGaml.g:5440:4: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) )
            	    {
            	    // InternalGaml.g:5440:4: ()
            	    // InternalGaml.g:5441:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAndAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:5447:4: ( (lv_op_2_0= 'and' ) )
            	    // InternalGaml.g:5448:5: (lv_op_2_0= 'and' )
            	    {
            	    // InternalGaml.g:5448:5: (lv_op_2_0= 'and' )
            	    // InternalGaml.g:5449:6: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,137,FOLLOW_5); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						newLeafNode(lv_op_2_0, grammarAccess.getAndAccess().getOpAndKeyword_1_1_0());
            	      					
            	    }
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElement(grammarAccess.getAndRule());
            	      						}
            	      						setWithLastConsumed(current, "op", lv_op_2_0, "and");
            	      					
            	    }

            	    }


            	    }

            	    // InternalGaml.g:5461:4: ( (lv_right_3_0= ruleCast ) )
            	    // InternalGaml.g:5462:5: (lv_right_3_0= ruleCast )
            	    {
            	    // InternalGaml.g:5462:5: (lv_right_3_0= ruleCast )
            	    // InternalGaml.g:5463:6: lv_right_3_0= ruleCast
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAndAccess().getRightCastParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_54);
            	    lv_right_3_0=ruleCast();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getAndRule());
            	      						}
            	      						set(
            	      							current,
            	      							"right",
            	      							lv_right_3_0,
            	      							"gaml.compiler.Gaml.Cast");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop76;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAnd"


    // $ANTLR start "entryRuleCast"
    // InternalGaml.g:5485:1: entryRuleCast returns [EObject current=null] : iv_ruleCast= ruleCast EOF ;
    public final EObject entryRuleCast() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCast = null;


        try {
            // InternalGaml.g:5485:45: (iv_ruleCast= ruleCast EOF )
            // InternalGaml.g:5486:2: iv_ruleCast= ruleCast EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getCastRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleCast=ruleCast();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleCast; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCast"


    // $ANTLR start "ruleCast"
    // InternalGaml.g:5492:1: ruleCast returns [EObject current=null] : (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) ;
    public final EObject ruleCast() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject this_Comparison_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_right_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5498:2: ( (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) )
            // InternalGaml.g:5499:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            {
            // InternalGaml.g:5499:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            // InternalGaml.g:5500:3: this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getCastAccess().getComparisonParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_11);
            this_Comparison_0=ruleComparison();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Comparison_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5508:3: ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( (LA78_0==18) ) {
                alt78=1;
            }
            switch (alt78) {
                case 1 :
                    // InternalGaml.g:5509:4: ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    {
                    // InternalGaml.g:5509:4: ( () ( (lv_op_2_0= 'as' ) ) )
                    // InternalGaml.g:5510:5: () ( (lv_op_2_0= 'as' ) )
                    {
                    // InternalGaml.g:5510:5: ()
                    // InternalGaml.g:5511:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getCastAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:5517:5: ( (lv_op_2_0= 'as' ) )
                    // InternalGaml.g:5518:6: (lv_op_2_0= 'as' )
                    {
                    // InternalGaml.g:5518:6: (lv_op_2_0= 'as' )
                    // InternalGaml.g:5519:7: lv_op_2_0= 'as'
                    {
                    lv_op_2_0=(Token)match(input,18,FOLLOW_55); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							newLeafNode(lv_op_2_0, grammarAccess.getCastAccess().getOpAsKeyword_1_0_1_0());
                      						
                    }
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElement(grammarAccess.getCastRule());
                      							}
                      							setWithLastConsumed(current, "op", lv_op_2_0, "as");
                      						
                    }

                    }


                    }


                    }

                    // InternalGaml.g:5532:4: ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    int alt77=2;
                    int LA77_0 = input.LA(1);

                    if ( (LA77_0==RULE_ID||LA77_0==47||LA77_0==49) ) {
                        alt77=1;
                    }
                    else if ( (LA77_0==37) ) {
                        alt77=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 77, 0, input);

                        throw nvae;
                    }
                    switch (alt77) {
                        case 1 :
                            // InternalGaml.g:5533:5: ( (lv_right_3_0= ruleTypeRef ) )
                            {
                            // InternalGaml.g:5533:5: ( (lv_right_3_0= ruleTypeRef ) )
                            // InternalGaml.g:5534:6: (lv_right_3_0= ruleTypeRef )
                            {
                            // InternalGaml.g:5534:6: (lv_right_3_0= ruleTypeRef )
                            // InternalGaml.g:5535:7: lv_right_3_0= ruleTypeRef
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getCastAccess().getRightTypeRefParserRuleCall_1_1_0_0());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_right_3_0=ruleTypeRef();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getCastRule());
                              							}
                              							set(
                              								current,
                              								"right",
                              								lv_right_3_0,
                              								"gaml.compiler.Gaml.TypeRef");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }


                            }
                            break;
                        case 2 :
                            // InternalGaml.g:5553:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            {
                            // InternalGaml.g:5553:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            // InternalGaml.g:5554:6: otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')'
                            {
                            otherlv_4=(Token)match(input,37,FOLLOW_35); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(otherlv_4, grammarAccess.getCastAccess().getLeftParenthesisKeyword_1_1_1_0());
                              					
                            }
                            // InternalGaml.g:5558:6: ( (lv_right_5_0= ruleTypeRef ) )
                            // InternalGaml.g:5559:7: (lv_right_5_0= ruleTypeRef )
                            {
                            // InternalGaml.g:5559:7: (lv_right_5_0= ruleTypeRef )
                            // InternalGaml.g:5560:8: lv_right_5_0= ruleTypeRef
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getCastAccess().getRightTypeRefParserRuleCall_1_1_1_1_0());
                              							
                            }
                            pushFollow(FOLLOW_36);
                            lv_right_5_0=ruleTypeRef();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElementForParent(grammarAccess.getCastRule());
                              								}
                              								set(
                              									current,
                              									"right",
                              									lv_right_5_0,
                              									"gaml.compiler.Gaml.TypeRef");
                              								afterParserOrEnumRuleCall();
                              							
                            }

                            }


                            }

                            otherlv_6=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(otherlv_6, grammarAccess.getCastAccess().getRightParenthesisKeyword_1_1_1_2());
                              					
                            }

                            }


                            }
                            break;

                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCast"


    // $ANTLR start "entryRuleComparison"
    // InternalGaml.g:5588:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
    public final EObject entryRuleComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComparison = null;


        try {
            // InternalGaml.g:5588:51: (iv_ruleComparison= ruleComparison EOF )
            // InternalGaml.g:5589:2: iv_ruleComparison= ruleComparison EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getComparisonRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleComparison=ruleComparison();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleComparison; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleComparison"


    // $ANTLR start "ruleComparison"
    // InternalGaml.g:5595:1: ruleComparison returns [EObject current=null] : (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) ;
    public final EObject ruleComparison() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        Token lv_op_2_4=null;
        Token lv_op_2_5=null;
        Token lv_op_2_6=null;
        EObject this_Addition_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5601:2: ( (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) )
            // InternalGaml.g:5602:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            {
            // InternalGaml.g:5602:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            // InternalGaml.g:5603:3: this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getComparisonAccess().getAdditionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_56);
            this_Addition_0=ruleAddition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Addition_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5611:3: ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==42||(LA80_0>=138 && LA80_0<=141)) ) {
                alt80=1;
            }
            else if ( (LA80_0==103) ) {
                int LA80_2 = input.LA(2);

                if ( ((LA80_2>=RULE_ID && LA80_2<=RULE_KEYWORD)||LA80_2==20||LA80_2==37||LA80_2==40||LA80_2==45||(LA80_2>=47 && LA80_2<=49)||(LA80_2>=53 && LA80_2<=101)||LA80_2==143||(LA80_2>=147 && LA80_2<=149)) ) {
                    alt80=1;
                }
            }
            switch (alt80) {
                case 1 :
                    // InternalGaml.g:5612:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) )
                    {
                    // InternalGaml.g:5612:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) )
                    // InternalGaml.g:5613:5: () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    {
                    // InternalGaml.g:5613:5: ()
                    // InternalGaml.g:5614:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getComparisonAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:5620:5: ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    // InternalGaml.g:5621:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    {
                    // InternalGaml.g:5621:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    // InternalGaml.g:5622:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    {
                    // InternalGaml.g:5622:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    int alt79=6;
                    switch ( input.LA(1) ) {
                    case 138:
                        {
                        alt79=1;
                        }
                        break;
                    case 42:
                        {
                        alt79=2;
                        }
                        break;
                    case 139:
                        {
                        alt79=3;
                        }
                        break;
                    case 140:
                        {
                        alt79=4;
                        }
                        break;
                    case 141:
                        {
                        alt79=5;
                        }
                        break;
                    case 103:
                        {
                        alt79=6;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 79, 0, input);

                        throw nvae;
                    }

                    switch (alt79) {
                        case 1 :
                            // InternalGaml.g:5623:8: lv_op_2_1= '!='
                            {
                            lv_op_2_1=(Token)match(input,138,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_1, grammarAccess.getComparisonAccess().getOpExclamationMarkEqualsSignKeyword_1_0_1_0_0());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_1, null);
                              							
                            }

                            }
                            break;
                        case 2 :
                            // InternalGaml.g:5634:8: lv_op_2_2= '='
                            {
                            lv_op_2_2=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_2, grammarAccess.getComparisonAccess().getOpEqualsSignKeyword_1_0_1_0_1());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_2, null);
                              							
                            }

                            }
                            break;
                        case 3 :
                            // InternalGaml.g:5645:8: lv_op_2_3= '>='
                            {
                            lv_op_2_3=(Token)match(input,139,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_3, grammarAccess.getComparisonAccess().getOpGreaterThanSignEqualsSignKeyword_1_0_1_0_2());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_3, null);
                              							
                            }

                            }
                            break;
                        case 4 :
                            // InternalGaml.g:5656:8: lv_op_2_4= '<='
                            {
                            lv_op_2_4=(Token)match(input,140,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_4, grammarAccess.getComparisonAccess().getOpLessThanSignEqualsSignKeyword_1_0_1_0_3());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_4, null);
                              							
                            }

                            }
                            break;
                        case 5 :
                            // InternalGaml.g:5667:8: lv_op_2_5= '<'
                            {
                            lv_op_2_5=(Token)match(input,141,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_5, grammarAccess.getComparisonAccess().getOpLessThanSignKeyword_1_0_1_0_4());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_5, null);
                              							
                            }

                            }
                            break;
                        case 6 :
                            // InternalGaml.g:5678:8: lv_op_2_6= '>'
                            {
                            lv_op_2_6=(Token)match(input,103,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_6, grammarAccess.getComparisonAccess().getOpGreaterThanSignKeyword_1_0_1_0_5());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getComparisonRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_6, null);
                              							
                            }

                            }
                            break;

                    }


                    }


                    }


                    }

                    // InternalGaml.g:5692:4: ( (lv_right_3_0= ruleAddition ) )
                    // InternalGaml.g:5693:5: (lv_right_3_0= ruleAddition )
                    {
                    // InternalGaml.g:5693:5: (lv_right_3_0= ruleAddition )
                    // InternalGaml.g:5694:6: lv_right_3_0= ruleAddition
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getComparisonAccess().getRightAdditionParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_right_3_0=ruleAddition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getComparisonRule());
                      						}
                      						set(
                      							current,
                      							"right",
                      							lv_right_3_0,
                      							"gaml.compiler.Gaml.Addition");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleComparison"


    // $ANTLR start "entryRuleAddition"
    // InternalGaml.g:5716:1: entryRuleAddition returns [EObject current=null] : iv_ruleAddition= ruleAddition EOF ;
    public final EObject entryRuleAddition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddition = null;


        try {
            // InternalGaml.g:5716:49: (iv_ruleAddition= ruleAddition EOF )
            // InternalGaml.g:5717:2: iv_ruleAddition= ruleAddition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAdditionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAddition=ruleAddition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAddition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAddition"


    // $ANTLR start "ruleAddition"
    // InternalGaml.g:5723:1: ruleAddition returns [EObject current=null] : (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) ;
    public final EObject ruleAddition() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_Multiplication_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5729:2: ( (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) )
            // InternalGaml.g:5730:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            {
            // InternalGaml.g:5730:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            // InternalGaml.g:5731:3: this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAdditionAccess().getMultiplicationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_57);
            this_Multiplication_0=ruleMultiplication();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Multiplication_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5739:3: ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            loop82:
            do {
                int alt82=2;
                int LA82_0 = input.LA(1);

                if ( ((LA82_0>=142 && LA82_0<=143)) ) {
                    alt82=1;
                }


                switch (alt82) {
            	case 1 :
            	    // InternalGaml.g:5740:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) )
            	    {
            	    // InternalGaml.g:5740:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) )
            	    // InternalGaml.g:5741:5: () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    {
            	    // InternalGaml.g:5741:5: ()
            	    // InternalGaml.g:5742:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getAdditionAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5748:5: ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    // InternalGaml.g:5749:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    {
            	    // InternalGaml.g:5749:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    // InternalGaml.g:5750:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    {
            	    // InternalGaml.g:5750:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    int alt81=2;
            	    int LA81_0 = input.LA(1);

            	    if ( (LA81_0==142) ) {
            	        alt81=1;
            	    }
            	    else if ( (LA81_0==143) ) {
            	        alt81=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 81, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt81) {
            	        case 1 :
            	            // InternalGaml.g:5751:8: lv_op_2_1= '+'
            	            {
            	            lv_op_2_1=(Token)match(input,142,FOLLOW_5); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_1, grammarAccess.getAdditionAccess().getOpPlusSignKeyword_1_0_1_0_0());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getAdditionRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_1, null);
            	              							
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:5762:8: lv_op_2_2= '-'
            	            {
            	            lv_op_2_2=(Token)match(input,143,FOLLOW_5); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_2, grammarAccess.getAdditionAccess().getOpHyphenMinusKeyword_1_0_1_0_1());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getAdditionRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_2, null);
            	              							
            	            }

            	            }
            	            break;

            	    }


            	    }


            	    }


            	    }

            	    // InternalGaml.g:5776:4: ( (lv_right_3_0= ruleMultiplication ) )
            	    // InternalGaml.g:5777:5: (lv_right_3_0= ruleMultiplication )
            	    {
            	    // InternalGaml.g:5777:5: (lv_right_3_0= ruleMultiplication )
            	    // InternalGaml.g:5778:6: lv_right_3_0= ruleMultiplication
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAdditionAccess().getRightMultiplicationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_57);
            	    lv_right_3_0=ruleMultiplication();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getAdditionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"right",
            	      							lv_right_3_0,
            	      							"gaml.compiler.Gaml.Multiplication");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop82;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAddition"


    // $ANTLR start "entryRuleMultiplication"
    // InternalGaml.g:5800:1: entryRuleMultiplication returns [EObject current=null] : iv_ruleMultiplication= ruleMultiplication EOF ;
    public final EObject entryRuleMultiplication() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMultiplication = null;


        try {
            // InternalGaml.g:5800:55: (iv_ruleMultiplication= ruleMultiplication EOF )
            // InternalGaml.g:5801:2: iv_ruleMultiplication= ruleMultiplication EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMultiplicationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMultiplication=ruleMultiplication();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMultiplication; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMultiplication"


    // $ANTLR start "ruleMultiplication"
    // InternalGaml.g:5807:1: ruleMultiplication returns [EObject current=null] : (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) ;
    public final EObject ruleMultiplication() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        EObject this_Binary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5813:2: ( (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) )
            // InternalGaml.g:5814:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            {
            // InternalGaml.g:5814:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            // InternalGaml.g:5815:3: this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getMultiplicationAccess().getBinaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_58);
            this_Binary_0=ruleBinary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Binary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5823:3: ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            loop84:
            do {
                int alt84=2;
                int LA84_0 = input.LA(1);

                if ( ((LA84_0>=144 && LA84_0<=146)) ) {
                    alt84=1;
                }


                switch (alt84) {
            	case 1 :
            	    // InternalGaml.g:5824:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) )
            	    {
            	    // InternalGaml.g:5824:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) )
            	    // InternalGaml.g:5825:5: () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    {
            	    // InternalGaml.g:5825:5: ()
            	    // InternalGaml.g:5826:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getMultiplicationAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5832:5: ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    // InternalGaml.g:5833:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    {
            	    // InternalGaml.g:5833:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    // InternalGaml.g:5834:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    {
            	    // InternalGaml.g:5834:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    int alt83=3;
            	    switch ( input.LA(1) ) {
            	    case 144:
            	        {
            	        alt83=1;
            	        }
            	        break;
            	    case 145:
            	        {
            	        alt83=2;
            	        }
            	        break;
            	    case 146:
            	        {
            	        alt83=3;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 83, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt83) {
            	        case 1 :
            	            // InternalGaml.g:5835:8: lv_op_2_1= '*'
            	            {
            	            lv_op_2_1=(Token)match(input,144,FOLLOW_5); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_1, grammarAccess.getMultiplicationAccess().getOpAsteriskKeyword_1_0_1_0_0());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getMultiplicationRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_1, null);
            	              							
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:5846:8: lv_op_2_2= '/'
            	            {
            	            lv_op_2_2=(Token)match(input,145,FOLLOW_5); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_2, grammarAccess.getMultiplicationAccess().getOpSolidusKeyword_1_0_1_0_1());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getMultiplicationRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_2, null);
            	              							
            	            }

            	            }
            	            break;
            	        case 3 :
            	            // InternalGaml.g:5857:8: lv_op_2_3= '^'
            	            {
            	            lv_op_2_3=(Token)match(input,146,FOLLOW_5); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_3, grammarAccess.getMultiplicationAccess().getOpCircumflexAccentKeyword_1_0_1_0_2());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getMultiplicationRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_3, null);
            	              							
            	            }

            	            }
            	            break;

            	    }


            	    }


            	    }


            	    }

            	    // InternalGaml.g:5871:4: ( (lv_right_3_0= ruleBinary ) )
            	    // InternalGaml.g:5872:5: (lv_right_3_0= ruleBinary )
            	    {
            	    // InternalGaml.g:5872:5: (lv_right_3_0= ruleBinary )
            	    // InternalGaml.g:5873:6: lv_right_3_0= ruleBinary
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getMultiplicationAccess().getRightBinaryParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_58);
            	    lv_right_3_0=ruleBinary();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getMultiplicationRule());
            	      						}
            	      						set(
            	      							current,
            	      							"right",
            	      							lv_right_3_0,
            	      							"gaml.compiler.Gaml.Binary");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop84;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMultiplication"


    // $ANTLR start "entryRuleBinary"
    // InternalGaml.g:5895:1: entryRuleBinary returns [EObject current=null] : iv_ruleBinary= ruleBinary EOF ;
    public final EObject entryRuleBinary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBinary = null;


        try {
            // InternalGaml.g:5895:47: (iv_ruleBinary= ruleBinary EOF )
            // InternalGaml.g:5896:2: iv_ruleBinary= ruleBinary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getBinaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleBinary=ruleBinary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleBinary; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleBinary"


    // $ANTLR start "ruleBinary"
    // InternalGaml.g:5902:1: ruleBinary returns [EObject current=null] : (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) ;
    public final EObject ruleBinary() throws RecognitionException {
        EObject current = null;

        EObject this_Unit_0 = null;

        AntlrDatatypeRuleToken lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5908:2: ( (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) )
            // InternalGaml.g:5909:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            {
            // InternalGaml.g:5909:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            // InternalGaml.g:5910:3: this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getBinaryAccess().getUnitParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_59);
            this_Unit_0=ruleUnit();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unit_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5918:3: ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            loop85:
            do {
                int alt85=2;
                int LA85_0 = input.LA(1);

                if ( (LA85_0==RULE_ID) ) {
                    int LA85_2 = input.LA(2);

                    if ( ((LA85_2>=RULE_ID && LA85_2<=RULE_KEYWORD)||LA85_2==20||LA85_2==37||LA85_2==40||LA85_2==45||(LA85_2>=47 && LA85_2<=49)||(LA85_2>=53 && LA85_2<=101)||LA85_2==143||(LA85_2>=147 && LA85_2<=149)) ) {
                        alt85=1;
                    }


                }
                else if ( (LA85_0==45||(LA85_0>=47 && LA85_0<=49)||(LA85_0>=53 && LA85_0<=101)) ) {
                    alt85=1;
                }


                switch (alt85) {
            	case 1 :
            	    // InternalGaml.g:5919:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) )
            	    {
            	    // InternalGaml.g:5919:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) )
            	    // InternalGaml.g:5920:5: () ( (lv_op_2_0= ruleValid_ID ) )
            	    {
            	    // InternalGaml.g:5920:5: ()
            	    // InternalGaml.g:5921:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getBinaryAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5927:5: ( (lv_op_2_0= ruleValid_ID ) )
            	    // InternalGaml.g:5928:6: (lv_op_2_0= ruleValid_ID )
            	    {
            	    // InternalGaml.g:5928:6: (lv_op_2_0= ruleValid_ID )
            	    // InternalGaml.g:5929:7: lv_op_2_0= ruleValid_ID
            	    {
            	    if ( state.backtracking==0 ) {

            	      							newCompositeNode(grammarAccess.getBinaryAccess().getOpValid_IDParserRuleCall_1_0_1_0());
            	      						
            	    }
            	    pushFollow(FOLLOW_5);
            	    lv_op_2_0=ruleValid_ID();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      							if (current==null) {
            	      								current = createModelElementForParent(grammarAccess.getBinaryRule());
            	      							}
            	      							set(
            	      								current,
            	      								"op",
            	      								lv_op_2_0,
            	      								"gaml.compiler.Gaml.Valid_ID");
            	      							afterParserOrEnumRuleCall();
            	      						
            	    }

            	    }


            	    }


            	    }

            	    // InternalGaml.g:5947:4: ( (lv_right_3_0= ruleUnit ) )
            	    // InternalGaml.g:5948:5: (lv_right_3_0= ruleUnit )
            	    {
            	    // InternalGaml.g:5948:5: (lv_right_3_0= ruleUnit )
            	    // InternalGaml.g:5949:6: lv_right_3_0= ruleUnit
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBinaryAccess().getRightUnitParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_59);
            	    lv_right_3_0=ruleUnit();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getBinaryRule());
            	      						}
            	      						set(
            	      							current,
            	      							"right",
            	      							lv_right_3_0,
            	      							"gaml.compiler.Gaml.Unit");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop85;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleBinary"


    // $ANTLR start "entryRuleUnit"
    // InternalGaml.g:5971:1: entryRuleUnit returns [EObject current=null] : iv_ruleUnit= ruleUnit EOF ;
    public final EObject entryRuleUnit() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnit = null;


        try {
            // InternalGaml.g:5971:45: (iv_ruleUnit= ruleUnit EOF )
            // InternalGaml.g:5972:2: iv_ruleUnit= ruleUnit EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getUnitRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleUnit=ruleUnit();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleUnit; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUnit"


    // $ANTLR start "ruleUnit"
    // InternalGaml.g:5978:1: ruleUnit returns [EObject current=null] : (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) ;
    public final EObject ruleUnit() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Unary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5984:2: ( (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) )
            // InternalGaml.g:5985:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            {
            // InternalGaml.g:5985:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            // InternalGaml.g:5986:3: this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getUnitAccess().getUnaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_60);
            this_Unary_0=ruleUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5994:3: ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            int alt86=2;
            int LA86_0 = input.LA(1);

            if ( (LA86_0==147) ) {
                alt86=1;
            }
            switch (alt86) {
                case 1 :
                    // InternalGaml.g:5995:4: ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) )
                    {
                    // InternalGaml.g:5995:4: ( () ( (lv_op_2_0= '#' ) ) )
                    // InternalGaml.g:5996:5: () ( (lv_op_2_0= '#' ) )
                    {
                    // InternalGaml.g:5996:5: ()
                    // InternalGaml.g:5997:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getUnitAccess().getUnitLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:6003:5: ( (lv_op_2_0= '#' ) )
                    // InternalGaml.g:6004:6: (lv_op_2_0= '#' )
                    {
                    // InternalGaml.g:6004:6: (lv_op_2_0= '#' )
                    // InternalGaml.g:6005:7: lv_op_2_0= '#'
                    {
                    lv_op_2_0=(Token)match(input,147,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							newLeafNode(lv_op_2_0, grammarAccess.getUnitAccess().getOpNumberSignKeyword_1_0_1_0());
                      						
                    }
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElement(grammarAccess.getUnitRule());
                      							}
                      							setWithLastConsumed(current, "op", lv_op_2_0, "#");
                      						
                    }

                    }


                    }


                    }

                    // InternalGaml.g:6018:4: ( (lv_right_3_0= ruleUnitRef ) )
                    // InternalGaml.g:6019:5: (lv_right_3_0= ruleUnitRef )
                    {
                    // InternalGaml.g:6019:5: (lv_right_3_0= ruleUnitRef )
                    // InternalGaml.g:6020:6: lv_right_3_0= ruleUnitRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getUnitAccess().getRightUnitRefParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_right_3_0=ruleUnitRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getUnitRule());
                      						}
                      						set(
                      							current,
                      							"right",
                      							lv_right_3_0,
                      							"gaml.compiler.Gaml.UnitRef");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUnit"


    // $ANTLR start "entryRuleUnary"
    // InternalGaml.g:6042:1: entryRuleUnary returns [EObject current=null] : iv_ruleUnary= ruleUnary EOF ;
    public final EObject entryRuleUnary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnary = null;


        try {
            // InternalGaml.g:6042:46: (iv_ruleUnary= ruleUnary EOF )
            // InternalGaml.g:6043:2: iv_ruleUnary= ruleUnary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getUnaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleUnary=ruleUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleUnary; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUnary"


    // $ANTLR start "ruleUnary"
    // InternalGaml.g:6049:1: ruleUnary returns [EObject current=null] : (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) ;
    public final EObject ruleUnary() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token lv_op_4_1=null;
        Token lv_op_4_2=null;
        Token lv_op_4_3=null;
        EObject this_Access_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_right_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6055:2: ( (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) )
            // InternalGaml.g:6056:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            {
            // InternalGaml.g:6056:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( ((LA89_0>=RULE_ID && LA89_0<=RULE_KEYWORD)||LA89_0==20||LA89_0==37||LA89_0==40||LA89_0==45||(LA89_0>=47 && LA89_0<=49)||(LA89_0>=53 && LA89_0<=101)) ) {
                alt89=1;
            }
            else if ( (LA89_0==143||(LA89_0>=147 && LA89_0<=149)) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // InternalGaml.g:6057:3: this_Access_0= ruleAccess
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getUnaryAccess().getAccessParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_Access_0=ruleAccess();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_Access_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:6066:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    {
                    // InternalGaml.g:6066:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    // InternalGaml.g:6067:4: () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    {
                    // InternalGaml.g:6067:4: ()
                    // InternalGaml.g:6068:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getUnaryAccess().getUnaryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6074:4: ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    int alt88=2;
                    int LA88_0 = input.LA(1);

                    if ( (LA88_0==147) ) {
                        alt88=1;
                    }
                    else if ( (LA88_0==143||(LA88_0>=148 && LA88_0<=149)) ) {
                        alt88=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 88, 0, input);

                        throw nvae;
                    }
                    switch (alt88) {
                        case 1 :
                            // InternalGaml.g:6075:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            {
                            // InternalGaml.g:6075:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            // InternalGaml.g:6076:6: ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) )
                            {
                            // InternalGaml.g:6076:6: ( (lv_op_2_0= '#' ) )
                            // InternalGaml.g:6077:7: (lv_op_2_0= '#' )
                            {
                            // InternalGaml.g:6077:7: (lv_op_2_0= '#' )
                            // InternalGaml.g:6078:8: lv_op_2_0= '#'
                            {
                            lv_op_2_0=(Token)match(input,147,FOLLOW_12); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								newLeafNode(lv_op_2_0, grammarAccess.getUnaryAccess().getOpNumberSignKeyword_1_1_0_0_0());
                              							
                            }
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElement(grammarAccess.getUnaryRule());
                              								}
                              								setWithLastConsumed(current, "op", lv_op_2_0, "#");
                              							
                            }

                            }


                            }

                            // InternalGaml.g:6090:6: ( (lv_right_3_0= ruleUnitRef ) )
                            // InternalGaml.g:6091:7: (lv_right_3_0= ruleUnitRef )
                            {
                            // InternalGaml.g:6091:7: (lv_right_3_0= ruleUnitRef )
                            // InternalGaml.g:6092:8: lv_right_3_0= ruleUnitRef
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getUnaryAccess().getRightUnitRefParserRuleCall_1_1_0_1_0());
                              							
                            }
                            pushFollow(FOLLOW_2);
                            lv_right_3_0=ruleUnitRef();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElementForParent(grammarAccess.getUnaryRule());
                              								}
                              								set(
                              									current,
                              									"right",
                              									lv_right_3_0,
                              									"gaml.compiler.Gaml.UnitRef");
                              								afterParserOrEnumRuleCall();
                              							
                            }

                            }


                            }


                            }


                            }
                            break;
                        case 2 :
                            // InternalGaml.g:6111:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            {
                            // InternalGaml.g:6111:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            // InternalGaml.g:6112:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) )
                            {
                            // InternalGaml.g:6112:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) )
                            // InternalGaml.g:6113:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            {
                            // InternalGaml.g:6113:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            // InternalGaml.g:6114:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            {
                            // InternalGaml.g:6114:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            int alt87=3;
                            switch ( input.LA(1) ) {
                            case 143:
                                {
                                alt87=1;
                                }
                                break;
                            case 148:
                                {
                                alt87=2;
                                }
                                break;
                            case 149:
                                {
                                alt87=3;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return current;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 87, 0, input);

                                throw nvae;
                            }

                            switch (alt87) {
                                case 1 :
                                    // InternalGaml.g:6115:9: lv_op_4_1= '-'
                                    {
                                    lv_op_4_1=(Token)match(input,143,FOLLOW_5); if (state.failed) return current;
                                    if ( state.backtracking==0 ) {

                                      									newLeafNode(lv_op_4_1, grammarAccess.getUnaryAccess().getOpHyphenMinusKeyword_1_1_1_0_0_0());
                                      								
                                    }
                                    if ( state.backtracking==0 ) {

                                      									if (current==null) {
                                      										current = createModelElement(grammarAccess.getUnaryRule());
                                      									}
                                      									setWithLastConsumed(current, "op", lv_op_4_1, null);
                                      								
                                    }

                                    }
                                    break;
                                case 2 :
                                    // InternalGaml.g:6126:9: lv_op_4_2= '!'
                                    {
                                    lv_op_4_2=(Token)match(input,148,FOLLOW_5); if (state.failed) return current;
                                    if ( state.backtracking==0 ) {

                                      									newLeafNode(lv_op_4_2, grammarAccess.getUnaryAccess().getOpExclamationMarkKeyword_1_1_1_0_0_1());
                                      								
                                    }
                                    if ( state.backtracking==0 ) {

                                      									if (current==null) {
                                      										current = createModelElement(grammarAccess.getUnaryRule());
                                      									}
                                      									setWithLastConsumed(current, "op", lv_op_4_2, null);
                                      								
                                    }

                                    }
                                    break;
                                case 3 :
                                    // InternalGaml.g:6137:9: lv_op_4_3= 'not'
                                    {
                                    lv_op_4_3=(Token)match(input,149,FOLLOW_5); if (state.failed) return current;
                                    if ( state.backtracking==0 ) {

                                      									newLeafNode(lv_op_4_3, grammarAccess.getUnaryAccess().getOpNotKeyword_1_1_1_0_0_2());
                                      								
                                    }
                                    if ( state.backtracking==0 ) {

                                      									if (current==null) {
                                      										current = createModelElement(grammarAccess.getUnaryRule());
                                      									}
                                      									setWithLastConsumed(current, "op", lv_op_4_3, null);
                                      								
                                    }

                                    }
                                    break;

                            }


                            }


                            }

                            // InternalGaml.g:6150:6: ( (lv_right_5_0= ruleUnary ) )
                            // InternalGaml.g:6151:7: (lv_right_5_0= ruleUnary )
                            {
                            // InternalGaml.g:6151:7: (lv_right_5_0= ruleUnary )
                            // InternalGaml.g:6152:8: lv_right_5_0= ruleUnary
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getUnaryAccess().getRightUnaryParserRuleCall_1_1_1_1_0());
                              							
                            }
                            pushFollow(FOLLOW_2);
                            lv_right_5_0=ruleUnary();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              								if (current==null) {
                              									current = createModelElementForParent(grammarAccess.getUnaryRule());
                              								}
                              								set(
                              									current,
                              									"right",
                              									lv_right_5_0,
                              									"gaml.compiler.Gaml.Unary");
                              								afterParserOrEnumRuleCall();
                              							
                            }

                            }


                            }


                            }


                            }
                            break;

                    }


                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUnary"


    // $ANTLR start "entryRuleAccess"
    // InternalGaml.g:6176:1: entryRuleAccess returns [EObject current=null] : iv_ruleAccess= ruleAccess EOF ;
    public final EObject entryRuleAccess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAccess = null;


        try {
            // InternalGaml.g:6176:47: (iv_ruleAccess= ruleAccess EOF )
            // InternalGaml.g:6177:2: iv_ruleAccess= ruleAccess EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAccessRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAccess=ruleAccess();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAccess; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAccess"


    // $ANTLR start "ruleAccess"
    // InternalGaml.g:6183:1: ruleAccess returns [EObject current=null] : (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) ;
    public final EObject ruleAccess() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        Token lv_op_5_0=null;
        EObject this_Primary_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_right_6_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6189:2: ( (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) )
            // InternalGaml.g:6190:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            {
            // InternalGaml.g:6190:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            // InternalGaml.g:6191:3: this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAccessAccess().getPrimaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_61);
            this_Primary_0=rulePrimary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Primary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:6199:3: ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            loop92:
            do {
                int alt92=2;
                int LA92_0 = input.LA(1);

                if ( (LA92_0==20||LA92_0==150) ) {
                    alt92=1;
                }


                switch (alt92) {
            	case 1 :
            	    // InternalGaml.g:6200:4: () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    {
            	    // InternalGaml.g:6200:4: ()
            	    // InternalGaml.g:6201:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAccessAccess().getAccessLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:6207:4: ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    int alt91=2;
            	    int LA91_0 = input.LA(1);

            	    if ( (LA91_0==20) ) {
            	        alt91=1;
            	    }
            	    else if ( (LA91_0==150) ) {
            	        alt91=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 91, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt91) {
            	        case 1 :
            	            // InternalGaml.g:6208:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            {
            	            // InternalGaml.g:6208:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            // InternalGaml.g:6209:6: ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']'
            	            {
            	            // InternalGaml.g:6209:6: ( (lv_op_2_0= '[' ) )
            	            // InternalGaml.g:6210:7: (lv_op_2_0= '[' )
            	            {
            	            // InternalGaml.g:6210:7: (lv_op_2_0= '[' )
            	            // InternalGaml.g:6211:8: lv_op_2_0= '['
            	            {
            	            lv_op_2_0=(Token)match(input,20,FOLLOW_14); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_2_0, grammarAccess.getAccessAccess().getOpLeftSquareBracketKeyword_1_1_0_0_0());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getAccessRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_2_0, "[");
            	              							
            	            }

            	            }


            	            }

            	            // InternalGaml.g:6223:6: ( (lv_right_3_0= ruleExpressionList ) )?
            	            int alt90=2;
            	            int LA90_0 = input.LA(1);

            	            if ( ((LA90_0>=RULE_ID && LA90_0<=RULE_KEYWORD)||LA90_0==20||LA90_0==22||LA90_0==26||(LA90_0>=34 && LA90_0<=35)||LA90_0==37||LA90_0==40||LA90_0==45||(LA90_0>=47 && LA90_0<=49)||(LA90_0>=53 && LA90_0<=101)||(LA90_0>=109 && LA90_0<=132)||LA90_0==143||(LA90_0>=147 && LA90_0<=149)) ) {
            	                alt90=1;
            	            }
            	            switch (alt90) {
            	                case 1 :
            	                    // InternalGaml.g:6224:7: (lv_right_3_0= ruleExpressionList )
            	                    {
            	                    // InternalGaml.g:6224:7: (lv_right_3_0= ruleExpressionList )
            	                    // InternalGaml.g:6225:8: lv_right_3_0= ruleExpressionList
            	                    {
            	                    if ( state.backtracking==0 ) {

            	                      								newCompositeNode(grammarAccess.getAccessAccess().getRightExpressionListParserRuleCall_1_1_0_1_0());
            	                      							
            	                    }
            	                    pushFollow(FOLLOW_15);
            	                    lv_right_3_0=ruleExpressionList();

            	                    state._fsp--;
            	                    if (state.failed) return current;
            	                    if ( state.backtracking==0 ) {

            	                      								if (current==null) {
            	                      									current = createModelElementForParent(grammarAccess.getAccessRule());
            	                      								}
            	                      								set(
            	                      									current,
            	                      									"right",
            	                      									lv_right_3_0,
            	                      									"gaml.compiler.Gaml.ExpressionList");
            	                      								afterParserOrEnumRuleCall();
            	                      							
            	                    }

            	                    }


            	                    }
            	                    break;

            	            }

            	            otherlv_4=(Token)match(input,21,FOLLOW_61); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						newLeafNode(otherlv_4, grammarAccess.getAccessAccess().getRightSquareBracketKeyword_1_1_0_2());
            	              					
            	            }

            	            }


            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:6248:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            {
            	            // InternalGaml.g:6248:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            // InternalGaml.g:6249:6: ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) )
            	            {
            	            // InternalGaml.g:6249:6: ( (lv_op_5_0= '.' ) )
            	            // InternalGaml.g:6250:7: (lv_op_5_0= '.' )
            	            {
            	            // InternalGaml.g:6250:7: (lv_op_5_0= '.' )
            	            // InternalGaml.g:6251:8: lv_op_5_0= '.'
            	            {
            	            lv_op_5_0=(Token)match(input,150,FOLLOW_62); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								newLeafNode(lv_op_5_0, grammarAccess.getAccessAccess().getOpFullStopKeyword_1_1_1_0_0());
            	              							
            	            }
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElement(grammarAccess.getAccessRule());
            	              								}
            	              								setWithLastConsumed(current, "op", lv_op_5_0, ".");
            	              							
            	            }

            	            }


            	            }

            	            // InternalGaml.g:6263:6: ( (lv_right_6_0= rulePrimary ) )
            	            // InternalGaml.g:6264:7: (lv_right_6_0= rulePrimary )
            	            {
            	            // InternalGaml.g:6264:7: (lv_right_6_0= rulePrimary )
            	            // InternalGaml.g:6265:8: lv_right_6_0= rulePrimary
            	            {
            	            if ( state.backtracking==0 ) {

            	              								newCompositeNode(grammarAccess.getAccessAccess().getRightPrimaryParserRuleCall_1_1_1_1_0());
            	              							
            	            }
            	            pushFollow(FOLLOW_61);
            	            lv_right_6_0=rulePrimary();

            	            state._fsp--;
            	            if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              								if (current==null) {
            	              									current = createModelElementForParent(grammarAccess.getAccessRule());
            	              								}
            	              								set(
            	              									current,
            	              									"right",
            	              									lv_right_6_0,
            	              									"gaml.compiler.Gaml.Primary");
            	              								afterParserOrEnumRuleCall();
            	              							
            	            }

            	            }


            	            }


            	            }


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop92;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAccess"


    // $ANTLR start "entryRulePrimary"
    // InternalGaml.g:6289:1: entryRulePrimary returns [EObject current=null] : iv_rulePrimary= rulePrimary EOF ;
    public final EObject entryRulePrimary() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePrimary = null;


        try {
            // InternalGaml.g:6289:48: (iv_rulePrimary= rulePrimary EOF )
            // InternalGaml.g:6290:2: iv_rulePrimary= rulePrimary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPrimaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePrimary=rulePrimary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePrimary; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePrimary"


    // $ANTLR start "rulePrimary"
    // InternalGaml.g:6296:1: rulePrimary returns [EObject current=null] : (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) ;
    public final EObject rulePrimary() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token lv_op_12_0=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        EObject this_TerminalExpression_0 = null;

        EObject this_AbstractRef_1 = null;

        EObject this_ExpressionList_3 = null;

        EObject lv_exprs_7_0 = null;

        EObject lv_left_11_0 = null;

        EObject lv_right_13_0 = null;

        EObject lv_z_15_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6302:2: ( (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) )
            // InternalGaml.g:6303:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            {
            // InternalGaml.g:6303:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            int alt95=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
            case RULE_INTEGER:
            case RULE_DOUBLE:
            case RULE_BOOLEAN:
            case RULE_KEYWORD:
                {
                alt95=1;
                }
                break;
            case RULE_ID:
            case 45:
            case 47:
            case 48:
            case 49:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
                {
                alt95=2;
                }
                break;
            case 37:
                {
                alt95=3;
                }
                break;
            case 20:
                {
                alt95=4;
                }
                break;
            case 40:
                {
                alt95=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 95, 0, input);

                throw nvae;
            }

            switch (alt95) {
                case 1 :
                    // InternalGaml.g:6304:3: this_TerminalExpression_0= ruleTerminalExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getPrimaryAccess().getTerminalExpressionParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_TerminalExpression_0=ruleTerminalExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_TerminalExpression_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:6313:3: this_AbstractRef_1= ruleAbstractRef
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getPrimaryAccess().getAbstractRefParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AbstractRef_1=ruleAbstractRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AbstractRef_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:6322:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    {
                    // InternalGaml.g:6322:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    // InternalGaml.g:6323:4: otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,37,FOLLOW_21); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getPrimaryAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getPrimaryAccess().getExpressionListParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_36);
                    this_ExpressionList_3=ruleExpressionList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ExpressionList_3;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_4=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getPrimaryAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:6341:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    {
                    // InternalGaml.g:6341:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    // InternalGaml.g:6342:4: otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']'
                    {
                    otherlv_5=(Token)match(input,20,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getPrimaryAccess().getLeftSquareBracketKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:6346:4: ()
                    // InternalGaml.g:6347:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getArrayAction_3_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6353:4: ( (lv_exprs_7_0= ruleExpressionList ) )?
                    int alt93=2;
                    int LA93_0 = input.LA(1);

                    if ( ((LA93_0>=RULE_ID && LA93_0<=RULE_KEYWORD)||LA93_0==20||LA93_0==22||LA93_0==26||(LA93_0>=34 && LA93_0<=35)||LA93_0==37||LA93_0==40||LA93_0==45||(LA93_0>=47 && LA93_0<=49)||(LA93_0>=53 && LA93_0<=101)||(LA93_0>=109 && LA93_0<=132)||LA93_0==143||(LA93_0>=147 && LA93_0<=149)) ) {
                        alt93=1;
                    }
                    switch (alt93) {
                        case 1 :
                            // InternalGaml.g:6354:5: (lv_exprs_7_0= ruleExpressionList )
                            {
                            // InternalGaml.g:6354:5: (lv_exprs_7_0= ruleExpressionList )
                            // InternalGaml.g:6355:6: lv_exprs_7_0= ruleExpressionList
                            {
                            if ( state.backtracking==0 ) {

                              						newCompositeNode(grammarAccess.getPrimaryAccess().getExprsExpressionListParserRuleCall_3_2_0());
                              					
                            }
                            pushFollow(FOLLOW_15);
                            lv_exprs_7_0=ruleExpressionList();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						if (current==null) {
                              							current = createModelElementForParent(grammarAccess.getPrimaryRule());
                              						}
                              						set(
                              							current,
                              							"exprs",
                              							lv_exprs_7_0,
                              							"gaml.compiler.Gaml.ExpressionList");
                              						afterParserOrEnumRuleCall();
                              					
                            }

                            }


                            }
                            break;

                    }

                    otherlv_8=(Token)match(input,21,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getPrimaryAccess().getRightSquareBracketKeyword_3_3());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalGaml.g:6378:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    {
                    // InternalGaml.g:6378:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    // InternalGaml.g:6379:4: otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}'
                    {
                    otherlv_9=(Token)match(input,40,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getPrimaryAccess().getLeftCurlyBracketKeyword_4_0());
                      			
                    }
                    // InternalGaml.g:6383:4: ()
                    // InternalGaml.g:6384:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getPointAction_4_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6390:4: ( (lv_left_11_0= ruleExpression ) )
                    // InternalGaml.g:6391:5: (lv_left_11_0= ruleExpression )
                    {
                    // InternalGaml.g:6391:5: (lv_left_11_0= ruleExpression )
                    // InternalGaml.g:6392:6: lv_left_11_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getLeftExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_63);
                    lv_left_11_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getPrimaryRule());
                      						}
                      						set(
                      							current,
                      							"left",
                      							lv_left_11_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalGaml.g:6409:4: ( (lv_op_12_0= ',' ) )
                    // InternalGaml.g:6410:5: (lv_op_12_0= ',' )
                    {
                    // InternalGaml.g:6410:5: (lv_op_12_0= ',' )
                    // InternalGaml.g:6411:6: lv_op_12_0= ','
                    {
                    lv_op_12_0=(Token)match(input,108,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_12_0, grammarAccess.getPrimaryAccess().getOpCommaKeyword_4_3_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getPrimaryRule());
                      						}
                      						setWithLastConsumed(current, "op", lv_op_12_0, ",");
                      					
                    }

                    }


                    }

                    // InternalGaml.g:6423:4: ( (lv_right_13_0= ruleExpression ) )
                    // InternalGaml.g:6424:5: (lv_right_13_0= ruleExpression )
                    {
                    // InternalGaml.g:6424:5: (lv_right_13_0= ruleExpression )
                    // InternalGaml.g:6425:6: lv_right_13_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getRightExpressionParserRuleCall_4_4_0());
                      					
                    }
                    pushFollow(FOLLOW_64);
                    lv_right_13_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getPrimaryRule());
                      						}
                      						set(
                      							current,
                      							"right",
                      							lv_right_13_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalGaml.g:6442:4: (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )?
                    int alt94=2;
                    int LA94_0 = input.LA(1);

                    if ( (LA94_0==108) ) {
                        alt94=1;
                    }
                    switch (alt94) {
                        case 1 :
                            // InternalGaml.g:6443:5: otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) )
                            {
                            otherlv_14=(Token)match(input,108,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getPrimaryAccess().getCommaKeyword_4_5_0());
                              				
                            }
                            // InternalGaml.g:6447:5: ( (lv_z_15_0= ruleExpression ) )
                            // InternalGaml.g:6448:6: (lv_z_15_0= ruleExpression )
                            {
                            // InternalGaml.g:6448:6: (lv_z_15_0= ruleExpression )
                            // InternalGaml.g:6449:7: lv_z_15_0= ruleExpression
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPrimaryAccess().getZExpressionParserRuleCall_4_5_1_0());
                              						
                            }
                            pushFollow(FOLLOW_48);
                            lv_z_15_0=ruleExpression();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getPrimaryRule());
                              							}
                              							set(
                              								current,
                              								"z",
                              								lv_z_15_0,
                              								"gaml.compiler.Gaml.Expression");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }


                            }
                            break;

                    }

                    otherlv_16=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getPrimaryAccess().getRightCurlyBracketKeyword_4_6());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePrimary"


    // $ANTLR start "entryRuleAbstractRef"
    // InternalGaml.g:6476:1: entryRuleAbstractRef returns [EObject current=null] : iv_ruleAbstractRef= ruleAbstractRef EOF ;
    public final EObject entryRuleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAbstractRef = null;


        try {
            // InternalGaml.g:6476:52: (iv_ruleAbstractRef= ruleAbstractRef EOF )
            // InternalGaml.g:6477:2: iv_ruleAbstractRef= ruleAbstractRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAbstractRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAbstractRef=ruleAbstractRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAbstractRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAbstractRef"


    // $ANTLR start "ruleAbstractRef"
    // InternalGaml.g:6483:1: ruleAbstractRef returns [EObject current=null] : ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) ;
    public final EObject ruleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject this_Function_0 = null;

        EObject this_VariableRef_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:6489:2: ( ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) )
            // InternalGaml.g:6490:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            {
            // InternalGaml.g:6490:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            int alt96=2;
            alt96 = dfa96.predict(input);
            switch (alt96) {
                case 1 :
                    // InternalGaml.g:6491:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    {
                    // InternalGaml.g:6491:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    // InternalGaml.g:6492:4: ( ruleFunction )=>this_Function_0= ruleFunction
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getAbstractRefAccess().getFunctionParserRuleCall_0());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_Function_0=ruleFunction();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_Function_0;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:6503:3: this_VariableRef_1= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getAbstractRefAccess().getVariableRefParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_VariableRef_1=ruleVariableRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_VariableRef_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAbstractRef"


    // $ANTLR start "entryRuleFunction"
    // InternalGaml.g:6515:1: entryRuleFunction returns [EObject current=null] : iv_ruleFunction= ruleFunction EOF ;
    public final EObject entryRuleFunction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunction = null;


        try {
            // InternalGaml.g:6515:49: (iv_ruleFunction= ruleFunction EOF )
            // InternalGaml.g:6516:2: iv_ruleFunction= ruleFunction EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFunctionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFunction=ruleFunction();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFunction; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFunction"


    // $ANTLR start "ruleFunction"
    // InternalGaml.g:6522:1: ruleFunction returns [EObject current=null] : ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) ;
    public final EObject ruleFunction() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_left_1_0 = null;

        EObject lv_type_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6528:2: ( ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) )
            // InternalGaml.g:6529:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            {
            // InternalGaml.g:6529:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            // InternalGaml.g:6530:3: () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')'
            {
            // InternalGaml.g:6530:3: ()
            // InternalGaml.g:6531:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getFunctionAccess().getFunctionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6537:3: ( (lv_left_1_0= ruleActionRef ) )
            // InternalGaml.g:6538:4: (lv_left_1_0= ruleActionRef )
            {
            // InternalGaml.g:6538:4: (lv_left_1_0= ruleActionRef )
            // InternalGaml.g:6539:5: lv_left_1_0= ruleActionRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFunctionAccess().getLeftActionRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_65);
            lv_left_1_0=ruleActionRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getFunctionRule());
              					}
              					set(
              						current,
              						"left",
              						lv_left_1_0,
              						"gaml.compiler.Gaml.ActionRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:6556:3: ( (lv_type_2_0= ruleTypeInfo ) )?
            int alt97=2;
            int LA97_0 = input.LA(1);

            if ( (LA97_0==141) ) {
                alt97=1;
            }
            switch (alt97) {
                case 1 :
                    // InternalGaml.g:6557:4: (lv_type_2_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:6557:4: (lv_type_2_0= ruleTypeInfo )
                    // InternalGaml.g:6558:5: lv_type_2_0= ruleTypeInfo
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getTypeTypeInfoParserRuleCall_2_0());
                      				
                    }
                    pushFollow(FOLLOW_66);
                    lv_type_2_0=ruleTypeInfo();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getFunctionRule());
                      					}
                      					set(
                      						current,
                      						"type",
                      						lv_type_2_0,
                      						"gaml.compiler.Gaml.TypeInfo");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_3=(Token)match(input,37,FOLLOW_67); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getFunctionAccess().getLeftParenthesisKeyword_3());
              		
            }
            // InternalGaml.g:6579:3: ( (lv_right_4_0= ruleExpressionList ) )?
            int alt98=2;
            int LA98_0 = input.LA(1);

            if ( ((LA98_0>=RULE_ID && LA98_0<=RULE_KEYWORD)||LA98_0==20||LA98_0==22||LA98_0==26||(LA98_0>=34 && LA98_0<=35)||LA98_0==37||LA98_0==40||LA98_0==45||(LA98_0>=47 && LA98_0<=49)||(LA98_0>=53 && LA98_0<=101)||(LA98_0>=109 && LA98_0<=132)||LA98_0==143||(LA98_0>=147 && LA98_0<=149)) ) {
                alt98=1;
            }
            switch (alt98) {
                case 1 :
                    // InternalGaml.g:6580:4: (lv_right_4_0= ruleExpressionList )
                    {
                    // InternalGaml.g:6580:4: (lv_right_4_0= ruleExpressionList )
                    // InternalGaml.g:6581:5: lv_right_4_0= ruleExpressionList
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getRightExpressionListParserRuleCall_4_0());
                      				
                    }
                    pushFollow(FOLLOW_36);
                    lv_right_4_0=ruleExpressionList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getFunctionRule());
                      					}
                      					set(
                      						current,
                      						"right",
                      						lv_right_4_0,
                      						"gaml.compiler.Gaml.ExpressionList");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_5=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_5, grammarAccess.getFunctionAccess().getRightParenthesisKeyword_5());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFunction"


    // $ANTLR start "entryRuleExpressionList"
    // InternalGaml.g:6606:1: entryRuleExpressionList returns [EObject current=null] : iv_ruleExpressionList= ruleExpressionList EOF ;
    public final EObject entryRuleExpressionList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpressionList = null;


        try {
            // InternalGaml.g:6606:55: (iv_ruleExpressionList= ruleExpressionList EOF )
            // InternalGaml.g:6607:2: iv_ruleExpressionList= ruleExpressionList EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getExpressionListRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleExpressionList=ruleExpressionList();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleExpressionList; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExpressionList"


    // $ANTLR start "ruleExpressionList"
    // InternalGaml.g:6613:1: ruleExpressionList returns [EObject current=null] : ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) ;
    public final EObject ruleExpressionList() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_exprs_0_0 = null;

        EObject lv_exprs_2_0 = null;

        EObject lv_exprs_3_0 = null;

        EObject lv_exprs_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6619:2: ( ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) )
            // InternalGaml.g:6620:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            {
            // InternalGaml.g:6620:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            int alt101=2;
            alt101 = dfa101.predict(input);
            switch (alt101) {
                case 1 :
                    // InternalGaml.g:6621:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    {
                    // InternalGaml.g:6621:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    // InternalGaml.g:6622:4: ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    {
                    // InternalGaml.g:6622:4: ( (lv_exprs_0_0= ruleExpression ) )
                    // InternalGaml.g:6623:5: (lv_exprs_0_0= ruleExpression )
                    {
                    // InternalGaml.g:6623:5: (lv_exprs_0_0= ruleExpression )
                    // InternalGaml.g:6624:6: lv_exprs_0_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_46);
                    lv_exprs_0_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getExpressionListRule());
                      						}
                      						add(
                      							current,
                      							"exprs",
                      							lv_exprs_0_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalGaml.g:6641:4: (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    loop99:
                    do {
                        int alt99=2;
                        int LA99_0 = input.LA(1);

                        if ( (LA99_0==108) ) {
                            alt99=1;
                        }


                        switch (alt99) {
                    	case 1 :
                    	    // InternalGaml.g:6642:5: otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) )
                    	    {
                    	    otherlv_1=(Token)match(input,108,FOLLOW_5); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_1, grammarAccess.getExpressionListAccess().getCommaKeyword_0_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:6646:5: ( (lv_exprs_2_0= ruleExpression ) )
                    	    // InternalGaml.g:6647:6: (lv_exprs_2_0= ruleExpression )
                    	    {
                    	    // InternalGaml.g:6647:6: (lv_exprs_2_0= ruleExpression )
                    	    // InternalGaml.g:6648:7: lv_exprs_2_0= ruleExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_46);
                    	    lv_exprs_2_0=ruleExpression();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getExpressionListRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"exprs",
                    	      								lv_exprs_2_0,
                    	      								"gaml.compiler.Gaml.Expression");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop99;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:6668:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    {
                    // InternalGaml.g:6668:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    // InternalGaml.g:6669:4: ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    {
                    // InternalGaml.g:6669:4: ( (lv_exprs_3_0= ruleParameter ) )
                    // InternalGaml.g:6670:5: (lv_exprs_3_0= ruleParameter )
                    {
                    // InternalGaml.g:6670:5: (lv_exprs_3_0= ruleParameter )
                    // InternalGaml.g:6671:6: lv_exprs_3_0= ruleParameter
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_46);
                    lv_exprs_3_0=ruleParameter();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getExpressionListRule());
                      						}
                      						add(
                      							current,
                      							"exprs",
                      							lv_exprs_3_0,
                      							"gaml.compiler.Gaml.Parameter");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalGaml.g:6688:4: (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    loop100:
                    do {
                        int alt100=2;
                        int LA100_0 = input.LA(1);

                        if ( (LA100_0==108) ) {
                            alt100=1;
                        }


                        switch (alt100) {
                    	case 1 :
                    	    // InternalGaml.g:6689:5: otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) )
                    	    {
                    	    otherlv_4=(Token)match(input,108,FOLLOW_21); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_4, grammarAccess.getExpressionListAccess().getCommaKeyword_1_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:6693:5: ( (lv_exprs_5_0= ruleParameter ) )
                    	    // InternalGaml.g:6694:6: (lv_exprs_5_0= ruleParameter )
                    	    {
                    	    // InternalGaml.g:6694:6: (lv_exprs_5_0= ruleParameter )
                    	    // InternalGaml.g:6695:7: lv_exprs_5_0= ruleParameter
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_46);
                    	    lv_exprs_5_0=ruleParameter();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getExpressionListRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"exprs",
                    	      								lv_exprs_5_0,
                    	      								"gaml.compiler.Gaml.Parameter");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop100;
                        }
                    } while (true);


                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExpressionList"


    // $ANTLR start "entryRuleParameter"
    // InternalGaml.g:6718:1: entryRuleParameter returns [EObject current=null] : iv_ruleParameter= ruleParameter EOF ;
    public final EObject entryRuleParameter() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameter = null;


        try {
            // InternalGaml.g:6718:50: (iv_ruleParameter= ruleParameter EOF )
            // InternalGaml.g:6719:2: iv_ruleParameter= ruleParameter EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getParameterRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleParameter=ruleParameter();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleParameter; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleParameter"


    // $ANTLR start "ruleParameter"
    // InternalGaml.g:6725:1: ruleParameter returns [EObject current=null] : ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) ;
    public final EObject ruleParameter() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        AntlrDatatypeRuleToken lv_builtInFacetKey_1_1 = null;

        AntlrDatatypeRuleToken lv_builtInFacetKey_1_2 = null;

        AntlrDatatypeRuleToken lv_builtInFacetKey_1_3 = null;

        AntlrDatatypeRuleToken lv_builtInFacetKey_1_4 = null;

        AntlrDatatypeRuleToken lv_builtInFacetKey_1_5 = null;

        EObject lv_left_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6731:2: ( ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) )
            // InternalGaml.g:6732:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            {
            // InternalGaml.g:6732:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            // InternalGaml.g:6733:3: () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) )
            {
            // InternalGaml.g:6733:3: ()
            // InternalGaml.g:6734:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getParameterAccess().getParameterAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6740:3: ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) )
            int alt103=2;
            int LA103_0 = input.LA(1);

            if ( (LA103_0==22||LA103_0==26||(LA103_0>=34 && LA103_0<=35)||(LA103_0>=109 && LA103_0<=132)) ) {
                alt103=1;
            }
            else if ( (LA103_0==RULE_ID||LA103_0==45||(LA103_0>=47 && LA103_0<=49)||(LA103_0>=53 && LA103_0<=101)) ) {
                alt103=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }
            switch (alt103) {
                case 1 :
                    // InternalGaml.g:6741:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    {
                    // InternalGaml.g:6741:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    // InternalGaml.g:6742:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    {
                    // InternalGaml.g:6742:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    // InternalGaml.g:6743:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    {
                    // InternalGaml.g:6743:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    int alt102=5;
                    switch ( input.LA(1) ) {
                    case 22:
                    case 109:
                        {
                        alt102=1;
                        }
                        break;
                    case 110:
                    case 111:
                    case 112:
                    case 113:
                    case 114:
                        {
                        alt102=2;
                        }
                        break;
                    case 34:
                    case 35:
                    case 115:
                    case 116:
                    case 117:
                    case 118:
                    case 119:
                    case 120:
                    case 121:
                    case 122:
                    case 123:
                    case 124:
                    case 125:
                    case 126:
                    case 127:
                    case 128:
                    case 129:
                    case 130:
                        {
                        alt102=3;
                        }
                        break;
                    case 26:
                    case 131:
                        {
                        alt102=4;
                        }
                        break;
                    case 132:
                        {
                        alt102=5;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 102, 0, input);

                        throw nvae;
                    }

                    switch (alt102) {
                        case 1 :
                            // InternalGaml.g:6744:7: lv_builtInFacetKey_1_1= ruleDefinitionFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyDefinitionFacetKeyParserRuleCall_1_0_0_0());
                              						
                            }
                            pushFollow(FOLLOW_5);
                            lv_builtInFacetKey_1_1=ruleDefinitionFacetKey();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getParameterRule());
                              							}
                              							set(
                              								current,
                              								"builtInFacetKey",
                              								lv_builtInFacetKey_1_1,
                              								"gaml.compiler.Gaml.DefinitionFacetKey");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 2 :
                            // InternalGaml.g:6760:7: lv_builtInFacetKey_1_2= ruleTypeFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyTypeFacetKeyParserRuleCall_1_0_0_1());
                              						
                            }
                            pushFollow(FOLLOW_5);
                            lv_builtInFacetKey_1_2=ruleTypeFacetKey();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getParameterRule());
                              							}
                              							set(
                              								current,
                              								"builtInFacetKey",
                              								lv_builtInFacetKey_1_2,
                              								"gaml.compiler.Gaml.TypeFacetKey");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 3 :
                            // InternalGaml.g:6776:7: lv_builtInFacetKey_1_3= ruleSpecialFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeySpecialFacetKeyParserRuleCall_1_0_0_2());
                              						
                            }
                            pushFollow(FOLLOW_5);
                            lv_builtInFacetKey_1_3=ruleSpecialFacetKey();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getParameterRule());
                              							}
                              							set(
                              								current,
                              								"builtInFacetKey",
                              								lv_builtInFacetKey_1_3,
                              								"gaml.compiler.Gaml.SpecialFacetKey");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 4 :
                            // InternalGaml.g:6792:7: lv_builtInFacetKey_1_4= ruleActionFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyActionFacetKeyParserRuleCall_1_0_0_3());
                              						
                            }
                            pushFollow(FOLLOW_5);
                            lv_builtInFacetKey_1_4=ruleActionFacetKey();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getParameterRule());
                              							}
                              							set(
                              								current,
                              								"builtInFacetKey",
                              								lv_builtInFacetKey_1_4,
                              								"gaml.compiler.Gaml.ActionFacetKey");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 5 :
                            // InternalGaml.g:6808:7: lv_builtInFacetKey_1_5= ruleVarFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyVarFacetKeyParserRuleCall_1_0_0_4());
                              						
                            }
                            pushFollow(FOLLOW_5);
                            lv_builtInFacetKey_1_5=ruleVarFacetKey();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getParameterRule());
                              							}
                              							set(
                              								current,
                              								"builtInFacetKey",
                              								lv_builtInFacetKey_1_5,
                              								"gaml.compiler.Gaml.VarFacetKey");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:6827:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    {
                    // InternalGaml.g:6827:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    // InternalGaml.g:6828:5: ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':'
                    {
                    // InternalGaml.g:6828:5: ( (lv_left_2_0= ruleVariableRef ) )
                    // InternalGaml.g:6829:6: (lv_left_2_0= ruleVariableRef )
                    {
                    // InternalGaml.g:6829:6: (lv_left_2_0= ruleVariableRef )
                    // InternalGaml.g:6830:7: lv_left_2_0= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getParameterAccess().getLeftVariableRefParserRuleCall_1_1_0_0());
                      						
                    }
                    pushFollow(FOLLOW_33);
                    lv_left_2_0=ruleVariableRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getParameterRule());
                      							}
                      							set(
                      								current,
                      								"left",
                      								lv_left_2_0,
                      								"gaml.compiler.Gaml.VariableRef");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }

                    otherlv_3=(Token)match(input,36,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getParameterAccess().getColonKeyword_1_1_1());
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:6853:3: ( (lv_right_4_0= ruleExpression ) )
            // InternalGaml.g:6854:4: (lv_right_4_0= ruleExpression )
            {
            // InternalGaml.g:6854:4: (lv_right_4_0= ruleExpression )
            // InternalGaml.g:6855:5: lv_right_4_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getParameterAccess().getRightExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_right_4_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getParameterRule());
              					}
              					set(
              						current,
              						"right",
              						lv_right_4_0,
              						"gaml.compiler.Gaml.Expression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleParameter"


    // $ANTLR start "entryRuleUnitRef"
    // InternalGaml.g:6876:1: entryRuleUnitRef returns [EObject current=null] : iv_ruleUnitRef= ruleUnitRef EOF ;
    public final EObject entryRuleUnitRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitRef = null;


        try {
            // InternalGaml.g:6876:48: (iv_ruleUnitRef= ruleUnitRef EOF )
            // InternalGaml.g:6877:2: iv_ruleUnitRef= ruleUnitRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getUnitRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleUnitRef=ruleUnitRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleUnitRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUnitRef"


    // $ANTLR start "ruleUnitRef"
    // InternalGaml.g:6883:1: ruleUnitRef returns [EObject current=null] : ( () ( (otherlv_1= RULE_ID ) ) ) ;
    public final EObject ruleUnitRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;


        	enterRule();

        try {
            // InternalGaml.g:6889:2: ( ( () ( (otherlv_1= RULE_ID ) ) ) )
            // InternalGaml.g:6890:2: ( () ( (otherlv_1= RULE_ID ) ) )
            {
            // InternalGaml.g:6890:2: ( () ( (otherlv_1= RULE_ID ) ) )
            // InternalGaml.g:6891:3: () ( (otherlv_1= RULE_ID ) )
            {
            // InternalGaml.g:6891:3: ()
            // InternalGaml.g:6892:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getUnitRefAccess().getUnitNameAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6898:3: ( (otherlv_1= RULE_ID ) )
            // InternalGaml.g:6899:4: (otherlv_1= RULE_ID )
            {
            // InternalGaml.g:6899:4: (otherlv_1= RULE_ID )
            // InternalGaml.g:6900:5: otherlv_1= RULE_ID
            {
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getUnitRefRule());
              					}
              				
            }
            otherlv_1=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(otherlv_1, grammarAccess.getUnitRefAccess().getRefUnitFakeDefinitionCrossReference_1_0());
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUnitRef"


    // $ANTLR start "entryRuleVariableRef"
    // InternalGaml.g:6915:1: entryRuleVariableRef returns [EObject current=null] : iv_ruleVariableRef= ruleVariableRef EOF ;
    public final EObject entryRuleVariableRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVariableRef = null;


        try {
            // InternalGaml.g:6915:52: (iv_ruleVariableRef= ruleVariableRef EOF )
            // InternalGaml.g:6916:2: iv_ruleVariableRef= ruleVariableRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getVariableRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleVariableRef=ruleVariableRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleVariableRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVariableRef"


    // $ANTLR start "ruleVariableRef"
    // InternalGaml.g:6922:1: ruleVariableRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleVariableRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:6928:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:6929:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:6929:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:6930:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:6930:3: ()
            // InternalGaml.g:6931:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getVariableRefAccess().getVariableRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6937:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:6938:4: ( ruleValid_ID )
            {
            // InternalGaml.g:6938:4: ( ruleValid_ID )
            // InternalGaml.g:6939:5: ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getVariableRefRule());
              					}
              				
            }
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getVariableRefAccess().getRefVarDefinitionCrossReference_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVariableRef"


    // $ANTLR start "entryRuleTypeRef"
    // InternalGaml.g:6957:1: entryRuleTypeRef returns [EObject current=null] : iv_ruleTypeRef= ruleTypeRef EOF ;
    public final EObject entryRuleTypeRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeRef = null;


        try {
            // InternalGaml.g:6957:48: (iv_ruleTypeRef= ruleTypeRef EOF )
            // InternalGaml.g:6958:2: iv_ruleTypeRef= ruleTypeRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeRef=ruleTypeRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeRef"


    // $ANTLR start "ruleTypeRef"
    // InternalGaml.g:6964:1: ruleTypeRef returns [EObject current=null] : ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) | ( () otherlv_7= 'image' ) ) ;
    public final EObject ruleTypeRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_4=null;
        Token otherlv_7=null;
        EObject lv_parameter_2_0 = null;

        EObject lv_parameter_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6970:2: ( ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) | ( () otherlv_7= 'image' ) ) )
            // InternalGaml.g:6971:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) | ( () otherlv_7= 'image' ) )
            {
            // InternalGaml.g:6971:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) | ( () otherlv_7= 'image' ) )
            int alt105=3;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt105=1;
                }
                break;
            case 47:
                {
                alt105=2;
                }
                break;
            case 49:
                {
                alt105=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }

            switch (alt105) {
                case 1 :
                    // InternalGaml.g:6972:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    {
                    // InternalGaml.g:6972:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    // InternalGaml.g:6973:4: () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    {
                    // InternalGaml.g:6973:4: ()
                    // InternalGaml.g:6974:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6980:4: ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    // InternalGaml.g:6981:5: ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    {
                    // InternalGaml.g:6981:5: ( (otherlv_1= RULE_ID ) )
                    // InternalGaml.g:6982:6: (otherlv_1= RULE_ID )
                    {
                    // InternalGaml.g:6982:6: (otherlv_1= RULE_ID )
                    // InternalGaml.g:6983:7: otherlv_1= RULE_ID
                    {
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElement(grammarAccess.getTypeRefRule());
                      							}
                      						
                    }
                    otherlv_1=(Token)match(input,RULE_ID,FOLLOW_68); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							newLeafNode(otherlv_1, grammarAccess.getTypeRefAccess().getRefTypeDefinitionCrossReference_0_1_0_0());
                      						
                    }

                    }


                    }

                    // InternalGaml.g:6994:5: ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    int alt104=2;
                    int LA104_0 = input.LA(1);

                    if ( (LA104_0==141) ) {
                        alt104=1;
                    }
                    switch (alt104) {
                        case 1 :
                            // InternalGaml.g:6995:6: (lv_parameter_2_0= ruleTypeInfo )
                            {
                            // InternalGaml.g:6995:6: (lv_parameter_2_0= ruleTypeInfo )
                            // InternalGaml.g:6996:7: lv_parameter_2_0= ruleTypeInfo
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getTypeRefAccess().getParameterTypeInfoParserRuleCall_0_1_1_0());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_parameter_2_0=ruleTypeInfo();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getTypeRefRule());
                              							}
                              							set(
                              								current,
                              								"parameter",
                              								lv_parameter_2_0,
                              								"gaml.compiler.Gaml.TypeInfo");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }
                            break;

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:7016:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    {
                    // InternalGaml.g:7016:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    // InternalGaml.g:7017:4: () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    {
                    // InternalGaml.g:7017:4: ()
                    // InternalGaml.g:7018:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7024:4: (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    // InternalGaml.g:7025:5: otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) )
                    {
                    otherlv_4=(Token)match(input,47,FOLLOW_69); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getTypeRefAccess().getSpeciesKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:7029:5: ( (lv_parameter_5_0= ruleTypeInfo ) )
                    // InternalGaml.g:7030:6: (lv_parameter_5_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:7030:6: (lv_parameter_5_0= ruleTypeInfo )
                    // InternalGaml.g:7031:7: lv_parameter_5_0= ruleTypeInfo
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getTypeRefAccess().getParameterTypeInfoParserRuleCall_1_1_1_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_parameter_5_0=ruleTypeInfo();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getTypeRefRule());
                      							}
                      							set(
                      								current,
                      								"parameter",
                      								lv_parameter_5_0,
                      								"gaml.compiler.Gaml.TypeInfo");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // InternalGaml.g:7051:3: ( () otherlv_7= 'image' )
                    {
                    // InternalGaml.g:7051:3: ( () otherlv_7= 'image' )
                    // InternalGaml.g:7052:4: () otherlv_7= 'image'
                    {
                    // InternalGaml.g:7052:4: ()
                    // InternalGaml.g:7053:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getTypeRefAccess().getImageKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeRef"


    // $ANTLR start "entryRuleTypeInfo"
    // InternalGaml.g:7068:1: entryRuleTypeInfo returns [EObject current=null] : iv_ruleTypeInfo= ruleTypeInfo EOF ;
    public final EObject entryRuleTypeInfo() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeInfo = null;


        try {
            // InternalGaml.g:7068:49: (iv_ruleTypeInfo= ruleTypeInfo EOF )
            // InternalGaml.g:7069:2: iv_ruleTypeInfo= ruleTypeInfo EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeInfoRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeInfo=ruleTypeInfo();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeInfo; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeInfo"


    // $ANTLR start "ruleTypeInfo"
    // InternalGaml.g:7075:1: ruleTypeInfo returns [EObject current=null] : (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) ;
    public final EObject ruleTypeInfo() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_first_1_0 = null;

        EObject lv_second_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7081:2: ( (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) )
            // InternalGaml.g:7082:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            {
            // InternalGaml.g:7082:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            // InternalGaml.g:7083:3: otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' )
            {
            otherlv_0=(Token)match(input,141,FOLLOW_35); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeInfoAccess().getLessThanSignKeyword_0());
              		
            }
            // InternalGaml.g:7087:3: ( (lv_first_1_0= ruleTypeRef ) )
            // InternalGaml.g:7088:4: (lv_first_1_0= ruleTypeRef )
            {
            // InternalGaml.g:7088:4: (lv_first_1_0= ruleTypeRef )
            // InternalGaml.g:7089:5: lv_first_1_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getTypeInfoAccess().getFirstTypeRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_70);
            lv_first_1_0=ruleTypeRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getTypeInfoRule());
              					}
              					set(
              						current,
              						"first",
              						lv_first_1_0,
              						"gaml.compiler.Gaml.TypeRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:7106:3: (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )?
            int alt106=2;
            int LA106_0 = input.LA(1);

            if ( (LA106_0==108) ) {
                alt106=1;
            }
            switch (alt106) {
                case 1 :
                    // InternalGaml.g:7107:4: otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) )
                    {
                    otherlv_2=(Token)match(input,108,FOLLOW_35); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getTypeInfoAccess().getCommaKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:7111:4: ( (lv_second_3_0= ruleTypeRef ) )
                    // InternalGaml.g:7112:5: (lv_second_3_0= ruleTypeRef )
                    {
                    // InternalGaml.g:7112:5: (lv_second_3_0= ruleTypeRef )
                    // InternalGaml.g:7113:6: lv_second_3_0= ruleTypeRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getTypeInfoAccess().getSecondTypeRefParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_44);
                    lv_second_3_0=ruleTypeRef();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getTypeInfoRule());
                      						}
                      						set(
                      							current,
                      							"second",
                      							lv_second_3_0,
                      							"gaml.compiler.Gaml.TypeRef");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalGaml.g:7131:3: ( ( '>' )=>otherlv_4= '>' )
            // InternalGaml.g:7132:4: ( '>' )=>otherlv_4= '>'
            {
            otherlv_4=(Token)match(input,103,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              				newLeafNode(otherlv_4, grammarAccess.getTypeInfoAccess().getGreaterThanSignKeyword_3());
              			
            }

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeInfo"


    // $ANTLR start "entryRuleActionRef"
    // InternalGaml.g:7142:1: entryRuleActionRef returns [EObject current=null] : iv_ruleActionRef= ruleActionRef EOF ;
    public final EObject entryRuleActionRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionRef = null;


        try {
            // InternalGaml.g:7142:50: (iv_ruleActionRef= ruleActionRef EOF )
            // InternalGaml.g:7143:2: iv_ruleActionRef= ruleActionRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionRef=ruleActionRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionRef"


    // $ANTLR start "ruleActionRef"
    // InternalGaml.g:7149:1: ruleActionRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleActionRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:7155:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:7156:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:7156:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:7157:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:7157:3: ()
            // InternalGaml.g:7158:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getActionRefAccess().getActionRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:7164:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:7165:4: ( ruleValid_ID )
            {
            // InternalGaml.g:7165:4: ( ruleValid_ID )
            // InternalGaml.g:7166:5: ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getActionRefRule());
              					}
              				
            }
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionRefAccess().getRefActionDefinitionCrossReference_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionRef"


    // $ANTLR start "entryRuleEquationRef"
    // InternalGaml.g:7184:1: entryRuleEquationRef returns [EObject current=null] : iv_ruleEquationRef= ruleEquationRef EOF ;
    public final EObject entryRuleEquationRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationRef = null;


        try {
            // InternalGaml.g:7184:52: (iv_ruleEquationRef= ruleEquationRef EOF )
            // InternalGaml.g:7185:2: iv_ruleEquationRef= ruleEquationRef EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getEquationRefRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleEquationRef=ruleEquationRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleEquationRef; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleEquationRef"


    // $ANTLR start "ruleEquationRef"
    // InternalGaml.g:7191:1: ruleEquationRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleEquationRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:7197:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:7198:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:7198:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:7199:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:7199:3: ()
            // InternalGaml.g:7200:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getEquationRefAccess().getEquationRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:7206:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:7207:4: ( ruleValid_ID )
            {
            // InternalGaml.g:7207:4: ( ruleValid_ID )
            // InternalGaml.g:7208:5: ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getEquationRefRule());
              					}
              				
            }
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getEquationRefAccess().getRefEquationDefinitionCrossReference_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleEquationRef"


    // $ANTLR start "entryRuleEquationDefinition"
    // InternalGaml.g:7226:1: entryRuleEquationDefinition returns [EObject current=null] : iv_ruleEquationDefinition= ruleEquationDefinition EOF ;
    public final EObject entryRuleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationDefinition = null;


        try {
            // InternalGaml.g:7226:59: (iv_ruleEquationDefinition= ruleEquationDefinition EOF )
            // InternalGaml.g:7227:2: iv_ruleEquationDefinition= ruleEquationDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getEquationDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleEquationDefinition=ruleEquationDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleEquationDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleEquationDefinition"


    // $ANTLR start "ruleEquationDefinition"
    // InternalGaml.g:7233:1: ruleEquationDefinition returns [EObject current=null] : (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) ;
    public final EObject ruleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Equations_0 = null;

        EObject this_EquationFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:7239:2: ( (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) )
            // InternalGaml.g:7240:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            {
            // InternalGaml.g:7240:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            int alt107=2;
            int LA107_0 = input.LA(1);

            if ( (LA107_0==45) ) {
                alt107=1;
            }
            else if ( (LA107_0==156) ) {
                alt107=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }
            switch (alt107) {
                case 1 :
                    // InternalGaml.g:7241:3: this_S_Equations_0= ruleS_Equations
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEquationDefinitionAccess().getS_EquationsParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Equations_0=ruleS_Equations();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Equations_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:7250:3: this_EquationFakeDefinition_1= ruleEquationFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEquationDefinitionAccess().getEquationFakeDefinitionParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_EquationFakeDefinition_1=ruleEquationFakeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_EquationFakeDefinition_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleEquationDefinition"


    // $ANTLR start "entryRuleTypeDefinition"
    // InternalGaml.g:7262:1: entryRuleTypeDefinition returns [EObject current=null] : iv_ruleTypeDefinition= ruleTypeDefinition EOF ;
    public final EObject entryRuleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDefinition = null;


        try {
            // InternalGaml.g:7262:55: (iv_ruleTypeDefinition= ruleTypeDefinition EOF )
            // InternalGaml.g:7263:2: iv_ruleTypeDefinition= ruleTypeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeDefinition=ruleTypeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeDefinition"


    // $ANTLR start "ruleTypeDefinition"
    // InternalGaml.g:7269:1: ruleTypeDefinition returns [EObject current=null] : (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) ;
    public final EObject ruleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Species_0 = null;

        EObject this_TypeFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:7275:2: ( (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) )
            // InternalGaml.g:7276:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            {
            // InternalGaml.g:7276:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            int alt108=2;
            int LA108_0 = input.LA(1);

            if ( ((LA108_0>=47 && LA108_0<=48)) ) {
                alt108=1;
            }
            else if ( (LA108_0==152) ) {
                alt108=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 108, 0, input);

                throw nvae;
            }
            switch (alt108) {
                case 1 :
                    // InternalGaml.g:7277:3: this_S_Species_0= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getTypeDefinitionAccess().getS_SpeciesParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Species_0=ruleS_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Species_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:7286:3: this_TypeFakeDefinition_1= ruleTypeFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getTypeDefinitionAccess().getTypeFakeDefinitionParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_TypeFakeDefinition_1=ruleTypeFakeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_TypeFakeDefinition_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeDefinition"


    // $ANTLR start "entryRuleVarDefinition"
    // InternalGaml.g:7298:1: entryRuleVarDefinition returns [EObject current=null] : iv_ruleVarDefinition= ruleVarDefinition EOF ;
    public final EObject entryRuleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDefinition = null;


        try {
            // InternalGaml.g:7298:54: (iv_ruleVarDefinition= ruleVarDefinition EOF )
            // InternalGaml.g:7299:2: iv_ruleVarDefinition= ruleVarDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getVarDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleVarDefinition=ruleVarDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleVarDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVarDefinition"


    // $ANTLR start "ruleVarDefinition"
    // InternalGaml.g:7305:1: ruleVarDefinition returns [EObject current=null] : ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment ) ;
    public final EObject ruleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Declaration_0 = null;

        EObject this_Model_1 = null;

        EObject this_ArgumentDefinition_2 = null;

        EObject this_DefinitionFacet_3 = null;

        EObject this_VarFakeDefinition_4 = null;

        EObject this_Import_5 = null;

        EObject this_S_Experiment_6 = null;



        	enterRule();

        try {
            // InternalGaml.g:7311:2: ( ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment ) )
            // InternalGaml.g:7312:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )
            {
            // InternalGaml.g:7312:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )
            int alt109=7;
            alt109 = dfa109.predict(input);
            switch (alt109) {
                case 1 :
                    // InternalGaml.g:7313:3: ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration )
                    {
                    // InternalGaml.g:7313:3: ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration )
                    // InternalGaml.g:7314:4: ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_DeclarationParserRuleCall_0());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Declaration_0=ruleS_Declaration();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Declaration_0;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:7325:3: this_Model_1= ruleModel
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getModelParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_Model_1=ruleModel();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_Model_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:7334:3: this_ArgumentDefinition_2= ruleArgumentDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getArgumentDefinitionParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ArgumentDefinition_2=ruleArgumentDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ArgumentDefinition_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:7343:3: this_DefinitionFacet_3= ruleDefinitionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getDefinitionFacetParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_DefinitionFacet_3=ruleDefinitionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_DefinitionFacet_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:7352:3: this_VarFakeDefinition_4= ruleVarFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getVarFakeDefinitionParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_VarFakeDefinition_4=ruleVarFakeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_VarFakeDefinition_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:7361:3: this_Import_5= ruleImport
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getImportParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_Import_5=ruleImport();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_Import_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:7370:3: this_S_Experiment_6= ruleS_Experiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ExperimentParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Experiment_6=ruleS_Experiment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Experiment_6;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVarDefinition"


    // $ANTLR start "entryRuleActionDefinition"
    // InternalGaml.g:7382:1: entryRuleActionDefinition returns [EObject current=null] : iv_ruleActionDefinition= ruleActionDefinition EOF ;
    public final EObject entryRuleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionDefinition = null;


        try {
            // InternalGaml.g:7382:57: (iv_ruleActionDefinition= ruleActionDefinition EOF )
            // InternalGaml.g:7383:2: iv_ruleActionDefinition= ruleActionDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionDefinition=ruleActionDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionDefinition"


    // $ANTLR start "ruleActionDefinition"
    // InternalGaml.g:7389:1: ruleActionDefinition returns [EObject current=null] : (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) ;
    public final EObject ruleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Action_0 = null;

        EObject this_ActionFakeDefinition_1 = null;

        EObject this_S_Definition_2 = null;

        EObject this_TypeDefinition_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:7395:2: ( (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) )
            // InternalGaml.g:7396:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            {
            // InternalGaml.g:7396:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            int alt110=4;
            switch ( input.LA(1) ) {
            case 39:
                {
                alt110=1;
                }
                break;
            case 153:
                {
                alt110=2;
                }
                break;
            case RULE_ID:
            case 49:
                {
                alt110=3;
                }
                break;
            case 47:
                {
                int LA110_4 = input.LA(2);

                if ( (LA110_4==141) ) {
                    alt110=3;
                }
                else if ( (LA110_4==RULE_ID||LA110_4==22) ) {
                    alt110=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 110, 4, input);

                    throw nvae;
                }
                }
                break;
            case 48:
            case 152:
                {
                alt110=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 110, 0, input);

                throw nvae;
            }

            switch (alt110) {
                case 1 :
                    // InternalGaml.g:7397:3: this_S_Action_0= ruleS_Action
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getActionDefinitionAccess().getS_ActionParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Action_0=ruleS_Action();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Action_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:7406:3: this_ActionFakeDefinition_1= ruleActionFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getActionDefinitionAccess().getActionFakeDefinitionParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ActionFakeDefinition_1=ruleActionFakeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ActionFakeDefinition_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:7415:3: this_S_Definition_2= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getActionDefinitionAccess().getS_DefinitionParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_2=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Definition_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:7424:3: this_TypeDefinition_3= ruleTypeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getActionDefinitionAccess().getTypeDefinitionParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_TypeDefinition_3=ruleTypeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_TypeDefinition_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionDefinition"


    // $ANTLR start "entryRuleUnitFakeDefinition"
    // InternalGaml.g:7436:1: entryRuleUnitFakeDefinition returns [EObject current=null] : iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF ;
    public final EObject entryRuleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitFakeDefinition = null;


        try {
            // InternalGaml.g:7436:59: (iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF )
            // InternalGaml.g:7437:2: iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getUnitFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleUnitFakeDefinition=ruleUnitFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleUnitFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUnitFakeDefinition"


    // $ANTLR start "ruleUnitFakeDefinition"
    // InternalGaml.g:7443:1: ruleUnitFakeDefinition returns [EObject current=null] : (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7449:2: ( (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:7450:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:7450:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:7451:3: otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,151,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getUnitFakeDefinitionAccess().getUnitKeyword_0());
              		
            }
            // InternalGaml.g:7455:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:7456:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:7456:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:7457:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_name_1_0, grammarAccess.getUnitFakeDefinitionAccess().getNameIDTerminalRuleCall_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getUnitFakeDefinitionRule());
              					}
              					setWithLastConsumed(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUnitFakeDefinition"


    // $ANTLR start "entryRuleTypeFakeDefinition"
    // InternalGaml.g:7477:1: entryRuleTypeFakeDefinition returns [EObject current=null] : iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF ;
    public final EObject entryRuleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFakeDefinition = null;


        try {
            // InternalGaml.g:7477:59: (iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF )
            // InternalGaml.g:7478:2: iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTypeFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTypeFakeDefinition=ruleTypeFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTypeFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeFakeDefinition"


    // $ANTLR start "ruleTypeFakeDefinition"
    // InternalGaml.g:7484:1: ruleTypeFakeDefinition returns [EObject current=null] : (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7490:2: ( (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:7491:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:7491:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:7492:3: otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,152,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeFakeDefinitionAccess().getTypeKeyword_0());
              		
            }
            // InternalGaml.g:7496:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:7497:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:7497:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:7498:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_name_1_0, grammarAccess.getTypeFakeDefinitionAccess().getNameIDTerminalRuleCall_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getTypeFakeDefinitionRule());
              					}
              					setWithLastConsumed(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeFakeDefinition"


    // $ANTLR start "entryRuleActionFakeDefinition"
    // InternalGaml.g:7518:1: entryRuleActionFakeDefinition returns [EObject current=null] : iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF ;
    public final EObject entryRuleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFakeDefinition = null;


        try {
            // InternalGaml.g:7518:61: (iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF )
            // InternalGaml.g:7519:2: iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getActionFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleActionFakeDefinition=ruleActionFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleActionFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleActionFakeDefinition"


    // $ANTLR start "ruleActionFakeDefinition"
    // InternalGaml.g:7525:1: ruleActionFakeDefinition returns [EObject current=null] : (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7531:2: ( (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7532:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7532:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7533:3: otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,153,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getActionFakeDefinitionAccess().getActionKeyword_0());
              		
            }
            // InternalGaml.g:7537:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7538:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7538:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7539:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionFakeDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getActionFakeDefinitionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleActionFakeDefinition"


    // $ANTLR start "entryRuleSkillFakeDefinition"
    // InternalGaml.g:7560:1: entryRuleSkillFakeDefinition returns [EObject current=null] : iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF ;
    public final EObject entryRuleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSkillFakeDefinition = null;


        try {
            // InternalGaml.g:7560:60: (iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF )
            // InternalGaml.g:7561:2: iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSkillFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSkillFakeDefinition=ruleSkillFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSkillFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSkillFakeDefinition"


    // $ANTLR start "ruleSkillFakeDefinition"
    // InternalGaml.g:7567:1: ruleSkillFakeDefinition returns [EObject current=null] : (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7573:2: ( (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:7574:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:7574:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:7575:3: otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,154,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSkillFakeDefinitionAccess().getSkillKeyword_0());
              		
            }
            // InternalGaml.g:7579:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:7580:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:7580:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:7581:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_name_1_0, grammarAccess.getSkillFakeDefinitionAccess().getNameIDTerminalRuleCall_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getSkillFakeDefinitionRule());
              					}
              					setWithLastConsumed(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSkillFakeDefinition"


    // $ANTLR start "entryRuleVarFakeDefinition"
    // InternalGaml.g:7601:1: entryRuleVarFakeDefinition returns [EObject current=null] : iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF ;
    public final EObject entryRuleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFakeDefinition = null;


        try {
            // InternalGaml.g:7601:58: (iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF )
            // InternalGaml.g:7602:2: iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getVarFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleVarFakeDefinition=ruleVarFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleVarFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVarFakeDefinition"


    // $ANTLR start "ruleVarFakeDefinition"
    // InternalGaml.g:7608:1: ruleVarFakeDefinition returns [EObject current=null] : (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7614:2: ( (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7615:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7615:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7616:3: otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,155,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getVarFakeDefinitionAccess().getVarKeyword_0());
              		
            }
            // InternalGaml.g:7620:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7621:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7621:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7622:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getVarFakeDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getVarFakeDefinitionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVarFakeDefinition"


    // $ANTLR start "entryRuleEquationFakeDefinition"
    // InternalGaml.g:7643:1: entryRuleEquationFakeDefinition returns [EObject current=null] : iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF ;
    public final EObject entryRuleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationFakeDefinition = null;


        try {
            // InternalGaml.g:7643:63: (iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF )
            // InternalGaml.g:7644:2: iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getEquationFakeDefinitionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleEquationFakeDefinition=ruleEquationFakeDefinition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleEquationFakeDefinition; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleEquationFakeDefinition"


    // $ANTLR start "ruleEquationFakeDefinition"
    // InternalGaml.g:7650:1: ruleEquationFakeDefinition returns [EObject current=null] : (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7656:2: ( (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7657:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7657:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7658:3: otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,156,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getEquationFakeDefinitionAccess().getEquationKeyword_0());
              		
            }
            // InternalGaml.g:7662:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7663:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7663:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7664:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getEquationFakeDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getEquationFakeDefinitionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.Valid_ID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleEquationFakeDefinition"


    // $ANTLR start "entryRuleValid_ID"
    // InternalGaml.g:7685:1: entryRuleValid_ID returns [String current=null] : iv_ruleValid_ID= ruleValid_ID EOF ;
    public final String entryRuleValid_ID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValid_ID = null;


        try {
            // InternalGaml.g:7685:48: (iv_ruleValid_ID= ruleValid_ID EOF )
            // InternalGaml.g:7686:2: iv_ruleValid_ID= ruleValid_ID EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getValid_IDRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleValid_ID=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleValid_ID.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleValid_ID"


    // $ANTLR start "ruleValid_ID"
    // InternalGaml.g:7692:1: ruleValid_ID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this__SpeciesKey_0= rule_SpeciesKey | this__ImageLayerKey_1= rule_ImageLayerKey | this__DoKey_2= rule_DoKey | this__ReflexKey_3= rule_ReflexKey | this__VarOrConstKey_4= rule_VarOrConstKey | this__GeneralKey_5= rule_GeneralKey | this__EquationsKey_6= rule_EquationsKey | this__ExperimentKey_7= rule_ExperimentKey | this_ID_8= RULE_ID ) ;
    public final AntlrDatatypeRuleToken ruleValid_ID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_8=null;
        AntlrDatatypeRuleToken this__SpeciesKey_0 = null;

        AntlrDatatypeRuleToken this__ImageLayerKey_1 = null;

        AntlrDatatypeRuleToken this__DoKey_2 = null;

        AntlrDatatypeRuleToken this__ReflexKey_3 = null;

        AntlrDatatypeRuleToken this__VarOrConstKey_4 = null;

        AntlrDatatypeRuleToken this__GeneralKey_5 = null;

        AntlrDatatypeRuleToken this__EquationsKey_6 = null;

        AntlrDatatypeRuleToken this__ExperimentKey_7 = null;



        	enterRule();

        try {
            // InternalGaml.g:7698:2: ( (this__SpeciesKey_0= rule_SpeciesKey | this__ImageLayerKey_1= rule_ImageLayerKey | this__DoKey_2= rule_DoKey | this__ReflexKey_3= rule_ReflexKey | this__VarOrConstKey_4= rule_VarOrConstKey | this__GeneralKey_5= rule_GeneralKey | this__EquationsKey_6= rule_EquationsKey | this__ExperimentKey_7= rule_ExperimentKey | this_ID_8= RULE_ID ) )
            // InternalGaml.g:7699:2: (this__SpeciesKey_0= rule_SpeciesKey | this__ImageLayerKey_1= rule_ImageLayerKey | this__DoKey_2= rule_DoKey | this__ReflexKey_3= rule_ReflexKey | this__VarOrConstKey_4= rule_VarOrConstKey | this__GeneralKey_5= rule_GeneralKey | this__EquationsKey_6= rule_EquationsKey | this__ExperimentKey_7= rule_ExperimentKey | this_ID_8= RULE_ID )
            {
            // InternalGaml.g:7699:2: (this__SpeciesKey_0= rule_SpeciesKey | this__ImageLayerKey_1= rule_ImageLayerKey | this__DoKey_2= rule_DoKey | this__ReflexKey_3= rule_ReflexKey | this__VarOrConstKey_4= rule_VarOrConstKey | this__GeneralKey_5= rule_GeneralKey | this__EquationsKey_6= rule_EquationsKey | this__ExperimentKey_7= rule_ExperimentKey | this_ID_8= RULE_ID )
            int alt111=9;
            switch ( input.LA(1) ) {
            case 47:
            case 48:
                {
                alt111=1;
                }
                break;
            case 49:
                {
                alt111=2;
                }
                break;
            case 93:
            case 94:
                {
                alt111=3;
                }
                break;
            case 99:
            case 100:
            case 101:
                {
                alt111=4;
                }
                break;
            case 95:
            case 96:
            case 97:
            case 98:
                {
                alt111=5;
                }
                break;
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
                {
                alt111=6;
                }
                break;
            case 45:
                {
                alt111=7;
                }
                break;
            case 53:
                {
                alt111=8;
                }
                break;
            case RULE_ID:
                {
                alt111=9;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 111, 0, input);

                throw nvae;
            }

            switch (alt111) {
                case 1 :
                    // InternalGaml.g:7700:3: this__SpeciesKey_0= rule_SpeciesKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_SpeciesKeyParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__SpeciesKey_0=rule_SpeciesKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__SpeciesKey_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:7711:3: this__ImageLayerKey_1= rule_ImageLayerKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_ImageLayerKeyParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__ImageLayerKey_1=rule_ImageLayerKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__ImageLayerKey_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:7722:3: this__DoKey_2= rule_DoKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_DoKeyParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__DoKey_2=rule_DoKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__DoKey_2);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:7733:3: this__ReflexKey_3= rule_ReflexKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_ReflexKeyParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__ReflexKey_3=rule_ReflexKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__ReflexKey_3);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:7744:3: this__VarOrConstKey_4= rule_VarOrConstKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_VarOrConstKeyParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__VarOrConstKey_4=rule_VarOrConstKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__VarOrConstKey_4);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:7755:3: this__GeneralKey_5= rule_GeneralKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_GeneralKeyParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__GeneralKey_5=rule_GeneralKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__GeneralKey_5);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:7766:3: this__EquationsKey_6= rule_EquationsKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_EquationsKeyParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__EquationsKey_6=rule_EquationsKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__EquationsKey_6);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:7777:3: this__ExperimentKey_7= rule_ExperimentKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_ExperimentKeyParserRuleCall_7());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__ExperimentKey_7=rule_ExperimentKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__ExperimentKey_7);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:7788:3: this_ID_8= RULE_ID
                    {
                    this_ID_8=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ID_8);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_ID_8, grammarAccess.getValid_IDAccess().getIDTerminalRuleCall_8());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleValid_ID"


    // $ANTLR start "entryRuleTerminalExpression"
    // InternalGaml.g:7799:1: entryRuleTerminalExpression returns [EObject current=null] : iv_ruleTerminalExpression= ruleTerminalExpression EOF ;
    public final EObject entryRuleTerminalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTerminalExpression = null;


        try {
            // InternalGaml.g:7799:59: (iv_ruleTerminalExpression= ruleTerminalExpression EOF )
            // InternalGaml.g:7800:2: iv_ruleTerminalExpression= ruleTerminalExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getTerminalExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleTerminalExpression=ruleTerminalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleTerminalExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTerminalExpression"


    // $ANTLR start "ruleTerminalExpression"
    // InternalGaml.g:7806:1: ruleTerminalExpression returns [EObject current=null] : (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) ;
    public final EObject ruleTerminalExpression() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token lv_op_4_0=null;
        Token lv_op_6_0=null;
        Token lv_op_8_0=null;
        EObject this_StringLiteral_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7812:2: ( (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) )
            // InternalGaml.g:7813:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            {
            // InternalGaml.g:7813:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            int alt112=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt112=1;
                }
                break;
            case RULE_INTEGER:
                {
                alt112=2;
                }
                break;
            case RULE_DOUBLE:
                {
                alt112=3;
                }
                break;
            case RULE_BOOLEAN:
                {
                alt112=4;
                }
                break;
            case RULE_KEYWORD:
                {
                alt112=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 112, 0, input);

                throw nvae;
            }

            switch (alt112) {
                case 1 :
                    // InternalGaml.g:7814:3: this_StringLiteral_0= ruleStringLiteral
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getTerminalExpressionAccess().getStringLiteralParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StringLiteral_0=ruleStringLiteral();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StringLiteral_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:7823:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    {
                    // InternalGaml.g:7823:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    // InternalGaml.g:7824:4: () ( (lv_op_2_0= RULE_INTEGER ) )
                    {
                    // InternalGaml.g:7824:4: ()
                    // InternalGaml.g:7825:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getIntLiteralAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7831:4: ( (lv_op_2_0= RULE_INTEGER ) )
                    // InternalGaml.g:7832:5: (lv_op_2_0= RULE_INTEGER )
                    {
                    // InternalGaml.g:7832:5: (lv_op_2_0= RULE_INTEGER )
                    // InternalGaml.g:7833:6: lv_op_2_0= RULE_INTEGER
                    {
                    lv_op_2_0=(Token)match(input,RULE_INTEGER,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_2_0, grammarAccess.getTerminalExpressionAccess().getOpINTEGERTerminalRuleCall_1_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getTerminalExpressionRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"op",
                      							lv_op_2_0,
                      							"gaml.compiler.Gaml.INTEGER");
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // InternalGaml.g:7851:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    {
                    // InternalGaml.g:7851:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    // InternalGaml.g:7852:4: () ( (lv_op_4_0= RULE_DOUBLE ) )
                    {
                    // InternalGaml.g:7852:4: ()
                    // InternalGaml.g:7853:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getDoubleLiteralAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7859:4: ( (lv_op_4_0= RULE_DOUBLE ) )
                    // InternalGaml.g:7860:5: (lv_op_4_0= RULE_DOUBLE )
                    {
                    // InternalGaml.g:7860:5: (lv_op_4_0= RULE_DOUBLE )
                    // InternalGaml.g:7861:6: lv_op_4_0= RULE_DOUBLE
                    {
                    lv_op_4_0=(Token)match(input,RULE_DOUBLE,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_4_0, grammarAccess.getTerminalExpressionAccess().getOpDOUBLETerminalRuleCall_2_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getTerminalExpressionRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"op",
                      							lv_op_4_0,
                      							"gaml.compiler.Gaml.DOUBLE");
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:7879:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    {
                    // InternalGaml.g:7879:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    // InternalGaml.g:7880:4: () ( (lv_op_6_0= RULE_BOOLEAN ) )
                    {
                    // InternalGaml.g:7880:4: ()
                    // InternalGaml.g:7881:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getBooleanLiteralAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7887:4: ( (lv_op_6_0= RULE_BOOLEAN ) )
                    // InternalGaml.g:7888:5: (lv_op_6_0= RULE_BOOLEAN )
                    {
                    // InternalGaml.g:7888:5: (lv_op_6_0= RULE_BOOLEAN )
                    // InternalGaml.g:7889:6: lv_op_6_0= RULE_BOOLEAN
                    {
                    lv_op_6_0=(Token)match(input,RULE_BOOLEAN,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_6_0, grammarAccess.getTerminalExpressionAccess().getOpBOOLEANTerminalRuleCall_3_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getTerminalExpressionRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"op",
                      							lv_op_6_0,
                      							"gaml.compiler.Gaml.BOOLEAN");
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 5 :
                    // InternalGaml.g:7907:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    {
                    // InternalGaml.g:7907:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    // InternalGaml.g:7908:4: () ( (lv_op_8_0= RULE_KEYWORD ) )
                    {
                    // InternalGaml.g:7908:4: ()
                    // InternalGaml.g:7909:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getReservedLiteralAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7915:4: ( (lv_op_8_0= RULE_KEYWORD ) )
                    // InternalGaml.g:7916:5: (lv_op_8_0= RULE_KEYWORD )
                    {
                    // InternalGaml.g:7916:5: (lv_op_8_0= RULE_KEYWORD )
                    // InternalGaml.g:7917:6: lv_op_8_0= RULE_KEYWORD
                    {
                    lv_op_8_0=(Token)match(input,RULE_KEYWORD,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_op_8_0, grammarAccess.getTerminalExpressionAccess().getOpKEYWORDTerminalRuleCall_4_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getTerminalExpressionRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"op",
                      							lv_op_8_0,
                      							"gaml.compiler.Gaml.KEYWORD");
                      					
                    }

                    }


                    }


                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTerminalExpression"


    // $ANTLR start "entryRuleStringLiteral"
    // InternalGaml.g:7938:1: entryRuleStringLiteral returns [EObject current=null] : iv_ruleStringLiteral= ruleStringLiteral EOF ;
    public final EObject entryRuleStringLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStringLiteral = null;


        try {
            // InternalGaml.g:7938:54: (iv_ruleStringLiteral= ruleStringLiteral EOF )
            // InternalGaml.g:7939:2: iv_ruleStringLiteral= ruleStringLiteral EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStringLiteralRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStringLiteral=ruleStringLiteral();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStringLiteral; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStringLiteral"


    // $ANTLR start "ruleStringLiteral"
    // InternalGaml.g:7945:1: ruleStringLiteral returns [EObject current=null] : ( (lv_op_0_0= RULE_STRING ) ) ;
    public final EObject ruleStringLiteral() throws RecognitionException {
        EObject current = null;

        Token lv_op_0_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7951:2: ( ( (lv_op_0_0= RULE_STRING ) ) )
            // InternalGaml.g:7952:2: ( (lv_op_0_0= RULE_STRING ) )
            {
            // InternalGaml.g:7952:2: ( (lv_op_0_0= RULE_STRING ) )
            // InternalGaml.g:7953:3: (lv_op_0_0= RULE_STRING )
            {
            // InternalGaml.g:7953:3: (lv_op_0_0= RULE_STRING )
            // InternalGaml.g:7954:4: lv_op_0_0= RULE_STRING
            {
            lv_op_0_0=(Token)match(input,RULE_STRING,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              				newLeafNode(lv_op_0_0, grammarAccess.getStringLiteralAccess().getOpSTRINGTerminalRuleCall_0());
              			
            }
            if ( state.backtracking==0 ) {

              				if (current==null) {
              					current = createModelElement(grammarAccess.getStringLiteralRule());
              				}
              				setWithLastConsumed(
              					current,
              					"op",
              					lv_op_0_0,
              					"gaml.compiler.Gaml.STRING");
              			
            }

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStringLiteral"

    // $ANTLR start synpred1_InternalGaml
    public final void synpred1_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:80:4: ( '@' | 'model' )
        // InternalGaml.g:
        {
        if ( input.LA(1)==16||input.LA(1)==19 ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred1_InternalGaml

    // $ANTLR start synpred2_InternalGaml
    public final void synpred2_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1052:4: ( ruleS_Equations )
        // InternalGaml.g:1052:5: ruleS_Equations
        {
        pushFollow(FOLLOW_2);
        ruleS_Equations();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_InternalGaml

    // $ANTLR start synpred3_InternalGaml
    public final void synpred3_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1064:4: ( ruleS_Do )
        // InternalGaml.g:1064:5: ruleS_Do
        {
        pushFollow(FOLLOW_2);
        ruleS_Do();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_InternalGaml

    // $ANTLR start synpred4_InternalGaml
    public final void synpred4_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1076:4: ( ruleS_Loop )
        // InternalGaml.g:1076:5: ruleS_Loop
        {
        pushFollow(FOLLOW_2);
        ruleS_Loop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_InternalGaml

    // $ANTLR start synpred5_InternalGaml
    public final void synpred5_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1088:4: ( ruleS_Action )
        // InternalGaml.g:1088:5: ruleS_Action
        {
        pushFollow(FOLLOW_2);
        ruleS_Action();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_InternalGaml

    // $ANTLR start synpred6_InternalGaml
    public final void synpred6_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1100:4: ( ruleS_Var )
        // InternalGaml.g:1100:5: ruleS_Var
        {
        pushFollow(FOLLOW_2);
        ruleS_Var();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_InternalGaml

    // $ANTLR start synpred7_InternalGaml
    public final void synpred7_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1112:4: ( ruleS_Species )
        // InternalGaml.g:1112:5: ruleS_Species
        {
        pushFollow(FOLLOW_2);
        ruleS_Species();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_InternalGaml

    // $ANTLR start synpred8_InternalGaml
    public final void synpred8_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1124:4: ( ruleS_Reflex )
        // InternalGaml.g:1124:5: ruleS_Reflex
        {
        pushFollow(FOLLOW_2);
        ruleS_Reflex();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_InternalGaml

    // $ANTLR start synpred9_InternalGaml
    public final void synpred9_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1136:4: ( ruleS_Assignment )
        // InternalGaml.g:1136:5: ruleS_Assignment
        {
        pushFollow(FOLLOW_2);
        ruleS_Assignment();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_InternalGaml

    // $ANTLR start synpred10_InternalGaml
    public final void synpred10_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1148:4: ( ruleS_General )
        // InternalGaml.g:1148:5: ruleS_General
        {
        pushFollow(FOLLOW_2);
        ruleS_General();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_InternalGaml

    // $ANTLR start synpred11_InternalGaml
    public final void synpred11_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1160:4: ( ruleS_Declaration )
        // InternalGaml.g:1160:5: ruleS_Declaration
        {
        pushFollow(FOLLOW_2);
        ruleS_Declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_InternalGaml

    // $ANTLR start synpred12_InternalGaml
    public final void synpred12_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1172:4: ( ruleS_Definition )
        // InternalGaml.g:1172:5: ruleS_Definition
        {
        pushFollow(FOLLOW_2);
        ruleS_Definition();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_InternalGaml

    // $ANTLR start synpred13_InternalGaml
    public final void synpred13_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1536:5: ( 'else' )
        // InternalGaml.g:1536:6: 'else'
        {
        match(input,30,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_InternalGaml

    // $ANTLR start synpred14_InternalGaml
    public final void synpred14_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1634:5: ( 'catch' )
        // InternalGaml.g:1634:6: 'catch'
        {
        match(input,32,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_InternalGaml

    // $ANTLR start synpred15_InternalGaml
    public final void synpred15_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1799:4: ( 'species' | 'image' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==47||input.LA(1)==49 ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred15_InternalGaml

    // $ANTLR start synpred17_InternalGaml
    public final void synpred17_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:4825:5: ( ( ( ruleExpression ) ) )
        // InternalGaml.g:4825:6: ( ( ruleExpression ) )
        {
        // InternalGaml.g:4825:6: ( ( ruleExpression ) )
        // InternalGaml.g:4826:6: ( ruleExpression )
        {
        // InternalGaml.g:4826:6: ( ruleExpression )
        // InternalGaml.g:4827:7: ruleExpression
        {
        pushFollow(FOLLOW_2);
        ruleExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }
    }
    // $ANTLR end synpred17_InternalGaml

    // $ANTLR start synpred18_InternalGaml
    public final void synpred18_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:4922:5: ( 'species' | 'image' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==47||input.LA(1)==49 ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred18_InternalGaml

    // $ANTLR start synpred19_InternalGaml
    public final void synpred19_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:6492:4: ( ruleFunction )
        // InternalGaml.g:6492:5: ruleFunction
        {
        pushFollow(FOLLOW_2);
        ruleFunction();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_InternalGaml

    // $ANTLR start synpred20_InternalGaml
    public final void synpred20_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:7132:4: ( '>' )
        // InternalGaml.g:7132:5: '>'
        {
        match(input,103,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_InternalGaml

    // $ANTLR start synpred21_InternalGaml
    public final void synpred21_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:7314:4: ( ruleS_Declaration )
        // InternalGaml.g:7314:5: ruleS_Declaration
        {
        pushFollow(FOLLOW_2);
        ruleS_Declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_InternalGaml

    // Delegated rules

    public final boolean synpred9_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred18_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred18_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred17_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred17_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred19_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred19_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred20_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred20_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred21_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred21_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred15_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred15_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_InternalGaml_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA17 dfa17 = new DFA17(this);
    protected DFA28 dfa28 = new DFA28(this);
    protected DFA43 dfa43 = new DFA43(this);
    protected DFA58 dfa58 = new DFA58(this);
    protected DFA69 dfa69 = new DFA69(this);
    protected DFA70 dfa70 = new DFA70(this);
    protected DFA96 dfa96 = new DFA96(this);
    protected DFA101 dfa101 = new DFA101(this);
    protected DFA109 dfa109 = new DFA109(this);
    static final String dfa_1s = "\125\uffff";
    static final String dfa_2s = "\1\4\5\uffff\16\0\5\uffff\52\0\22\uffff";
    static final String dfa_3s = "\1\u0095\5\uffff\16\0\5\uffff\52\0\22\uffff";
    static final String dfa_4s = "\1\uffff\1\1\1\2\1\3\1\4\1\5\16\uffff\5\15\52\uffff\7\15\1\6\1\21\1\7\1\10\1\17\1\11\1\12\1\13\1\20\1\14\1\16";
    static final String dfa_5s = "\1\0\5\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\5\uffff\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\22\uffff}>";
    static final String[] dfa_6s = {
            "\1\102\1\24\1\25\1\26\1\27\1\30\12\uffff\1\104\6\uffff\1\11\1\4\2\uffff\1\5\1\uffff\1\2\3\uffff\1\103\1\uffff\1\12\1\105\3\uffff\1\1\1\6\1\3\1\17\1\20\1\31\3\uffff\1\101\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\73\1\74\1\75\1\76\1\77\1\100\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\7\1\10\1\13\1\14\1\15\1\16\1\21\1\22\1\23\51\uffff\1\107\3\uffff\1\106\1\110\1\111",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_1 = DFA.unpackEncodedString(dfa_1s);
    static final char[] dfa_2 = DFA.unpackEncodedStringToUnsignedChars(dfa_2s);
    static final char[] dfa_3 = DFA.unpackEncodedStringToUnsignedChars(dfa_3s);
    static final short[] dfa_4 = DFA.unpackEncodedString(dfa_4s);
    static final short[] dfa_5 = DFA.unpackEncodedString(dfa_5s);
    static final short[][] dfa_6 = unpackEncodedStringArray(dfa_6s);

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = dfa_1;
            this.eof = dfa_1;
            this.min = dfa_2;
            this.max = dfa_3;
            this.accept = dfa_4;
            this.special = dfa_5;
            this.transition = dfa_6;
        }
        public String getDescription() {
            return "1005:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | ( ( ruleS_Equations )=>this_S_Equations_5= ruleS_Equations ) | ( ( ruleS_Do )=>this_S_Do_6= ruleS_Do ) | ( ( ruleS_Loop )=>this_S_Loop_7= ruleS_Loop ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Var )=>this_S_Var_9= ruleS_Var ) | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_13= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_14= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_15= ruleS_Definition ) | this_S_Other_16= ruleS_Other )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA17_0 = input.LA(1);

                         
                        int index17_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA17_0==44) ) {s = 1;}

                        else if ( (LA17_0==33) ) {s = 2;}

                        else if ( (LA17_0==46) ) {s = 3;}

                        else if ( (LA17_0==28) ) {s = 4;}

                        else if ( (LA17_0==31) ) {s = 5;}

                        else if ( (LA17_0==45) ) {s = 6;}

                        else if ( (LA17_0==93) ) {s = 7;}

                        else if ( (LA17_0==94) ) {s = 8;}

                        else if ( (LA17_0==27) ) {s = 9;}

                        else if ( (LA17_0==39) ) {s = 10;}

                        else if ( (LA17_0==95) ) {s = 11;}

                        else if ( (LA17_0==96) ) {s = 12;}

                        else if ( (LA17_0==97) ) {s = 13;}

                        else if ( (LA17_0==98) ) {s = 14;}

                        else if ( (LA17_0==47) ) {s = 15;}

                        else if ( (LA17_0==48) ) {s = 16;}

                        else if ( (LA17_0==99) ) {s = 17;}

                        else if ( (LA17_0==100) ) {s = 18;}

                        else if ( (LA17_0==101) ) {s = 19;}

                        else if ( (LA17_0==RULE_STRING) && (synpred9_InternalGaml())) {s = 20;}

                        else if ( (LA17_0==RULE_INTEGER) && (synpred9_InternalGaml())) {s = 21;}

                        else if ( (LA17_0==RULE_DOUBLE) && (synpred9_InternalGaml())) {s = 22;}

                        else if ( (LA17_0==RULE_BOOLEAN) && (synpred9_InternalGaml())) {s = 23;}

                        else if ( (LA17_0==RULE_KEYWORD) && (synpred9_InternalGaml())) {s = 24;}

                        else if ( (LA17_0==49) ) {s = 25;}

                        else if ( (LA17_0==81) ) {s = 26;}

                        else if ( (LA17_0==82) ) {s = 27;}

                        else if ( (LA17_0==83) ) {s = 28;}

                        else if ( (LA17_0==84) ) {s = 29;}

                        else if ( (LA17_0==85) ) {s = 30;}

                        else if ( (LA17_0==86) ) {s = 31;}

                        else if ( (LA17_0==87) ) {s = 32;}

                        else if ( (LA17_0==88) ) {s = 33;}

                        else if ( (LA17_0==89) ) {s = 34;}

                        else if ( (LA17_0==90) ) {s = 35;}

                        else if ( (LA17_0==91) ) {s = 36;}

                        else if ( (LA17_0==92) ) {s = 37;}

                        else if ( (LA17_0==54) ) {s = 38;}

                        else if ( (LA17_0==55) ) {s = 39;}

                        else if ( (LA17_0==56) ) {s = 40;}

                        else if ( (LA17_0==57) ) {s = 41;}

                        else if ( (LA17_0==58) ) {s = 42;}

                        else if ( (LA17_0==59) ) {s = 43;}

                        else if ( (LA17_0==60) ) {s = 44;}

                        else if ( (LA17_0==61) ) {s = 45;}

                        else if ( (LA17_0==62) ) {s = 46;}

                        else if ( (LA17_0==63) ) {s = 47;}

                        else if ( (LA17_0==64) ) {s = 48;}

                        else if ( (LA17_0==65) ) {s = 49;}

                        else if ( (LA17_0==66) ) {s = 50;}

                        else if ( (LA17_0==67) ) {s = 51;}

                        else if ( (LA17_0==68) ) {s = 52;}

                        else if ( (LA17_0==69) ) {s = 53;}

                        else if ( (LA17_0==70) ) {s = 54;}

                        else if ( (LA17_0==71) ) {s = 55;}

                        else if ( (LA17_0==72) ) {s = 56;}

                        else if ( (LA17_0==73) ) {s = 57;}

                        else if ( (LA17_0==74) ) {s = 58;}

                        else if ( (LA17_0==75) ) {s = 59;}

                        else if ( (LA17_0==76) ) {s = 60;}

                        else if ( (LA17_0==77) ) {s = 61;}

                        else if ( (LA17_0==78) ) {s = 62;}

                        else if ( (LA17_0==79) ) {s = 63;}

                        else if ( (LA17_0==80) ) {s = 64;}

                        else if ( (LA17_0==53) ) {s = 65;}

                        else if ( (LA17_0==RULE_ID) ) {s = 66;}

                        else if ( (LA17_0==37) && (synpred9_InternalGaml())) {s = 67;}

                        else if ( (LA17_0==20) && (synpred9_InternalGaml())) {s = 68;}

                        else if ( (LA17_0==40) && (synpred9_InternalGaml())) {s = 69;}

                        else if ( (LA17_0==147) && (synpred9_InternalGaml())) {s = 70;}

                        else if ( (LA17_0==143) && (synpred9_InternalGaml())) {s = 71;}

                        else if ( (LA17_0==148) && (synpred9_InternalGaml())) {s = 72;}

                        else if ( (LA17_0==149) && (synpred9_InternalGaml())) {s = 73;}

                         
                        input.seek(index17_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA17_6 = input.LA(1);

                         
                        int index17_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_InternalGaml()) ) {s = 74;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_6);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA17_7 = input.LA(1);

                         
                        int index17_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_InternalGaml()) ) {s = 76;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA17_8 = input.LA(1);

                         
                        int index17_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_InternalGaml()) ) {s = 76;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA17_9 = input.LA(1);

                         
                        int index17_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 77;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                         
                        input.seek(index17_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA17_10 = input.LA(1);

                         
                        int index17_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_InternalGaml()) ) {s = 79;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                         
                        input.seek(index17_10);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA17_11 = input.LA(1);

                         
                        int index17_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 80;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA17_12 = input.LA(1);

                         
                        int index17_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 80;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA17_13 = input.LA(1);

                         
                        int index17_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 80;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_13);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA17_14 = input.LA(1);

                         
                        int index17_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 80;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_14);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA17_15 = input.LA(1);

                         
                        int index17_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_InternalGaml()) ) {s = 81;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (synpred12_InternalGaml()) ) {s = 82;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_15);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA17_16 = input.LA(1);

                         
                        int index17_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_InternalGaml()) ) {s = 81;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_16);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA17_17 = input.LA(1);

                         
                        int index17_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 83;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_17);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA17_18 = input.LA(1);

                         
                        int index17_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 83;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_18);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA17_19 = input.LA(1);

                         
                        int index17_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 83;}

                        else if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_19);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA17_25 = input.LA(1);

                         
                        int index17_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (synpred12_InternalGaml()) ) {s = 82;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_25);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA17_26 = input.LA(1);

                         
                        int index17_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_26);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA17_27 = input.LA(1);

                         
                        int index17_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_27);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA17_28 = input.LA(1);

                         
                        int index17_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_28);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA17_29 = input.LA(1);

                         
                        int index17_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_29);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA17_30 = input.LA(1);

                         
                        int index17_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_30);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA17_31 = input.LA(1);

                         
                        int index17_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_31);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA17_32 = input.LA(1);

                         
                        int index17_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_32);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA17_33 = input.LA(1);

                         
                        int index17_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_33);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA17_34 = input.LA(1);

                         
                        int index17_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_34);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA17_35 = input.LA(1);

                         
                        int index17_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_35);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA17_36 = input.LA(1);

                         
                        int index17_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_36);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA17_37 = input.LA(1);

                         
                        int index17_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_37);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA17_38 = input.LA(1);

                         
                        int index17_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_38);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA17_39 = input.LA(1);

                         
                        int index17_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_39);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA17_40 = input.LA(1);

                         
                        int index17_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_40);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA17_41 = input.LA(1);

                         
                        int index17_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_41);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA17_42 = input.LA(1);

                         
                        int index17_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_42);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA17_43 = input.LA(1);

                         
                        int index17_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_43);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA17_44 = input.LA(1);

                         
                        int index17_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_44);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA17_45 = input.LA(1);

                         
                        int index17_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_45);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA17_46 = input.LA(1);

                         
                        int index17_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_46);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA17_47 = input.LA(1);

                         
                        int index17_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_47);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA17_48 = input.LA(1);

                         
                        int index17_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_48);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA17_49 = input.LA(1);

                         
                        int index17_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_49);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA17_50 = input.LA(1);

                         
                        int index17_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_50);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA17_51 = input.LA(1);

                         
                        int index17_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_51);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA17_52 = input.LA(1);

                         
                        int index17_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_52);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA17_53 = input.LA(1);

                         
                        int index17_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_53);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA17_54 = input.LA(1);

                         
                        int index17_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_54);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA17_55 = input.LA(1);

                         
                        int index17_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_55);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA17_56 = input.LA(1);

                         
                        int index17_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_56);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA17_57 = input.LA(1);

                         
                        int index17_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_57);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA17_58 = input.LA(1);

                         
                        int index17_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_58);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA17_59 = input.LA(1);

                         
                        int index17_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_59);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA17_60 = input.LA(1);

                         
                        int index17_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_60);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA17_61 = input.LA(1);

                         
                        int index17_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_61);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA17_62 = input.LA(1);

                         
                        int index17_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_62);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA17_63 = input.LA(1);

                         
                        int index17_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_63);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA17_64 = input.LA(1);

                         
                        int index17_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred10_InternalGaml()) ) {s = 84;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_64);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA17_65 = input.LA(1);

                         
                        int index17_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_65);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA17_66 = input.LA(1);

                         
                        int index17_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_InternalGaml()) ) {s = 73;}

                        else if ( (synpred11_InternalGaml()) ) {s = 78;}

                        else if ( (synpred12_InternalGaml()) ) {s = 82;}

                        else if ( (true) ) {s = 75;}

                         
                        input.seek(index17_66);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 17, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_7s = "\12\uffff";
    static final String dfa_8s = "\1\4\1\uffff\1\4\7\uffff";
    static final String dfa_9s = "\1\145\1\uffff\1\u008d\7\uffff";
    static final String dfa_10s = "\1\uffff\1\1\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\1";
    static final String dfa_11s = "\1\0\1\uffff\1\1\7\uffff}>";
    static final String[] dfa_12s = {
            "\1\1\26\uffff\1\10\13\uffff\1\6\7\uffff\1\2\1\4\1\3\55\uffff\4\7\3\5",
            "",
            "\1\4\21\uffff\1\4\166\uffff\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_7 = DFA.unpackEncodedString(dfa_7s);
    static final char[] dfa_8 = DFA.unpackEncodedStringToUnsignedChars(dfa_8s);
    static final char[] dfa_9 = DFA.unpackEncodedStringToUnsignedChars(dfa_9s);
    static final short[] dfa_10 = DFA.unpackEncodedString(dfa_10s);
    static final short[] dfa_11 = DFA.unpackEncodedString(dfa_11s);
    static final short[][] dfa_12 = unpackEncodedStringArray(dfa_12s);

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_8;
            this.max = dfa_9;
            this.accept = dfa_10;
            this.special = dfa_11;
            this.transition = dfa_12;
        }
        public String getDescription() {
            return "1797:2: ( ( ( 'species' | 'image' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Var_4= ruleS_Var | this_S_Loop_5= ruleS_Loop )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA28_0 = input.LA(1);

                         
                        int index28_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_0==RULE_ID) && (synpred15_InternalGaml())) {s = 1;}

                        else if ( (LA28_0==47) ) {s = 2;}

                        else if ( (LA28_0==49) && (synpred15_InternalGaml())) {s = 3;}

                        else if ( (LA28_0==48) ) {s = 4;}

                        else if ( ((LA28_0>=99 && LA28_0<=101)) ) {s = 5;}

                        else if ( (LA28_0==39) ) {s = 6;}

                        else if ( ((LA28_0>=95 && LA28_0<=98)) ) {s = 7;}

                        else if ( (LA28_0==27) ) {s = 8;}

                         
                        input.seek(index28_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA28_2 = input.LA(1);

                         
                        int index28_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA28_2==RULE_ID||LA28_2==22) ) {s = 4;}

                        else if ( (LA28_2==141) && (synpred15_InternalGaml())) {s = 9;}

                         
                        input.seek(index28_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 28, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_13s = "\71\uffff";
    static final String dfa_14s = "\1\4\66\45\2\uffff";
    static final String dfa_15s = "\1\145\66\u008d\2\uffff";
    static final String dfa_16s = "\67\uffff\1\2\1\1";
    static final String dfa_17s = "\71\uffff}>";
    static final String[] dfa_18s = {
            "\1\66\50\uffff\1\64\1\uffff\1\1\1\2\1\3\3\uffff\1\65\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\4\1\5\1\11\1\12\1\13\1\14\1\6\1\7\1\10",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "\1\70\4\uffff\1\67\142\uffff\1\70",
            "",
            ""
    };

    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final char[] dfa_14 = DFA.unpackEncodedStringToUnsignedChars(dfa_14s);
    static final char[] dfa_15 = DFA.unpackEncodedStringToUnsignedChars(dfa_15s);
    static final short[] dfa_16 = DFA.unpackEncodedString(dfa_16s);
    static final short[] dfa_17 = DFA.unpackEncodedString(dfa_17s);
    static final short[][] dfa_18 = unpackEncodedStringArray(dfa_18s);

    class DFA43 extends DFA {

        public DFA43(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 43;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_14;
            this.max = dfa_15;
            this.accept = dfa_16;
            this.special = dfa_17;
            this.transition = dfa_18;
        }
        public String getDescription() {
            return "2557:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )";
        }
    }
    static final String dfa_19s = "\1\17\2\uffff\1\147\6\uffff";
    static final String dfa_20s = "\1\153\2\uffff\1\151\6\uffff";
    static final String dfa_21s = "\1\uffff\1\1\1\2\1\uffff\1\4\1\6\1\7\1\10\1\5\1\3";
    static final String dfa_22s = "\12\uffff}>";
    static final String[] dfa_23s = {
            "\1\1\126\uffff\1\2\1\3\1\4\1\7\1\5\1\6",
            "",
            "",
            "\1\11\1\uffff\1\10",
            "",
            "",
            "",
            "",
            "",
            ""
    };
    static final char[] dfa_19 = DFA.unpackEncodedStringToUnsignedChars(dfa_19s);
    static final char[] dfa_20 = DFA.unpackEncodedStringToUnsignedChars(dfa_20s);
    static final short[] dfa_21 = DFA.unpackEncodedString(dfa_21s);
    static final short[] dfa_22 = DFA.unpackEncodedString(dfa_22s);
    static final short[][] dfa_23 = unpackEncodedStringArray(dfa_23s);

    class DFA58 extends DFA {

        public DFA58(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 58;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_19;
            this.max = dfa_20;
            this.accept = dfa_21;
            this.special = dfa_22;
            this.transition = dfa_23;
        }
        public String getDescription() {
            return "3964:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )";
        }
    }
    static final String dfa_24s = "\104\uffff";
    static final String dfa_25s = "\1\4\75\uffff\1\0\5\uffff";
    static final String dfa_26s = "\1\u0095\75\uffff\1\0\5\uffff";
    static final String dfa_27s = "\1\uffff\75\1\1\uffff\4\1\1\2";
    static final String dfa_28s = "\1\0\75\uffff\1\1\5\uffff}>";
    static final String[] dfa_29s = {
            "\1\73\1\1\1\2\1\3\1\4\1\5\12\uffff\1\75\20\uffff\1\74\2\uffff\1\76\4\uffff\1\71\1\uffff\1\6\1\7\1\10\3\uffff\1\72\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\11\1\12\1\16\1\17\1\20\1\21\1\13\1\14\1\15\51\uffff\1\100\3\uffff\1\77\1\101\1\102",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_24 = DFA.unpackEncodedString(dfa_24s);
    static final char[] dfa_25 = DFA.unpackEncodedStringToUnsignedChars(dfa_25s);
    static final char[] dfa_26 = DFA.unpackEncodedStringToUnsignedChars(dfa_26s);
    static final short[] dfa_27 = DFA.unpackEncodedString(dfa_27s);
    static final short[] dfa_28 = DFA.unpackEncodedString(dfa_28s);
    static final short[][] dfa_29 = unpackEncodedStringArray(dfa_29s);

    class DFA69 extends DFA {

        public DFA69(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 69;
            this.eot = dfa_24;
            this.eof = dfa_24;
            this.min = dfa_25;
            this.max = dfa_26;
            this.accept = dfa_27;
            this.special = dfa_28;
            this.transition = dfa_29;
        }
        public String getDescription() {
            return "4823:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA69_0 = input.LA(1);

                         
                        int index69_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA69_0==RULE_STRING) && (synpred17_InternalGaml())) {s = 1;}

                        else if ( (LA69_0==RULE_INTEGER) && (synpred17_InternalGaml())) {s = 2;}

                        else if ( (LA69_0==RULE_DOUBLE) && (synpred17_InternalGaml())) {s = 3;}

                        else if ( (LA69_0==RULE_BOOLEAN) && (synpred17_InternalGaml())) {s = 4;}

                        else if ( (LA69_0==RULE_KEYWORD) && (synpred17_InternalGaml())) {s = 5;}

                        else if ( (LA69_0==47) && (synpred17_InternalGaml())) {s = 6;}

                        else if ( (LA69_0==48) && (synpred17_InternalGaml())) {s = 7;}

                        else if ( (LA69_0==49) && (synpred17_InternalGaml())) {s = 8;}

                        else if ( (LA69_0==93) && (synpred17_InternalGaml())) {s = 9;}

                        else if ( (LA69_0==94) && (synpred17_InternalGaml())) {s = 10;}

                        else if ( (LA69_0==99) && (synpred17_InternalGaml())) {s = 11;}

                        else if ( (LA69_0==100) && (synpred17_InternalGaml())) {s = 12;}

                        else if ( (LA69_0==101) && (synpred17_InternalGaml())) {s = 13;}

                        else if ( (LA69_0==95) && (synpred17_InternalGaml())) {s = 14;}

                        else if ( (LA69_0==96) && (synpred17_InternalGaml())) {s = 15;}

                        else if ( (LA69_0==97) && (synpred17_InternalGaml())) {s = 16;}

                        else if ( (LA69_0==98) && (synpred17_InternalGaml())) {s = 17;}

                        else if ( (LA69_0==81) && (synpred17_InternalGaml())) {s = 18;}

                        else if ( (LA69_0==82) && (synpred17_InternalGaml())) {s = 19;}

                        else if ( (LA69_0==83) && (synpred17_InternalGaml())) {s = 20;}

                        else if ( (LA69_0==84) && (synpred17_InternalGaml())) {s = 21;}

                        else if ( (LA69_0==85) && (synpred17_InternalGaml())) {s = 22;}

                        else if ( (LA69_0==86) && (synpred17_InternalGaml())) {s = 23;}

                        else if ( (LA69_0==87) && (synpred17_InternalGaml())) {s = 24;}

                        else if ( (LA69_0==88) && (synpred17_InternalGaml())) {s = 25;}

                        else if ( (LA69_0==89) && (synpred17_InternalGaml())) {s = 26;}

                        else if ( (LA69_0==90) && (synpred17_InternalGaml())) {s = 27;}

                        else if ( (LA69_0==91) && (synpred17_InternalGaml())) {s = 28;}

                        else if ( (LA69_0==92) && (synpred17_InternalGaml())) {s = 29;}

                        else if ( (LA69_0==54) && (synpred17_InternalGaml())) {s = 30;}

                        else if ( (LA69_0==55) && (synpred17_InternalGaml())) {s = 31;}

                        else if ( (LA69_0==56) && (synpred17_InternalGaml())) {s = 32;}

                        else if ( (LA69_0==57) && (synpred17_InternalGaml())) {s = 33;}

                        else if ( (LA69_0==58) && (synpred17_InternalGaml())) {s = 34;}

                        else if ( (LA69_0==59) && (synpred17_InternalGaml())) {s = 35;}

                        else if ( (LA69_0==60) && (synpred17_InternalGaml())) {s = 36;}

                        else if ( (LA69_0==61) && (synpred17_InternalGaml())) {s = 37;}

                        else if ( (LA69_0==62) && (synpred17_InternalGaml())) {s = 38;}

                        else if ( (LA69_0==63) && (synpred17_InternalGaml())) {s = 39;}

                        else if ( (LA69_0==64) && (synpred17_InternalGaml())) {s = 40;}

                        else if ( (LA69_0==65) && (synpred17_InternalGaml())) {s = 41;}

                        else if ( (LA69_0==66) && (synpred17_InternalGaml())) {s = 42;}

                        else if ( (LA69_0==67) && (synpred17_InternalGaml())) {s = 43;}

                        else if ( (LA69_0==68) && (synpred17_InternalGaml())) {s = 44;}

                        else if ( (LA69_0==69) && (synpred17_InternalGaml())) {s = 45;}

                        else if ( (LA69_0==70) && (synpred17_InternalGaml())) {s = 46;}

                        else if ( (LA69_0==71) && (synpred17_InternalGaml())) {s = 47;}

                        else if ( (LA69_0==72) && (synpred17_InternalGaml())) {s = 48;}

                        else if ( (LA69_0==73) && (synpred17_InternalGaml())) {s = 49;}

                        else if ( (LA69_0==74) && (synpred17_InternalGaml())) {s = 50;}

                        else if ( (LA69_0==75) && (synpred17_InternalGaml())) {s = 51;}

                        else if ( (LA69_0==76) && (synpred17_InternalGaml())) {s = 52;}

                        else if ( (LA69_0==77) && (synpred17_InternalGaml())) {s = 53;}

                        else if ( (LA69_0==78) && (synpred17_InternalGaml())) {s = 54;}

                        else if ( (LA69_0==79) && (synpred17_InternalGaml())) {s = 55;}

                        else if ( (LA69_0==80) && (synpred17_InternalGaml())) {s = 56;}

                        else if ( (LA69_0==45) && (synpred17_InternalGaml())) {s = 57;}

                        else if ( (LA69_0==53) && (synpred17_InternalGaml())) {s = 58;}

                        else if ( (LA69_0==RULE_ID) && (synpred17_InternalGaml())) {s = 59;}

                        else if ( (LA69_0==37) && (synpred17_InternalGaml())) {s = 60;}

                        else if ( (LA69_0==20) && (synpred17_InternalGaml())) {s = 61;}

                        else if ( (LA69_0==40) ) {s = 62;}

                        else if ( (LA69_0==147) && (synpred17_InternalGaml())) {s = 63;}

                        else if ( (LA69_0==143) && (synpred17_InternalGaml())) {s = 64;}

                        else if ( (LA69_0==148) && (synpred17_InternalGaml())) {s = 65;}

                        else if ( (LA69_0==149) && (synpred17_InternalGaml())) {s = 66;}

                         
                        input.seek(index69_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA69_62 = input.LA(1);

                         
                        int index69_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 66;}

                        else if ( (true) ) {s = 67;}

                         
                        input.seek(index69_62);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 69, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_30s = "\1\4\3\0\100\uffff";
    static final String dfa_31s = "\1\u0095\3\0\100\uffff";
    static final String dfa_32s = "\4\uffff\1\2\76\uffff\1\1";
    static final String dfa_33s = "\1\uffff\1\0\1\1\1\2\100\uffff}>";
    static final String[] dfa_34s = {
            "\1\1\5\4\12\uffff\1\4\20\uffff\1\4\2\uffff\1\4\4\uffff\1\4\1\uffff\1\2\1\4\1\3\3\uffff\61\4\51\uffff\1\4\3\uffff\3\4",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };
    static final char[] dfa_30 = DFA.unpackEncodedStringToUnsignedChars(dfa_30s);
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final short[] dfa_32 = DFA.unpackEncodedString(dfa_32s);
    static final short[] dfa_33 = DFA.unpackEncodedString(dfa_33s);
    static final short[][] dfa_34 = unpackEncodedStringArray(dfa_34s);

    class DFA70 extends DFA {

        public DFA70(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 70;
            this.eot = dfa_24;
            this.eof = dfa_24;
            this.min = dfa_30;
            this.max = dfa_31;
            this.accept = dfa_32;
            this.special = dfa_33;
            this.transition = dfa_34;
        }
        public String getDescription() {
            return "4920:3: ( ( ( 'species' | 'image' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA70_1 = input.LA(1);

                         
                        int index70_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_InternalGaml()) ) {s = 67;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index70_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA70_2 = input.LA(1);

                         
                        int index70_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_InternalGaml()) ) {s = 67;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index70_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA70_3 = input.LA(1);

                         
                        int index70_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_InternalGaml()) ) {s = 67;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index70_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 70, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_35s = "\1\4\66\0\2\uffff";
    static final String dfa_36s = "\1\145\66\0\2\uffff";
    static final String dfa_37s = "\67\uffff\1\1\1\2";
    static final String dfa_38s = "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\65\2\uffff}>";
    static final String[] dfa_39s = {
            "\1\66\50\uffff\1\64\1\uffff\1\1\1\2\1\3\3\uffff\1\65\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\4\1\5\1\11\1\12\1\13\1\14\1\6\1\7\1\10",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };
    static final char[] dfa_35 = DFA.unpackEncodedStringToUnsignedChars(dfa_35s);
    static final char[] dfa_36 = DFA.unpackEncodedStringToUnsignedChars(dfa_36s);
    static final short[] dfa_37 = DFA.unpackEncodedString(dfa_37s);
    static final short[] dfa_38 = DFA.unpackEncodedString(dfa_38s);
    static final short[][] dfa_39 = unpackEncodedStringArray(dfa_39s);

    class DFA96 extends DFA {

        public DFA96(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 96;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_35;
            this.max = dfa_36;
            this.accept = dfa_37;
            this.special = dfa_38;
            this.transition = dfa_39;
        }
        public String getDescription() {
            return "6490:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA96_1 = input.LA(1);

                         
                        int index96_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA96_2 = input.LA(1);

                         
                        int index96_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA96_3 = input.LA(1);

                         
                        int index96_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA96_4 = input.LA(1);

                         
                        int index96_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA96_5 = input.LA(1);

                         
                        int index96_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA96_6 = input.LA(1);

                         
                        int index96_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA96_7 = input.LA(1);

                         
                        int index96_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA96_8 = input.LA(1);

                         
                        int index96_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA96_9 = input.LA(1);

                         
                        int index96_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA96_10 = input.LA(1);

                         
                        int index96_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA96_11 = input.LA(1);

                         
                        int index96_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA96_12 = input.LA(1);

                         
                        int index96_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA96_13 = input.LA(1);

                         
                        int index96_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA96_14 = input.LA(1);

                         
                        int index96_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA96_15 = input.LA(1);

                         
                        int index96_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA96_16 = input.LA(1);

                         
                        int index96_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA96_17 = input.LA(1);

                         
                        int index96_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA96_18 = input.LA(1);

                         
                        int index96_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA96_19 = input.LA(1);

                         
                        int index96_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA96_20 = input.LA(1);

                         
                        int index96_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA96_21 = input.LA(1);

                         
                        int index96_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA96_22 = input.LA(1);

                         
                        int index96_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA96_23 = input.LA(1);

                         
                        int index96_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA96_24 = input.LA(1);

                         
                        int index96_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA96_25 = input.LA(1);

                         
                        int index96_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA96_26 = input.LA(1);

                         
                        int index96_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA96_27 = input.LA(1);

                         
                        int index96_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA96_28 = input.LA(1);

                         
                        int index96_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA96_29 = input.LA(1);

                         
                        int index96_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA96_30 = input.LA(1);

                         
                        int index96_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_30);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA96_31 = input.LA(1);

                         
                        int index96_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_31);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA96_32 = input.LA(1);

                         
                        int index96_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_32);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA96_33 = input.LA(1);

                         
                        int index96_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_33);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA96_34 = input.LA(1);

                         
                        int index96_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_34);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA96_35 = input.LA(1);

                         
                        int index96_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_35);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA96_36 = input.LA(1);

                         
                        int index96_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_36);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA96_37 = input.LA(1);

                         
                        int index96_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_37);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA96_38 = input.LA(1);

                         
                        int index96_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_38);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA96_39 = input.LA(1);

                         
                        int index96_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_39);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA96_40 = input.LA(1);

                         
                        int index96_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_40);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA96_41 = input.LA(1);

                         
                        int index96_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_41);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA96_42 = input.LA(1);

                         
                        int index96_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_42);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA96_43 = input.LA(1);

                         
                        int index96_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_43);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA96_44 = input.LA(1);

                         
                        int index96_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_44);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA96_45 = input.LA(1);

                         
                        int index96_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_45);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA96_46 = input.LA(1);

                         
                        int index96_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_46);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA96_47 = input.LA(1);

                         
                        int index96_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_47);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA96_48 = input.LA(1);

                         
                        int index96_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_48);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA96_49 = input.LA(1);

                         
                        int index96_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_49);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA96_50 = input.LA(1);

                         
                        int index96_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_50);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA96_51 = input.LA(1);

                         
                        int index96_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_51);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA96_52 = input.LA(1);

                         
                        int index96_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_52);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA96_53 = input.LA(1);

                         
                        int index96_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_53);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA96_54 = input.LA(1);

                         
                        int index96_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 56;}

                         
                        input.seek(index96_54);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 96, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_40s = "\2\uffff\66\1\1\uffff";
    static final String dfa_41s = "\1\4\1\uffff\66\4\1\uffff";
    static final String dfa_42s = "\1\u0095\1\uffff\66\u0096\1\uffff";
    static final String dfa_43s = "\1\uffff\1\1\66\uffff\1\2";
    static final String[] dfa_44s = {
            "\1\67\5\1\12\uffff\1\1\1\uffff\1\70\3\uffff\1\70\7\uffff\2\70\1\uffff\1\1\2\uffff\1\1\4\uffff\1\65\1\uffff\1\2\1\3\1\4\3\uffff\1\66\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\5\1\6\1\12\1\13\1\14\1\15\1\7\1\10\1\11\7\uffff\30\70\12\uffff\1\1\3\uffff\3\1",
            "",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\16\uffff\1\70\2\1\3\uffff\1\1\2\uffff\1\1\1\uffff\3\1\3\uffff\61\1\1\uffff\1\1\4\uffff\1\1\31\uffff\16\1\2\uffff\1\1",
            ""
    };
    static final short[] dfa_40 = DFA.unpackEncodedString(dfa_40s);
    static final char[] dfa_41 = DFA.unpackEncodedStringToUnsignedChars(dfa_41s);
    static final char[] dfa_42 = DFA.unpackEncodedStringToUnsignedChars(dfa_42s);
    static final short[] dfa_43 = DFA.unpackEncodedString(dfa_43s);
    static final short[][] dfa_44 = unpackEncodedStringArray(dfa_44s);

    class DFA101 extends DFA {

        public DFA101(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 101;
            this.eot = dfa_13;
            this.eof = dfa_40;
            this.min = dfa_41;
            this.max = dfa_42;
            this.accept = dfa_43;
            this.special = dfa_17;
            this.transition = dfa_44;
        }
        public String getDescription() {
            return "6620:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )";
        }
    }
    static final String dfa_45s = "\26\uffff";
    static final String dfa_46s = "\1\4\3\0\22\uffff";
    static final String dfa_47s = "\1\u009b\3\0\22\uffff";
    static final String dfa_48s = "\4\uffff\12\1\1\2\1\uffff\1\4\1\uffff\1\5\1\6\1\7\1\3";
    static final String dfa_49s = "\1\0\1\1\1\2\1\3\22\uffff}>";
    static final String[] dfa_50s = {
            "\1\1\13\uffff\1\16\1\23\1\uffff\1\16\2\uffff\1\20\4\uffff\1\15\13\uffff\1\10\7\uffff\1\2\1\4\1\3\3\uffff\1\24\51\uffff\1\11\1\12\1\13\1\14\1\5\1\6\1\7\7\uffff\1\20\55\uffff\1\22",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_45 = DFA.unpackEncodedString(dfa_45s);
    static final char[] dfa_46 = DFA.unpackEncodedStringToUnsignedChars(dfa_46s);
    static final char[] dfa_47 = DFA.unpackEncodedStringToUnsignedChars(dfa_47s);
    static final short[] dfa_48 = DFA.unpackEncodedString(dfa_48s);
    static final short[] dfa_49 = DFA.unpackEncodedString(dfa_49s);
    static final short[][] dfa_50 = unpackEncodedStringArray(dfa_50s);

    class DFA109 extends DFA {

        public DFA109(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 109;
            this.eot = dfa_45;
            this.eof = dfa_45;
            this.min = dfa_46;
            this.max = dfa_47;
            this.accept = dfa_48;
            this.special = dfa_49;
            this.transition = dfa_50;
        }
        public String getDescription() {
            return "7312:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA109_0 = input.LA(1);

                         
                        int index109_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA109_0==RULE_ID) ) {s = 1;}

                        else if ( (LA109_0==47) ) {s = 2;}

                        else if ( (LA109_0==49) ) {s = 3;}

                        else if ( (LA109_0==48) && (synpred21_InternalGaml())) {s = 4;}

                        else if ( (LA109_0==99) && (synpred21_InternalGaml())) {s = 5;}

                        else if ( (LA109_0==100) && (synpred21_InternalGaml())) {s = 6;}

                        else if ( (LA109_0==101) && (synpred21_InternalGaml())) {s = 7;}

                        else if ( (LA109_0==39) && (synpred21_InternalGaml())) {s = 8;}

                        else if ( (LA109_0==95) && (synpred21_InternalGaml())) {s = 9;}

                        else if ( (LA109_0==96) && (synpred21_InternalGaml())) {s = 10;}

                        else if ( (LA109_0==97) && (synpred21_InternalGaml())) {s = 11;}

                        else if ( (LA109_0==98) && (synpred21_InternalGaml())) {s = 12;}

                        else if ( (LA109_0==27) && (synpred21_InternalGaml())) {s = 13;}

                        else if ( (LA109_0==16||LA109_0==19) ) {s = 14;}

                        else if ( (LA109_0==22||LA109_0==109) ) {s = 16;}

                        else if ( (LA109_0==155) ) {s = 18;}

                        else if ( (LA109_0==17) ) {s = 19;}

                        else if ( (LA109_0==53) ) {s = 20;}

                         
                        input.seek(index109_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA109_1 = input.LA(1);

                         
                        int index109_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_InternalGaml()) ) {s = 13;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index109_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA109_2 = input.LA(1);

                         
                        int index109_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_InternalGaml()) ) {s = 13;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index109_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA109_3 = input.LA(1);

                         
                        int index109_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_InternalGaml()) ) {s = 13;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index109_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 109, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0xFFE3A120001003F0L,0x0000003FFFFFFFFFL,0x0000000000388000L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000090000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0xFFE3A00000000010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0021800002020000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0021800002000002L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0xFFE3A12C047003F0L,0xFFFFE03FFFFFFFFFL,0x000000000038801FL});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0xFFE3A00000400030L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0xFFE3A00000000030L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000010C05C08010L,0xFFFFE00000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000010C05408010L,0xFFFFE00000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000000400010L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0xFFE3A12C045003F0L,0xFFFFE03FFFFFFFFFL,0x000000000038801FL});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0xFFE3A00004000010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000010C04408010L,0xFFFFE00000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0xFFE3A120201003F0L,0x0000003FFFFFFFFFL,0x0000000000388000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000010010000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0xFFE3A124011003F0L,0x0000003FFFFFFFFFL,0x0000000000388000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0xFFE3A120011003F0L,0x0000003FFFFFFFFFL,0x0000000000388000L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0xFFE3A10800400010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000010800000000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0000012C05C08010L,0xFFFFE00000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0002800000000010L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0xFFE3A00000400010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000000C05408010L,0xFFFFE00000000000L,0x000000000000003FL});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000000000008000L,0x00000FC000000000L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0xFFE3A20000000010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0xFFE3A80000000010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x001F820000000000L,0x000000001FFE0000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000000L,0x0000020000000000L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000002L,0x0000100000000000L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0xFFE3A10000000010L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0xFFE3F3A2981003F0L,0x0000003FFFFFFFFFL,0x0000000000388000L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0002802000000010L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000040000000002L,0x0000008000000000L,0x0000000000003C00L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x000000000000C000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000070000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0xFFE3A00000000012L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0x0000000000100002L,0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0xFFE3A120001003F0L,0x0000003FFFFFFFFFL});
    public static final BitSet FOLLOW_63 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_64 = new BitSet(new long[]{0x0000020000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_65 = new BitSet(new long[]{0x0000002000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_66 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_67 = new BitSet(new long[]{0xFFE3A16C045003F0L,0xFFFFE03FFFFFFFFFL,0x000000000038801FL});
    public static final BitSet FOLLOW_68 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_69 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_70 = new BitSet(new long[]{0x0000000000000000L,0x0000108000000000L});

}