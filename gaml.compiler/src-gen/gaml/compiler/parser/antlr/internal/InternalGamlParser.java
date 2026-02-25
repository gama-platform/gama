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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_STRING", "RULE_INTEGER", "RULE_DOUBLE", "RULE_BOOLEAN", "RULE_KEYWORD", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'__synthetic__'", "'<-'", "'model'", "'import'", "'as'", "'@'", "'['", "']'", "'model:'", "';'", "'global'", "'do'", "'invoke'", "'loop'", "'if'", "'else'", "'try'", "'catch'", "'return'", "'when'", "':'", "'('", "')'", "'action'", "'equation'", "'{'", "'}'", "'='", "'display'", "'picture'", "'graphics'", "'event'", "'overlay'", "'solve'", "'species'", "'grid'", "'experiment'", "'ask'", "'release'", "'capture'", "'create'", "'write'", "'error'", "'warn'", "'exception'", "'save'", "'assert'", "'inspect'", "'browse'", "'restore'", "'draw'", "'using'", "'switch'", "'put'", "'add'", "'remove'", "'match'", "'match_between'", "'match_one'", "'parameter'", "'status'", "'highlight'", "'focus_on'", "'layout'", "'light'", "'camera'", "'text'", "'data'", "'chart'", "'agents'", "'display_population'", "'display_grid'", "'datalist'", "'mesh'", "'rotation'", "'init'", "'reflex'", "'aspect'", "'<<'", "'>'", "'<<+'", "'>-'", "'+<-'", "'<+'", "','", "'name:'", "'returns:'", "'as:'", "'of:'", "'parent:'", "'species:'", "'type:'", "'camera:'", "'data:'", "'const:'", "'value:'", "'topology:'", "'item:'", "'init:'", "'message:'", "'control:'", "'layout:'", "'environment:'", "'text:'", "'image:'", "'using:'", "'parameter:'", "'aspect:'", "'light:'", "'action:'", "'on_change:'", "'var:'", "'->'", "'::'", "'?'", "'or'", "'and'", "'!='", "'>='", "'<='", "'<'", "'+'", "'-'", "'*'", "'/'", "'^'", "'#'", "'!'", "'not'", "'.'", "'**unit*'", "'**type*'", "'**action*'", "'**skill*'", "'**var*'", "'**equation*'"
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
    public static final int RULE_KEYWORD=9;
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
            else if ( (LA1_0==50) ) {
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

                if ( (LA4_0==24||(LA4_0>=48 && LA4_0<=50)) ) {
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

                    if ( ((LA6_0>=RULE_ID && LA6_0<=RULE_KEYWORD)||LA6_0==20||LA6_0==33||LA6_0==35||LA6_0==39||(LA6_0>=48 && LA6_0<=91)||(LA6_0>=99 && LA6_0<=125)||LA6_0==136||(LA6_0>=140 && LA6_0<=142)) ) {
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
    // InternalGaml.g:548:1: ruleHeadlessExperiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleHeadlessExperiment() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        Token otherlv_2=null;
        Token lv_importURI_3_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject this_FacetsAndBlock_4 = null;



        	enterRule();

        try {
            // InternalGaml.g:554:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:555:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:555:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:556:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current]
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

            // InternalGaml.g:575:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:576:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:576:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:577:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:577:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==RULE_ID||(LA8_0>=48 && LA8_0<=91)) ) {
                alt8=1;
            }
            else if ( (LA8_0==RULE_STRING) ) {
                alt8=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // InternalGaml.g:578:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getHeadlessExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_17);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getHeadlessExperimentRule());
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
                    // InternalGaml.g:594:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getHeadlessExperimentAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getHeadlessExperimentRule());
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

            // InternalGaml.g:611:3: (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==22) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // InternalGaml.g:612:4: otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) )
                    {
                    otherlv_2=(Token)match(input,22,FOLLOW_10); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getHeadlessExperimentAccess().getModelKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:616:4: ( (lv_importURI_3_0= RULE_STRING ) )
                    // InternalGaml.g:617:5: (lv_importURI_3_0= RULE_STRING )
                    {
                    // InternalGaml.g:617:5: (lv_importURI_3_0= RULE_STRING )
                    // InternalGaml.g:618:6: lv_importURI_3_0= RULE_STRING
                    {
                    lv_importURI_3_0=(Token)match(input,RULE_STRING,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_importURI_3_0, grammarAccess.getHeadlessExperimentAccess().getImportURISTRINGTerminalRuleCall_2_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getHeadlessExperimentRule());
                      						}
                      						setWithLastConsumed(
                      							current,
                      							"importURI",
                      							lv_importURI_3_0,
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
              			newCompositeNode(grammarAccess.getHeadlessExperimentAccess().getFacetsAndBlockParserRuleCall_3());
              		
            }
            pushFollow(FOLLOW_2);
            this_FacetsAndBlock_4=ruleFacetsAndBlock(current);

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_FacetsAndBlock_4;
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
    // InternalGaml.g:651:1: ruleFacetsAndBlock[EObject in_current] returns [EObject current=in_current] : ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) ;
    public final EObject ruleFacetsAndBlock(EObject in_current) throws RecognitionException {
        EObject current = in_current;

        Token otherlv_2=null;
        EObject lv_facets_0_0 = null;

        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:657:2: ( ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) )
            // InternalGaml.g:658:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            {
            // InternalGaml.g:658:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            // InternalGaml.g:659:3: ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            {
            // InternalGaml.g:659:3: ( (lv_facets_0_0= ruleFacet ) )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==RULE_ID||LA10_0==15||LA10_0==33||(LA10_0>=99 && LA10_0<=126)) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // InternalGaml.g:660:4: (lv_facets_0_0= ruleFacet )
            	    {
            	    // InternalGaml.g:660:4: (lv_facets_0_0= ruleFacet )
            	    // InternalGaml.g:661:5: lv_facets_0_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getFacetsAndBlockAccess().getFacetsFacetParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
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
            	    break loop10;
                }
            } while (true);

            // InternalGaml.g:678:3: ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==39) ) {
                alt11=1;
            }
            else if ( (LA11_0==23) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // InternalGaml.g:679:4: ( (lv_block_1_0= ruleBlock ) )
                    {
                    // InternalGaml.g:679:4: ( (lv_block_1_0= ruleBlock ) )
                    // InternalGaml.g:680:5: (lv_block_1_0= ruleBlock )
                    {
                    // InternalGaml.g:680:5: (lv_block_1_0= ruleBlock )
                    // InternalGaml.g:681:6: lv_block_1_0= ruleBlock
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
                    // InternalGaml.g:699:4: otherlv_2= ';'
                    {
                    otherlv_2=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:708:1: entryRuleS_Section returns [EObject current=null] : iv_ruleS_Section= ruleS_Section EOF ;
    public final EObject entryRuleS_Section() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Section = null;


        try {
            // InternalGaml.g:708:50: (iv_ruleS_Section= ruleS_Section EOF )
            // InternalGaml.g:709:2: iv_ruleS_Section= ruleS_Section EOF
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
    // InternalGaml.g:715:1: ruleS_Section returns [EObject current=null] : (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) ;
    public final EObject ruleS_Section() throws RecognitionException {
        EObject current = null;

        EObject this_S_Global_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Experiment_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:721:2: ( (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) )
            // InternalGaml.g:722:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            {
            // InternalGaml.g:722:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            int alt12=3;
            switch ( input.LA(1) ) {
            case 24:
                {
                alt12=1;
                }
                break;
            case 48:
            case 49:
                {
                alt12=2;
                }
                break;
            case 50:
                {
                alt12=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // InternalGaml.g:723:3: this_S_Global_0= ruleS_Global
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
                    // InternalGaml.g:732:3: this_S_Species_1= ruleS_Species
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
                    // InternalGaml.g:741:3: this_S_Experiment_2= ruleS_Experiment
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
    // InternalGaml.g:753:1: entryRuleS_Global returns [EObject current=null] : iv_ruleS_Global= ruleS_Global EOF ;
    public final EObject entryRuleS_Global() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Global = null;


        try {
            // InternalGaml.g:753:49: (iv_ruleS_Global= ruleS_Global EOF )
            // InternalGaml.g:754:2: iv_ruleS_Global= ruleS_Global EOF
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
    // InternalGaml.g:760:1: ruleS_Global returns [EObject current=null] : ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Global() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:766:2: ( ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:767:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:767:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:768:3: ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:768:3: ( (lv_key_0_0= 'global' ) )
            // InternalGaml.g:769:4: (lv_key_0_0= 'global' )
            {
            // InternalGaml.g:769:4: (lv_key_0_0= 'global' )
            // InternalGaml.g:770:5: lv_key_0_0= 'global'
            {
            lv_key_0_0=(Token)match(input,24,FOLLOW_17); if (state.failed) return current;
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
    // InternalGaml.g:797:1: entryRuleS_Species returns [EObject current=null] : iv_ruleS_Species= ruleS_Species EOF ;
    public final EObject entryRuleS_Species() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Species = null;


        try {
            // InternalGaml.g:797:50: (iv_ruleS_Species= ruleS_Species EOF )
            // InternalGaml.g:798:2: iv_ruleS_Species= ruleS_Species EOF
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
    // InternalGaml.g:804:1: ruleS_Species returns [EObject current=null] : ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Species() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:810:2: ( ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:811:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:811:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:812:3: ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:812:3: ( (lv_key_0_0= rule_SpeciesKey ) )
            // InternalGaml.g:813:4: (lv_key_0_0= rule_SpeciesKey )
            {
            // InternalGaml.g:813:4: (lv_key_0_0= rule_SpeciesKey )
            // InternalGaml.g:814:5: lv_key_0_0= rule_SpeciesKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesAccess().getKey_SpeciesKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_12);
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

            // InternalGaml.g:831:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:832:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:832:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:833:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_name_1_0, grammarAccess.getS_SpeciesAccess().getNameIDTerminalRuleCall_1_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_SpeciesRule());
              					}
              					setWithLastConsumed(
              						current,
              						"name",
              						lv_name_1_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_SpeciesRule());
              			}
              			newCompositeNode(grammarAccess.getS_SpeciesAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_Species"


    // $ANTLR start "entryRuleS_Experiment"
    // InternalGaml.g:864:1: entryRuleS_Experiment returns [EObject current=null] : iv_ruleS_Experiment= ruleS_Experiment EOF ;
    public final EObject entryRuleS_Experiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Experiment = null;


        try {
            // InternalGaml.g:864:53: (iv_ruleS_Experiment= ruleS_Experiment EOF )
            // InternalGaml.g:865:2: iv_ruleS_Experiment= ruleS_Experiment EOF
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
    // InternalGaml.g:871:1: ruleS_Experiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Experiment() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:877:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:878:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:878:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:879:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:879:3: ( (lv_key_0_0= rule_ExperimentKey ) )
            // InternalGaml.g:880:4: (lv_key_0_0= rule_ExperimentKey )
            {
            // InternalGaml.g:880:4: (lv_key_0_0= rule_ExperimentKey )
            // InternalGaml.g:881:5: lv_key_0_0= rule_ExperimentKey
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

            // InternalGaml.g:898:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:899:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:899:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:900:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:900:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_ID||(LA13_0>=48 && LA13_0<=91)) ) {
                alt13=1;
            }
            else if ( (LA13_0==RULE_STRING) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // InternalGaml.g:901:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_17);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ExperimentRule());
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
                    // InternalGaml.g:917:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getS_ExperimentAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_ExperimentRule());
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

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_ExperimentRule());
              			}
              			newCompositeNode(grammarAccess.getS_ExperimentAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_Experiment"


    // $ANTLR start "entryRuleStatement"
    // InternalGaml.g:949:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalGaml.g:949:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalGaml.g:950:2: iv_ruleStatement= ruleStatement EOF
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
    // InternalGaml.g:956:1: ruleStatement returns [EObject current=null] : (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_12= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition ) | this_S_Other_15= ruleS_Other ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_S_Display_0 = null;

        EObject this_S_Return_1 = null;

        EObject this_S_Solve_2 = null;

        EObject this_S_If_3 = null;

        EObject this_S_Try_4 = null;

        EObject this_S_Do_5 = null;

        EObject this_S_Loop_6 = null;

        EObject this_S_Equations_7 = null;

        EObject this_S_Action_8 = null;

        EObject this_S_Species_9 = null;

        EObject this_S_Reflex_10 = null;

        EObject this_S_Assignment_11 = null;

        EObject this_S_General_12 = null;

        EObject this_S_Declaration_13 = null;

        EObject this_S_Definition_14 = null;

        EObject this_S_Other_15 = null;



        	enterRule();

        try {
            // InternalGaml.g:962:2: ( (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_12= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition ) | this_S_Other_15= ruleS_Other ) )
            // InternalGaml.g:963:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_12= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition ) | this_S_Other_15= ruleS_Other )
            {
            // InternalGaml.g:963:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_12= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition ) | this_S_Other_15= ruleS_Other )
            int alt14=16;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // InternalGaml.g:964:3: this_S_Display_0= ruleS_Display
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
                    // InternalGaml.g:973:3: this_S_Return_1= ruleS_Return
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
                    // InternalGaml.g:982:3: this_S_Solve_2= ruleS_Solve
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
                    // InternalGaml.g:991:3: this_S_If_3= ruleS_If
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
                    // InternalGaml.g:1000:3: this_S_Try_4= ruleS_Try
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
                    // InternalGaml.g:1009:3: this_S_Do_5= ruleS_Do
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_DoParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Do_5=ruleS_Do();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Do_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:1018:3: this_S_Loop_6= ruleS_Loop
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_LoopParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Loop_6=ruleS_Loop();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Loop_6;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:1027:3: ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations )
                    {
                    // InternalGaml.g:1027:3: ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations )
                    // InternalGaml.g:1028:4: ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_EquationsParserRuleCall_7());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Equations_7=ruleS_Equations();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Equations_7;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalGaml.g:1039:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    {
                    // InternalGaml.g:1039:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    // InternalGaml.g:1040:4: ( ruleS_Action )=>this_S_Action_8= ruleS_Action
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
                    // InternalGaml.g:1051:3: ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species )
                    {
                    // InternalGaml.g:1051:3: ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species )
                    // InternalGaml.g:1052:4: ( ruleS_Species )=>this_S_Species_9= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_SpeciesParserRuleCall_9());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Species_9=ruleS_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Species_9;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalGaml.g:1063:3: ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex )
                    {
                    // InternalGaml.g:1063:3: ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex )
                    // InternalGaml.g:1064:4: ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_ReflexParserRuleCall_10());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Reflex_10=ruleS_Reflex();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Reflex_10;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 12 :
                    // InternalGaml.g:1075:3: ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment )
                    {
                    // InternalGaml.g:1075:3: ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment )
                    // InternalGaml.g:1076:4: ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_AssignmentParserRuleCall_11());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Assignment_11=ruleS_Assignment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Assignment_11;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 13 :
                    // InternalGaml.g:1087:3: ( ( ruleS_General )=>this_S_General_12= ruleS_General )
                    {
                    // InternalGaml.g:1087:3: ( ( ruleS_General )=>this_S_General_12= ruleS_General )
                    // InternalGaml.g:1088:4: ( ruleS_General )=>this_S_General_12= ruleS_General
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_GeneralParserRuleCall_12());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_General_12=ruleS_General();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_General_12;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 14 :
                    // InternalGaml.g:1099:3: ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration )
                    {
                    // InternalGaml.g:1099:3: ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration )
                    // InternalGaml.g:1100:4: ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DeclarationParserRuleCall_13());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Declaration_13=ruleS_Declaration();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Declaration_13;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 15 :
                    // InternalGaml.g:1111:3: ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition )
                    {
                    // InternalGaml.g:1111:3: ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition )
                    // InternalGaml.g:1112:4: ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DefinitionParserRuleCall_14());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_14=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Definition_14;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 16 :
                    // InternalGaml.g:1123:3: this_S_Other_15= ruleS_Other
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_OtherParserRuleCall_15());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Other_15=ruleS_Other();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Other_15;
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
    // InternalGaml.g:1135:1: entryRuleS_General returns [EObject current=null] : iv_ruleS_General= ruleS_General EOF ;
    public final EObject entryRuleS_General() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_General = null;


        try {
            // InternalGaml.g:1135:50: (iv_ruleS_General= ruleS_General EOF )
            // InternalGaml.g:1136:2: iv_ruleS_General= ruleS_General EOF
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
    // InternalGaml.g:1142:1: ruleS_General returns [EObject current=null] : ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_General() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1148:2: ( ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1149:2: ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1149:2: ( ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1150:3: ( (lv_key_0_0= rule_GeneralKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1150:3: ( (lv_key_0_0= rule_GeneralKey ) )
            // InternalGaml.g:1151:4: (lv_key_0_0= rule_GeneralKey )
            {
            // InternalGaml.g:1151:4: (lv_key_0_0= rule_GeneralKey )
            // InternalGaml.g:1152:5: lv_key_0_0= rule_GeneralKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GeneralAccess().getKey_GeneralKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
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

            // InternalGaml.g:1169:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1170:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1170:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1171:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GeneralAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_GeneralRule());
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
              				current = createModelElement(grammarAccess.getS_GeneralRule());
              			}
              			newCompositeNode(grammarAccess.getS_GeneralAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_General"


    // $ANTLR start "entryRuleS_Do"
    // InternalGaml.g:1203:1: entryRuleS_Do returns [EObject current=null] : iv_ruleS_Do= ruleS_Do EOF ;
    public final EObject entryRuleS_Do() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Do = null;


        try {
            // InternalGaml.g:1203:45: (iv_ruleS_Do= ruleS_Do EOF )
            // InternalGaml.g:1204:2: iv_ruleS_Do= ruleS_Do EOF
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
    // InternalGaml.g:1210:1: ruleS_Do returns [EObject current=null] : ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Do() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_1=null;
        Token lv_key_0_2=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1216:2: ( ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1217:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1217:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1218:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1218:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) )
            // InternalGaml.g:1219:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            {
            // InternalGaml.g:1219:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            // InternalGaml.g:1220:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            {
            // InternalGaml.g:1220:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==25) ) {
                alt15=1;
            }
            else if ( (LA15_0==26) ) {
                alt15=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // InternalGaml.g:1221:6: lv_key_0_1= 'do'
                    {
                    lv_key_0_1=(Token)match(input,25,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_1, grammarAccess.getS_DoAccess().getKeyDoKeyword_0_0_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DoRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_1, null);
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:1232:6: lv_key_0_2= 'invoke'
                    {
                    lv_key_0_2=(Token)match(input,26,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_2, grammarAccess.getS_DoAccess().getKeyInvokeKeyword_0_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DoRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_2, null);
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:1245:3: ( (lv_expr_1_0= ruleAbstractRef ) )
            // InternalGaml.g:1246:4: (lv_expr_1_0= ruleAbstractRef )
            {
            // InternalGaml.g:1246:4: (lv_expr_1_0= ruleAbstractRef )
            // InternalGaml.g:1247:5: lv_expr_1_0= ruleAbstractRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DoAccess().getExprAbstractRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
            lv_expr_1_0=ruleAbstractRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DoRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.AbstractRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_DoRule());
              			}
              			newCompositeNode(grammarAccess.getS_DoAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_Do"


    // $ANTLR start "entryRuleS_Loop"
    // InternalGaml.g:1279:1: entryRuleS_Loop returns [EObject current=null] : iv_ruleS_Loop= ruleS_Loop EOF ;
    public final EObject entryRuleS_Loop() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Loop = null;


        try {
            // InternalGaml.g:1279:47: (iv_ruleS_Loop= ruleS_Loop EOF )
            // InternalGaml.g:1280:2: iv_ruleS_Loop= ruleS_Loop EOF
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
    // InternalGaml.g:1286:1: ruleS_Loop returns [EObject current=null] : ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Loop() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_0=null;
        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1292:2: ( ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) )
            // InternalGaml.g:1293:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1293:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            // InternalGaml.g:1294:3: ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) )
            {
            // InternalGaml.g:1294:3: ( (lv_key_0_0= 'loop' ) )
            // InternalGaml.g:1295:4: (lv_key_0_0= 'loop' )
            {
            // InternalGaml.g:1295:4: (lv_key_0_0= 'loop' )
            // InternalGaml.g:1296:5: lv_key_0_0= 'loop'
            {
            lv_key_0_0=(Token)match(input,27,FOLLOW_19); if (state.failed) return current;
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

            // InternalGaml.g:1308:3: ( (lv_name_1_0= RULE_ID ) )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==RULE_ID) ) {
                int LA16_1 = input.LA(2);

                if ( (LA16_1==RULE_ID||LA16_1==15||LA16_1==33||LA16_1==39||(LA16_1>=99 && LA16_1<=126)) ) {
                    alt16=1;
                }
            }
            switch (alt16) {
                case 1 :
                    // InternalGaml.g:1309:4: (lv_name_1_0= RULE_ID )
                    {
                    // InternalGaml.g:1309:4: (lv_name_1_0= RULE_ID )
                    // InternalGaml.g:1310:5: lv_name_1_0= RULE_ID
                    {
                    lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_19); if (state.failed) return current;
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

            // InternalGaml.g:1326:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==RULE_ID||LA17_0==15||LA17_0==33||(LA17_0>=99 && LA17_0<=126)) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // InternalGaml.g:1327:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1327:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:1328:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_LoopAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
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
            	    break loop17;
                }
            } while (true);

            // InternalGaml.g:1345:3: ( (lv_block_3_0= ruleBlock ) )
            // InternalGaml.g:1346:4: (lv_block_3_0= ruleBlock )
            {
            // InternalGaml.g:1346:4: (lv_block_3_0= ruleBlock )
            // InternalGaml.g:1347:5: lv_block_3_0= ruleBlock
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
    // InternalGaml.g:1368:1: entryRuleS_If returns [EObject current=null] : iv_ruleS_If= ruleS_If EOF ;
    public final EObject entryRuleS_If() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_If = null;


        try {
            // InternalGaml.g:1368:45: (iv_ruleS_If= ruleS_If EOF )
            // InternalGaml.g:1369:2: iv_ruleS_If= ruleS_If EOF
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
    // InternalGaml.g:1375:1: ruleS_If returns [EObject current=null] : ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) ;
    public final EObject ruleS_If() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_3=null;
        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;

        EObject lv_else_4_1 = null;

        EObject lv_else_4_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1381:2: ( ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) )
            // InternalGaml.g:1382:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            {
            // InternalGaml.g:1382:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            // InternalGaml.g:1383:3: ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            {
            // InternalGaml.g:1383:3: ( (lv_key_0_0= 'if' ) )
            // InternalGaml.g:1384:4: (lv_key_0_0= 'if' )
            {
            // InternalGaml.g:1384:4: (lv_key_0_0= 'if' )
            // InternalGaml.g:1385:5: lv_key_0_0= 'if'
            {
            lv_key_0_0=(Token)match(input,28,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:1397:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1398:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1398:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1399:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_IfAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_3);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_IfRule());
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

            // InternalGaml.g:1416:3: ( (lv_block_2_0= ruleBlock ) )
            // InternalGaml.g:1417:4: (lv_block_2_0= ruleBlock )
            {
            // InternalGaml.g:1417:4: (lv_block_2_0= ruleBlock )
            // InternalGaml.g:1418:5: lv_block_2_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_IfAccess().getBlockBlockParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_20);
            lv_block_2_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_IfRule());
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

            // InternalGaml.g:1435:3: ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==29) && (synpred10_InternalGaml())) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // InternalGaml.g:1436:4: ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    {
                    // InternalGaml.g:1436:4: ( ( 'else' )=>otherlv_3= 'else' )
                    // InternalGaml.g:1437:5: ( 'else' )=>otherlv_3= 'else'
                    {
                    otherlv_3=(Token)match(input,29,FOLLOW_21); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_IfAccess().getElseKeyword_3_0());
                      				
                    }

                    }

                    // InternalGaml.g:1443:4: ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    // InternalGaml.g:1444:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    {
                    // InternalGaml.g:1444:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    // InternalGaml.g:1445:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    {
                    // InternalGaml.g:1445:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==28) ) {
                        alt18=1;
                    }
                    else if ( (LA18_0==39) ) {
                        alt18=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 0, input);

                        throw nvae;
                    }
                    switch (alt18) {
                        case 1 :
                            // InternalGaml.g:1446:7: lv_else_4_1= ruleS_If
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getS_IfAccess().getElseS_IfParserRuleCall_3_1_0_0());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_else_4_1=ruleS_If();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getS_IfRule());
                              							}
                              							set(
                              								current,
                              								"else",
                              								lv_else_4_1,
                              								"gaml.compiler.Gaml.S_If");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }
                            break;
                        case 2 :
                            // InternalGaml.g:1462:7: lv_else_4_2= ruleBlock
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getS_IfAccess().getElseBlockParserRuleCall_3_1_0_1());
                              						
                            }
                            pushFollow(FOLLOW_2);
                            lv_else_4_2=ruleBlock();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getS_IfRule());
                              							}
                              							set(
                              								current,
                              								"else",
                              								lv_else_4_2,
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
    // InternalGaml.g:1485:1: entryRuleS_Try returns [EObject current=null] : iv_ruleS_Try= ruleS_Try EOF ;
    public final EObject entryRuleS_Try() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Try = null;


        try {
            // InternalGaml.g:1485:46: (iv_ruleS_Try= ruleS_Try EOF )
            // InternalGaml.g:1486:2: iv_ruleS_Try= ruleS_Try EOF
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
    // InternalGaml.g:1492:1: ruleS_Try returns [EObject current=null] : ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) ;
    public final EObject ruleS_Try() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_block_1_0 = null;

        EObject lv_catch_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1498:2: ( ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) )
            // InternalGaml.g:1499:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            {
            // InternalGaml.g:1499:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            // InternalGaml.g:1500:3: ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            {
            // InternalGaml.g:1500:3: ( (lv_key_0_0= 'try' ) )
            // InternalGaml.g:1501:4: (lv_key_0_0= 'try' )
            {
            // InternalGaml.g:1501:4: (lv_key_0_0= 'try' )
            // InternalGaml.g:1502:5: lv_key_0_0= 'try'
            {
            lv_key_0_0=(Token)match(input,30,FOLLOW_3); if (state.failed) return current;
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

            // InternalGaml.g:1514:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:1515:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:1515:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:1516:5: lv_block_1_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_TryAccess().getBlockBlockParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_22);
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

            // InternalGaml.g:1533:3: ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==31) && (synpred11_InternalGaml())) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalGaml.g:1534:4: ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) )
                    {
                    // InternalGaml.g:1534:4: ( ( 'catch' )=>otherlv_2= 'catch' )
                    // InternalGaml.g:1535:5: ( 'catch' )=>otherlv_2= 'catch'
                    {
                    otherlv_2=(Token)match(input,31,FOLLOW_3); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getS_TryAccess().getCatchKeyword_2_0());
                      				
                    }

                    }

                    // InternalGaml.g:1541:4: ( (lv_catch_3_0= ruleBlock ) )
                    // InternalGaml.g:1542:5: (lv_catch_3_0= ruleBlock )
                    {
                    // InternalGaml.g:1542:5: (lv_catch_3_0= ruleBlock )
                    // InternalGaml.g:1543:6: lv_catch_3_0= ruleBlock
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


    // $ANTLR start "entryRuleS_Return"
    // InternalGaml.g:1565:1: entryRuleS_Return returns [EObject current=null] : iv_ruleS_Return= ruleS_Return EOF ;
    public final EObject entryRuleS_Return() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Return = null;


        try {
            // InternalGaml.g:1565:49: (iv_ruleS_Return= ruleS_Return EOF )
            // InternalGaml.g:1566:2: iv_ruleS_Return= ruleS_Return EOF
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
    // InternalGaml.g:1572:1: ruleS_Return returns [EObject current=null] : ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) ;
    public final EObject ruleS_Return() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1578:2: ( ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) )
            // InternalGaml.g:1579:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            {
            // InternalGaml.g:1579:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            // InternalGaml.g:1580:3: ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';'
            {
            // InternalGaml.g:1580:3: ( (lv_key_0_0= 'return' ) )
            // InternalGaml.g:1581:4: (lv_key_0_0= 'return' )
            {
            // InternalGaml.g:1581:4: (lv_key_0_0= 'return' )
            // InternalGaml.g:1582:5: lv_key_0_0= 'return'
            {
            lv_key_0_0=(Token)match(input,32,FOLLOW_23); if (state.failed) return current;
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

            // InternalGaml.g:1594:3: ( (lv_expr_1_0= ruleExpression ) )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( ((LA21_0>=RULE_ID && LA21_0<=RULE_KEYWORD)||LA21_0==20||LA21_0==35||LA21_0==39||(LA21_0>=48 && LA21_0<=91)||LA21_0==136||(LA21_0>=140 && LA21_0<=142)) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // InternalGaml.g:1595:4: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:1595:4: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:1596:5: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReturnAccess().getExprExpressionParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_24);
                    lv_expr_1_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getS_ReturnRule());
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
                    break;

            }

            otherlv_2=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getS_ReturnAccess().getSemicolonKeyword_2());
              		
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


    // $ANTLR start "entryRuleS_Other"
    // InternalGaml.g:1621:1: entryRuleS_Other returns [EObject current=null] : iv_ruleS_Other= ruleS_Other EOF ;
    public final EObject entryRuleS_Other() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Other = null;


        try {
            // InternalGaml.g:1621:48: (iv_ruleS_Other= ruleS_Other EOF )
            // InternalGaml.g:1622:2: iv_ruleS_Other= ruleS_Other EOF
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
    // InternalGaml.g:1628:1: ruleS_Other returns [EObject current=null] : ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Other() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:1634:2: ( ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1635:2: ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1635:2: ( ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1636:3: ( (lv_key_0_0= ruleValid_ID ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1636:3: ( (lv_key_0_0= ruleValid_ID ) )
            // InternalGaml.g:1637:4: (lv_key_0_0= ruleValid_ID )
            {
            // InternalGaml.g:1637:4: (lv_key_0_0= ruleValid_ID )
            // InternalGaml.g:1638:5: lv_key_0_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherAccess().getKeyValid_IDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_17);
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


    // $ANTLR start "entryRuleS_Declaration"
    // InternalGaml.g:1670:1: entryRuleS_Declaration returns [EObject current=null] : iv_ruleS_Declaration= ruleS_Declaration EOF ;
    public final EObject entryRuleS_Declaration() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Declaration = null;


        try {
            // InternalGaml.g:1670:54: (iv_ruleS_Declaration= ruleS_Declaration EOF )
            // InternalGaml.g:1671:2: iv_ruleS_Declaration= ruleS_Declaration EOF
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
    // InternalGaml.g:1677:1: ruleS_Declaration returns [EObject current=null] : ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ;
    public final EObject ruleS_Declaration() throws RecognitionException {
        EObject current = null;

        EObject this_S_Definition_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Reflex_2 = null;

        EObject this_S_Action_3 = null;

        EObject this_S_Loop_4 = null;



        	enterRule();

        try {
            // InternalGaml.g:1683:2: ( ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) )
            // InternalGaml.g:1684:2: ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop )
            {
            // InternalGaml.g:1684:2: ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop )
            int alt22=5;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==RULE_ID) && (synpred12_InternalGaml())) {
                alt22=1;
            }
            else if ( (LA22_0==48) ) {
                int LA22_2 = input.LA(2);

                if ( (LA22_2==134) && (synpred12_InternalGaml())) {
                    alt22=1;
                }
                else if ( (LA22_2==RULE_ID) ) {
                    alt22=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA22_0==49) ) {
                alt22=2;
            }
            else if ( ((LA22_0>=89 && LA22_0<=91)) ) {
                alt22=3;
            }
            else if ( (LA22_0==37) ) {
                alt22=4;
            }
            else if ( (LA22_0==27) ) {
                alt22=5;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // InternalGaml.g:1685:3: ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                    {
                    // InternalGaml.g:1685:3: ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                    // InternalGaml.g:1686:4: ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition
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
                    // InternalGaml.g:1697:3: this_S_Species_1= ruleS_Species
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
                    // InternalGaml.g:1706:3: this_S_Reflex_2= ruleS_Reflex
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
                    // InternalGaml.g:1715:3: this_S_Action_3= ruleS_Action
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
                    // InternalGaml.g:1724:3: this_S_Loop_4= ruleS_Loop
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getS_DeclarationAccess().getS_LoopParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Loop_4=ruleS_Loop();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Loop_4;
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
    // InternalGaml.g:1736:1: entryRuleS_Reflex returns [EObject current=null] : iv_ruleS_Reflex= ruleS_Reflex EOF ;
    public final EObject entryRuleS_Reflex() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Reflex = null;


        try {
            // InternalGaml.g:1736:49: (iv_ruleS_Reflex= ruleS_Reflex EOF )
            // InternalGaml.g:1737:2: iv_ruleS_Reflex= ruleS_Reflex EOF
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
    // InternalGaml.g:1743:1: ruleS_Reflex returns [EObject current=null] : ( ( (lv_key_0_0= ruleK_Reflex ) ) ( (lv_name_1_0= ruleValid_ID ) )? (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )? ( (lv_block_5_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Reflex() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_expr_4_0 = null;

        EObject lv_block_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1749:2: ( ( ( (lv_key_0_0= ruleK_Reflex ) ) ( (lv_name_1_0= ruleValid_ID ) )? (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )? ( (lv_block_5_0= ruleBlock ) ) ) )
            // InternalGaml.g:1750:2: ( ( (lv_key_0_0= ruleK_Reflex ) ) ( (lv_name_1_0= ruleValid_ID ) )? (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )? ( (lv_block_5_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1750:2: ( ( (lv_key_0_0= ruleK_Reflex ) ) ( (lv_name_1_0= ruleValid_ID ) )? (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )? ( (lv_block_5_0= ruleBlock ) ) )
            // InternalGaml.g:1751:3: ( (lv_key_0_0= ruleK_Reflex ) ) ( (lv_name_1_0= ruleValid_ID ) )? (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )? ( (lv_block_5_0= ruleBlock ) )
            {
            // InternalGaml.g:1751:3: ( (lv_key_0_0= ruleK_Reflex ) )
            // InternalGaml.g:1752:4: (lv_key_0_0= ruleK_Reflex )
            {
            // InternalGaml.g:1752:4: (lv_key_0_0= ruleK_Reflex )
            // InternalGaml.g:1753:5: lv_key_0_0= ruleK_Reflex
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ReflexAccess().getKeyK_ReflexParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_25);
            lv_key_0_0=ruleK_Reflex();

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
              						"gaml.compiler.Gaml.K_Reflex");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:1770:3: ( (lv_name_1_0= ruleValid_ID ) )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==RULE_ID||(LA23_0>=48 && LA23_0<=91)) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // InternalGaml.g:1771:4: (lv_name_1_0= ruleValid_ID )
                    {
                    // InternalGaml.g:1771:4: (lv_name_1_0= ruleValid_ID )
                    // InternalGaml.g:1772:5: lv_name_1_0= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReflexAccess().getNameValid_IDParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_26);
                    lv_name_1_0=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getS_ReflexRule());
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
                    break;

            }

            // InternalGaml.g:1789:3: (otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) ) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==33) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalGaml.g:1790:4: otherlv_2= 'when' otherlv_3= ':' ( (lv_expr_4_0= ruleExpression ) )
                    {
                    otherlv_2=(Token)match(input,33,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getS_ReflexAccess().getWhenKeyword_2_0());
                      			
                    }
                    otherlv_3=(Token)match(input,34,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_ReflexAccess().getColonKeyword_2_1());
                      			
                    }
                    // InternalGaml.g:1798:4: ( (lv_expr_4_0= ruleExpression ) )
                    // InternalGaml.g:1799:5: (lv_expr_4_0= ruleExpression )
                    {
                    // InternalGaml.g:1799:5: (lv_expr_4_0= ruleExpression )
                    // InternalGaml.g:1800:6: lv_expr_4_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getExprExpressionParserRuleCall_2_2_0());
                      					
                    }
                    pushFollow(FOLLOW_3);
                    lv_expr_4_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"expr",
                      							lv_expr_4_0,
                      							"gaml.compiler.Gaml.Expression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalGaml.g:1818:3: ( (lv_block_5_0= ruleBlock ) )
            // InternalGaml.g:1819:4: (lv_block_5_0= ruleBlock )
            {
            // InternalGaml.g:1819:4: (lv_block_5_0= ruleBlock )
            // InternalGaml.g:1820:5: lv_block_5_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ReflexAccess().getBlockBlockParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_5_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ReflexRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_5_0,
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
    // InternalGaml.g:1841:1: entryRuleS_Definition returns [EObject current=null] : iv_ruleS_Definition= ruleS_Definition EOF ;
    public final EObject entryRuleS_Definition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Definition = null;


        try {
            // InternalGaml.g:1841:53: (iv_ruleS_Definition= ruleS_Definition EOF )
            // InternalGaml.g:1842:2: iv_ruleS_Definition= ruleS_Definition EOF
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
    // InternalGaml.g:1848:1: ruleS_Definition returns [EObject current=null] : ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Definition() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_tkey_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject lv_args_3_0 = null;

        EObject this_FacetsAndBlock_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:1854:2: ( ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1855:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1855:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1856:3: ( (lv_tkey_0_0= ruleTypeRef ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1856:3: ( (lv_tkey_0_0= ruleTypeRef ) )
            // InternalGaml.g:1857:4: (lv_tkey_0_0= ruleTypeRef )
            {
            // InternalGaml.g:1857:4: (lv_tkey_0_0= ruleTypeRef )
            // InternalGaml.g:1858:5: lv_tkey_0_0= ruleTypeRef
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

            // InternalGaml.g:1875:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:1876:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:1876:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:1877:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:1877:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==RULE_ID||(LA25_0>=48 && LA25_0<=91)) ) {
                alt25=1;
            }
            else if ( (LA25_0==RULE_STRING) ) {
                alt25=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // InternalGaml.g:1878:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DefinitionAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_28);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
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
                    // InternalGaml.g:1894:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_28); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getS_DefinitionAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DefinitionRule());
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

            // InternalGaml.g:1911:3: (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==35) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // InternalGaml.g:1912:4: otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,35,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getS_DefinitionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:1916:4: ( (lv_args_3_0= ruleActionArguments ) )
                    // InternalGaml.g:1917:5: (lv_args_3_0= ruleActionArguments )
                    {
                    // InternalGaml.g:1917:5: (lv_args_3_0= ruleActionArguments )
                    // InternalGaml.g:1918:6: lv_args_3_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DefinitionAccess().getArgsActionArgumentsParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_30);
                    lv_args_3_0=ruleActionArguments();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
                      						}
                      						set(
                      							current,
                      							"args",
                      							lv_args_3_0,
                      							"gaml.compiler.Gaml.ActionArguments");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,36,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getS_DefinitionAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_DefinitionRule());
              			}
              			newCompositeNode(grammarAccess.getS_DefinitionAccess().getFacetsAndBlockParserRuleCall_3());
              		
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
    // $ANTLR end "ruleS_Definition"


    // $ANTLR start "entryRuleS_Action"
    // InternalGaml.g:1955:1: entryRuleS_Action returns [EObject current=null] : iv_ruleS_Action= ruleS_Action EOF ;
    public final EObject entryRuleS_Action() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Action = null;


        try {
            // InternalGaml.g:1955:49: (iv_ruleS_Action= ruleS_Action EOF )
            // InternalGaml.g:1956:2: iv_ruleS_Action= ruleS_Action EOF
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
    // InternalGaml.g:1962:1: ruleS_Action returns [EObject current=null] : ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Action() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject lv_args_4_0 = null;

        EObject this_FacetsAndBlock_6 = null;



        	enterRule();

        try {
            // InternalGaml.g:1968:2: ( ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1969:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1969:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1970:3: () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1970:3: ()
            // InternalGaml.g:1971:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getS_ActionAccess().getS_ActionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:1977:3: ( (lv_key_1_0= 'action' ) )
            // InternalGaml.g:1978:4: (lv_key_1_0= 'action' )
            {
            // InternalGaml.g:1978:4: (lv_key_1_0= 'action' )
            // InternalGaml.g:1979:5: lv_key_1_0= 'action'
            {
            lv_key_1_0=(Token)match(input,37,FOLLOW_7); if (state.failed) return current;
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

            // InternalGaml.g:1991:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:1992:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:1992:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:1993:5: lv_name_2_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ActionAccess().getNameValid_IDParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_28);
            lv_name_2_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_ActionRule());
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

            // InternalGaml.g:2010:3: (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==35) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalGaml.g:2011:4: otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')'
                    {
                    otherlv_3=(Token)match(input,35,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_ActionAccess().getLeftParenthesisKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:2015:4: ( (lv_args_4_0= ruleActionArguments ) )
                    // InternalGaml.g:2016:5: (lv_args_4_0= ruleActionArguments )
                    {
                    // InternalGaml.g:2016:5: (lv_args_4_0= ruleActionArguments )
                    // InternalGaml.g:2017:6: lv_args_4_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ActionAccess().getArgsActionArgumentsParserRuleCall_3_1_0());
                      					
                    }
                    pushFollow(FOLLOW_30);
                    lv_args_4_0=ruleActionArguments();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ActionRule());
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

                    otherlv_5=(Token)match(input,36,FOLLOW_17); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getS_ActionAccess().getRightParenthesisKeyword_3_2());
                      			
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_ActionRule());
              			}
              			newCompositeNode(grammarAccess.getS_ActionAccess().getFacetsAndBlockParserRuleCall_4());
              		
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
    // $ANTLR end "ruleS_Action"


    // $ANTLR start "entryRuleS_Assignment"
    // InternalGaml.g:2054:1: entryRuleS_Assignment returns [EObject current=null] : iv_ruleS_Assignment= ruleS_Assignment EOF ;
    public final EObject entryRuleS_Assignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Assignment = null;


        try {
            // InternalGaml.g:2054:53: (iv_ruleS_Assignment= ruleS_Assignment EOF )
            // InternalGaml.g:2055:2: iv_ruleS_Assignment= ruleS_Assignment EOF
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
    // InternalGaml.g:2061:1: ruleS_Assignment returns [EObject current=null] : ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) ;
    public final EObject ruleS_Assignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_4=null;
        EObject lv_expr_0_0 = null;

        AntlrDatatypeRuleToken lv_key_1_0 = null;

        EObject lv_value_2_0 = null;

        EObject lv_facets_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2067:2: ( ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) )
            // InternalGaml.g:2068:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            {
            // InternalGaml.g:2068:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            // InternalGaml.g:2069:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';'
            {
            // InternalGaml.g:2069:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* )
            // InternalGaml.g:2070:4: ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )*
            {
            // InternalGaml.g:2070:4: ( (lv_expr_0_0= ruleExpression ) )
            // InternalGaml.g:2071:5: (lv_expr_0_0= ruleExpression )
            {
            // InternalGaml.g:2071:5: (lv_expr_0_0= ruleExpression )
            // InternalGaml.g:2072:6: lv_expr_0_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getExprExpressionParserRuleCall_0_0_0());
              					
            }
            pushFollow(FOLLOW_31);
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

            // InternalGaml.g:2089:4: ( (lv_key_1_0= ruleK_Assignment ) )
            // InternalGaml.g:2090:5: (lv_key_1_0= ruleK_Assignment )
            {
            // InternalGaml.g:2090:5: (lv_key_1_0= ruleK_Assignment )
            // InternalGaml.g:2091:6: lv_key_1_0= ruleK_Assignment
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getKeyK_AssignmentParserRuleCall_0_1_0());
              					
            }
            pushFollow(FOLLOW_5);
            lv_key_1_0=ruleK_Assignment();

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
              							"gaml.compiler.Gaml.K_Assignment");
              						afterParserOrEnumRuleCall();
              					
            }

            }


            }

            // InternalGaml.g:2108:4: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2109:5: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2109:5: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2110:6: lv_value_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getValueExpressionParserRuleCall_0_2_0());
              					
            }
            pushFollow(FOLLOW_32);
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

            // InternalGaml.g:2127:4: ( (lv_facets_3_0= ruleFacet ) )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==RULE_ID||LA28_0==15||LA28_0==33||(LA28_0>=99 && LA28_0<=126)) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // InternalGaml.g:2128:5: (lv_facets_3_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2128:5: (lv_facets_3_0= ruleFacet )
            	    // InternalGaml.g:2129:6: lv_facets_3_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getS_AssignmentAccess().getFacetsFacetParserRuleCall_0_3_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_32);
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
            	    break loop28;
                }
            } while (true);


            }

            otherlv_4=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2155:1: entryRuleS_Equations returns [EObject current=null] : iv_ruleS_Equations= ruleS_Equations EOF ;
    public final EObject entryRuleS_Equations() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equations = null;


        try {
            // InternalGaml.g:2155:52: (iv_ruleS_Equations= ruleS_Equations EOF )
            // InternalGaml.g:2156:2: iv_ruleS_Equations= ruleS_Equations EOF
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
    // InternalGaml.g:2162:1: ruleS_Equations returns [EObject current=null] : ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) ;
    public final EObject ruleS_Equations() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_facets_2_0 = null;

        EObject lv_equations_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2168:2: ( ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) )
            // InternalGaml.g:2169:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            {
            // InternalGaml.g:2169:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            // InternalGaml.g:2170:3: ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            {
            // InternalGaml.g:2170:3: ( (lv_key_0_0= 'equation' ) )
            // InternalGaml.g:2171:4: (lv_key_0_0= 'equation' )
            {
            // InternalGaml.g:2171:4: (lv_key_0_0= 'equation' )
            // InternalGaml.g:2172:5: lv_key_0_0= 'equation'
            {
            lv_key_0_0=(Token)match(input,38,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_EquationsAccess().getKeyEquationKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_EquationsRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "equation");
              				
            }

            }


            }

            // InternalGaml.g:2184:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:2185:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:2185:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:2186:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationsAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_18);
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

            // InternalGaml.g:2203:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==RULE_ID||LA29_0==15||LA29_0==33||(LA29_0>=99 && LA29_0<=126)) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // InternalGaml.g:2204:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2204:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2205:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_EquationsAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
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
            	    break loop29;
                }
            } while (true);

            // InternalGaml.g:2222:3: ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==39) ) {
                alt31=1;
            }
            else if ( (LA31_0==23) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // InternalGaml.g:2223:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    {
                    // InternalGaml.g:2223:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    // InternalGaml.g:2224:5: otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}'
                    {
                    otherlv_3=(Token)match(input,39,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0());
                      				
                    }
                    // InternalGaml.g:2228:5: ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )*
                    loop30:
                    do {
                        int alt30=2;
                        int LA30_0 = input.LA(1);

                        if ( (LA30_0==RULE_ID||(LA30_0>=48 && LA30_0<=91)) ) {
                            alt30=1;
                        }


                        switch (alt30) {
                    	case 1 :
                    	    // InternalGaml.g:2229:6: ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';'
                    	    {
                    	    // InternalGaml.g:2229:6: ( (lv_equations_4_0= ruleS_Equation ) )
                    	    // InternalGaml.g:2230:7: (lv_equations_4_0= ruleS_Equation )
                    	    {
                    	    // InternalGaml.g:2230:7: (lv_equations_4_0= ruleS_Equation )
                    	    // InternalGaml.g:2231:8: lv_equations_4_0= ruleS_Equation
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      								newCompositeNode(grammarAccess.getS_EquationsAccess().getEquationsS_EquationParserRuleCall_3_0_1_0_0());
                    	      							
                    	    }
                    	    pushFollow(FOLLOW_24);
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

                    	    otherlv_5=(Token)match(input,23,FOLLOW_33); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						newLeafNode(otherlv_5, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_0_1_1());
                    	      					
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);

                    otherlv_6=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_6, grammarAccess.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2());
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:2259:4: otherlv_7= ';'
                    {
                    otherlv_7=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2268:1: entryRuleS_Equation returns [EObject current=null] : iv_ruleS_Equation= ruleS_Equation EOF ;
    public final EObject entryRuleS_Equation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equation = null;


        try {
            // InternalGaml.g:2268:51: (iv_ruleS_Equation= ruleS_Equation EOF )
            // InternalGaml.g:2269:2: iv_ruleS_Equation= ruleS_Equation EOF
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
    // InternalGaml.g:2275:1: ruleS_Equation returns [EObject current=null] : ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) ;
    public final EObject ruleS_Equation() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        EObject lv_expr_0_1 = null;

        EObject lv_expr_0_2 = null;

        EObject lv_value_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2281:2: ( ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:2282:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:2282:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            // InternalGaml.g:2283:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) )
            {
            // InternalGaml.g:2283:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) )
            // InternalGaml.g:2284:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            {
            // InternalGaml.g:2284:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            // InternalGaml.g:2285:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            {
            // InternalGaml.g:2285:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            int alt32=2;
            alt32 = dfa32.predict(input);
            switch (alt32) {
                case 1 :
                    // InternalGaml.g:2286:6: lv_expr_0_1= ruleFunction
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprFunctionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_34);
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
                    // InternalGaml.g:2302:6: lv_expr_0_2= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprVariableRefParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_34);
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

            // InternalGaml.g:2320:3: ( (lv_key_1_0= '=' ) )
            // InternalGaml.g:2321:4: (lv_key_1_0= '=' )
            {
            // InternalGaml.g:2321:4: (lv_key_1_0= '=' )
            // InternalGaml.g:2322:5: lv_key_1_0= '='
            {
            lv_key_1_0=(Token)match(input,41,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:2334:3: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2335:4: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2335:4: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2336:5: lv_value_2_0= ruleExpression
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
    // InternalGaml.g:2357:1: entryRuleS_Solve returns [EObject current=null] : iv_ruleS_Solve= ruleS_Solve EOF ;
    public final EObject entryRuleS_Solve() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Solve = null;


        try {
            // InternalGaml.g:2357:48: (iv_ruleS_Solve= ruleS_Solve EOF )
            // InternalGaml.g:2358:2: iv_ruleS_Solve= ruleS_Solve EOF
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
    // InternalGaml.g:2364:1: ruleS_Solve returns [EObject current=null] : ( ( (lv_key_0_0= ruleK_Solve ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Solve() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2370:2: ( ( ( (lv_key_0_0= ruleK_Solve ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2371:2: ( ( (lv_key_0_0= ruleK_Solve ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2371:2: ( ( (lv_key_0_0= ruleK_Solve ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2372:3: ( (lv_key_0_0= ruleK_Solve ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2372:3: ( (lv_key_0_0= ruleK_Solve ) )
            // InternalGaml.g:2373:4: (lv_key_0_0= ruleK_Solve )
            {
            // InternalGaml.g:2373:4: (lv_key_0_0= ruleK_Solve )
            // InternalGaml.g:2374:5: lv_key_0_0= ruleK_Solve
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getKeyK_SolveParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_key_0_0=ruleK_Solve();

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
              						"gaml.compiler.Gaml.K_Solve");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:2391:3: ( (lv_expr_1_0= ruleEquationRef ) )
            // InternalGaml.g:2392:4: (lv_expr_1_0= ruleEquationRef )
            {
            // InternalGaml.g:2392:4: (lv_expr_1_0= ruleEquationRef )
            // InternalGaml.g:2393:5: lv_expr_1_0= ruleEquationRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getExprEquationRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
            lv_expr_1_0=ruleEquationRef();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SolveRule());
              					}
              					set(
              						current,
              						"expr",
              						lv_expr_1_0,
              						"gaml.compiler.Gaml.EquationRef");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_SolveRule());
              			}
              			newCompositeNode(grammarAccess.getS_SolveAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_Solve"


    // $ANTLR start "entryRuleS_Display"
    // InternalGaml.g:2425:1: entryRuleS_Display returns [EObject current=null] : iv_ruleS_Display= ruleS_Display EOF ;
    public final EObject entryRuleS_Display() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Display = null;


        try {
            // InternalGaml.g:2425:50: (iv_ruleS_Display= ruleS_Display EOF )
            // InternalGaml.g:2426:2: iv_ruleS_Display= ruleS_Display EOF
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
    // InternalGaml.g:2432:1: ruleS_Display returns [EObject current=null] : ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) ;
    public final EObject ruleS_Display() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2438:2: ( ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) )
            // InternalGaml.g:2439:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            {
            // InternalGaml.g:2439:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            // InternalGaml.g:2440:3: ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) )
            {
            // InternalGaml.g:2440:3: ( (lv_key_0_0= 'display' ) )
            // InternalGaml.g:2441:4: (lv_key_0_0= 'display' )
            {
            // InternalGaml.g:2441:4: (lv_key_0_0= 'display' )
            // InternalGaml.g:2442:5: lv_key_0_0= 'display'
            {
            lv_key_0_0=(Token)match(input,42,FOLLOW_16); if (state.failed) return current;
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

            // InternalGaml.g:2454:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:2455:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:2455:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:2456:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:2456:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==RULE_ID||(LA33_0>=48 && LA33_0<=91)) ) {
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
                    // InternalGaml.g:2457:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DisplayAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_19);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_DisplayRule());
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
                    // InternalGaml.g:2473:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_19); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getS_DisplayAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_DisplayRule());
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

            // InternalGaml.g:2490:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==RULE_ID||LA34_0==15||LA34_0==33||(LA34_0>=99 && LA34_0<=126)) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // InternalGaml.g:2491:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2491:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2492:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_DisplayAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
            	    lv_facets_2_0=ruleFacet();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getS_DisplayRule());
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
            	    break loop34;
                }
            } while (true);

            // InternalGaml.g:2509:3: ( (lv_block_3_0= ruleDisplayBlock ) )
            // InternalGaml.g:2510:4: (lv_block_3_0= ruleDisplayBlock )
            {
            // InternalGaml.g:2510:4: (lv_block_3_0= ruleDisplayBlock )
            // InternalGaml.g:2511:5: lv_block_3_0= ruleDisplayBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DisplayAccess().getBlockDisplayBlockParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_3_0=ruleDisplayBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DisplayRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_3_0,
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
    // InternalGaml.g:2532:1: entryRuleDisplayBlock returns [EObject current=null] : iv_ruleDisplayBlock= ruleDisplayBlock EOF ;
    public final EObject entryRuleDisplayBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDisplayBlock = null;


        try {
            // InternalGaml.g:2532:53: (iv_ruleDisplayBlock= ruleDisplayBlock EOF )
            // InternalGaml.g:2533:2: iv_ruleDisplayBlock= ruleDisplayBlock EOF
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
    // InternalGaml.g:2539:1: ruleDisplayBlock returns [EObject current=null] : ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' ) ;
    public final EObject ruleDisplayBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2545:2: ( ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' ) )
            // InternalGaml.g:2546:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:2546:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}' )
            // InternalGaml.g:2547:3: () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Layer ) )* otherlv_3= '}'
            {
            // InternalGaml.g:2547:3: ()
            // InternalGaml.g:2548:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getDisplayBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,39,FOLLOW_35); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getDisplayBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:2558:3: ( (lv_statements_2_0= ruleS_Layer ) )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( ((LA35_0>=43 && LA35_0<=46)||(LA35_0>=48 && LA35_0<=49)||(LA35_0>=78 && LA35_0<=88)) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // InternalGaml.g:2559:4: (lv_statements_2_0= ruleS_Layer )
            	    {
            	    // InternalGaml.g:2559:4: (lv_statements_2_0= ruleS_Layer )
            	    // InternalGaml.g:2560:5: lv_statements_2_0= ruleS_Layer
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getDisplayBlockAccess().getStatementsS_LayerParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_35);
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
            	    break loop35;
                }
            } while (true);

            otherlv_3=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2585:1: entryRuleS_Layer returns [EObject current=null] : iv_ruleS_Layer= ruleS_Layer EOF ;
    public final EObject entryRuleS_Layer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Layer = null;


        try {
            // InternalGaml.g:2585:48: (iv_ruleS_Layer= ruleS_Layer EOF )
            // InternalGaml.g:2586:2: iv_ruleS_Layer= ruleS_Layer EOF
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
    // InternalGaml.g:2592:1: ruleS_Layer returns [EObject current=null] : (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer ) ;
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
            // InternalGaml.g:2598:2: ( (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer ) )
            // InternalGaml.g:2599:2: (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer )
            {
            // InternalGaml.g:2599:2: (this_S_SpeciesLayer_0= ruleS_SpeciesLayer | this_S_ImageLayer_1= ruleS_ImageLayer | this_S_GraphicsLayer_2= ruleS_GraphicsLayer | this_S_EventLayer_3= ruleS_EventLayer | this_S_OverlayLayer_4= ruleS_OverlayLayer | this_S_OtherLayer_5= ruleS_OtherLayer )
            int alt36=6;
            switch ( input.LA(1) ) {
            case 48:
            case 49:
                {
                alt36=1;
                }
                break;
            case 43:
                {
                alt36=2;
                }
                break;
            case 44:
                {
                alt36=3;
                }
                break;
            case 45:
                {
                alt36=4;
                }
                break;
            case 46:
                {
                alt36=5;
                }
                break;
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
                {
                alt36=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }

            switch (alt36) {
                case 1 :
                    // InternalGaml.g:2600:3: this_S_SpeciesLayer_0= ruleS_SpeciesLayer
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
                    // InternalGaml.g:2609:3: this_S_ImageLayer_1= ruleS_ImageLayer
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
                    // InternalGaml.g:2618:3: this_S_GraphicsLayer_2= ruleS_GraphicsLayer
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
                    // InternalGaml.g:2627:3: this_S_EventLayer_3= ruleS_EventLayer
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
                    // InternalGaml.g:2636:3: this_S_OverlayLayer_4= ruleS_OverlayLayer
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
                    // InternalGaml.g:2645:3: this_S_OtherLayer_5= ruleS_OtherLayer
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
    // InternalGaml.g:2657:1: entryRuleS_SpeciesLayer returns [EObject current=null] : iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF ;
    public final EObject entryRuleS_SpeciesLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_SpeciesLayer = null;


        try {
            // InternalGaml.g:2657:55: (iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF )
            // InternalGaml.g:2658:2: iv_ruleS_SpeciesLayer= ruleS_SpeciesLayer EOF
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
    // InternalGaml.g:2664:1: ruleS_SpeciesLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_SpeciesLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2670:2: ( ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2671:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2671:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2672:3: ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2672:3: ( (lv_key_0_0= rule_SpeciesKey ) )
            // InternalGaml.g:2673:4: (lv_key_0_0= rule_SpeciesKey )
            {
            // InternalGaml.g:2673:4: (lv_key_0_0= rule_SpeciesKey )
            // InternalGaml.g:2674:5: lv_key_0_0= rule_SpeciesKey
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

            // InternalGaml.g:2691:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:2692:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:2692:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:2693:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
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
    // InternalGaml.g:2725:1: entryRuleS_ImageLayer returns [EObject current=null] : iv_ruleS_ImageLayer= ruleS_ImageLayer EOF ;
    public final EObject entryRuleS_ImageLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_ImageLayer = null;


        try {
            // InternalGaml.g:2725:53: (iv_ruleS_ImageLayer= ruleS_ImageLayer EOF )
            // InternalGaml.g:2726:2: iv_ruleS_ImageLayer= ruleS_ImageLayer EOF
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
    // InternalGaml.g:2732:1: ruleS_ImageLayer returns [EObject current=null] : ( ( (lv_key_0_0= 'picture' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' ) ;
    public final EObject ruleS_ImageLayer() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_3=null;
        EObject lv_expr_1_0 = null;

        EObject lv_facets_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2738:2: ( ( ( (lv_key_0_0= 'picture' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' ) )
            // InternalGaml.g:2739:2: ( ( (lv_key_0_0= 'picture' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' )
            {
            // InternalGaml.g:2739:2: ( ( (lv_key_0_0= 'picture' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';' )
            // InternalGaml.g:2740:3: ( (lv_key_0_0= 'picture' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_facets_2_0= ruleFacet ) )* otherlv_3= ';'
            {
            // InternalGaml.g:2740:3: ( (lv_key_0_0= 'picture' ) )
            // InternalGaml.g:2741:4: (lv_key_0_0= 'picture' )
            {
            // InternalGaml.g:2741:4: (lv_key_0_0= 'picture' )
            // InternalGaml.g:2742:5: lv_key_0_0= 'picture'
            {
            lv_key_0_0=(Token)match(input,43,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_ImageLayerAccess().getKeyPictureKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_ImageLayerRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "picture");
              				
            }

            }


            }

            // InternalGaml.g:2754:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:2755:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:2755:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:2756:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ImageLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_32);
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

            // InternalGaml.g:2773:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==RULE_ID||LA37_0==15||LA37_0==33||(LA37_0>=99 && LA37_0<=126)) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // InternalGaml.g:2774:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2774:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2775:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_ImageLayerAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_32);
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
            	    break loop37;
                }
            } while (true);

            otherlv_3=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2800:1: entryRuleS_GraphicsLayer returns [EObject current=null] : iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF ;
    public final EObject entryRuleS_GraphicsLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_GraphicsLayer = null;


        try {
            // InternalGaml.g:2800:56: (iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF )
            // InternalGaml.g:2801:2: iv_ruleS_GraphicsLayer= ruleS_GraphicsLayer EOF
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
    // InternalGaml.g:2807:1: ruleS_GraphicsLayer returns [EObject current=null] : ( ( (lv_key_0_0= 'graphics' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_GraphicsLayer() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2813:2: ( ( ( (lv_key_0_0= 'graphics' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2814:2: ( ( (lv_key_0_0= 'graphics' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2814:2: ( ( (lv_key_0_0= 'graphics' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2815:3: ( (lv_key_0_0= 'graphics' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2815:3: ( (lv_key_0_0= 'graphics' ) )
            // InternalGaml.g:2816:4: (lv_key_0_0= 'graphics' )
            {
            // InternalGaml.g:2816:4: (lv_key_0_0= 'graphics' )
            // InternalGaml.g:2817:5: lv_key_0_0= 'graphics'
            {
            lv_key_0_0=(Token)match(input,44,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_GraphicsLayerAccess().getKeyGraphicsKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_GraphicsLayerRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "graphics");
              				
            }

            }


            }

            // InternalGaml.g:2829:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:2830:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:2830:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:2831:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_GraphicsLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
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
    // InternalGaml.g:2863:1: entryRuleS_EventLayer returns [EObject current=null] : iv_ruleS_EventLayer= ruleS_EventLayer EOF ;
    public final EObject entryRuleS_EventLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_EventLayer = null;


        try {
            // InternalGaml.g:2863:53: (iv_ruleS_EventLayer= ruleS_EventLayer EOF )
            // InternalGaml.g:2864:2: iv_ruleS_EventLayer= ruleS_EventLayer EOF
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
    // InternalGaml.g:2870:1: ruleS_EventLayer returns [EObject current=null] : ( ( (lv_key_0_0= 'event' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_EventLayer() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2876:2: ( ( ( (lv_key_0_0= 'event' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2877:2: ( ( (lv_key_0_0= 'event' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2877:2: ( ( (lv_key_0_0= 'event' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2878:3: ( (lv_key_0_0= 'event' ) ) ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2878:3: ( (lv_key_0_0= 'event' ) )
            // InternalGaml.g:2879:4: (lv_key_0_0= 'event' )
            {
            // InternalGaml.g:2879:4: (lv_key_0_0= 'event' )
            // InternalGaml.g:2880:5: lv_key_0_0= 'event'
            {
            lv_key_0_0=(Token)match(input,45,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_EventLayerAccess().getKeyEventKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_EventLayerRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "event");
              				
            }

            }


            }

            // InternalGaml.g:2892:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:2893:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:2893:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:2894:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EventLayerAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
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
    // InternalGaml.g:2926:1: entryRuleS_OverlayLayer returns [EObject current=null] : iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF ;
    public final EObject entryRuleS_OverlayLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_OverlayLayer = null;


        try {
            // InternalGaml.g:2926:55: (iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF )
            // InternalGaml.g:2927:2: iv_ruleS_OverlayLayer= ruleS_OverlayLayer EOF
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
    // InternalGaml.g:2933:1: ruleS_OverlayLayer returns [EObject current=null] : ( ( (lv_key_0_0= 'overlay' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_OverlayLayer() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:2939:2: ( ( ( (lv_key_0_0= 'overlay' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2940:2: ( ( (lv_key_0_0= 'overlay' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2940:2: ( ( (lv_key_0_0= 'overlay' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2941:3: ( (lv_key_0_0= 'overlay' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2941:3: ( (lv_key_0_0= 'overlay' ) )
            // InternalGaml.g:2942:4: (lv_key_0_0= 'overlay' )
            {
            // InternalGaml.g:2942:4: (lv_key_0_0= 'overlay' )
            // InternalGaml.g:2943:5: lv_key_0_0= 'overlay'
            {
            lv_key_0_0=(Token)match(input,46,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_OverlayLayerAccess().getKeyOverlayKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_OverlayLayerRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "overlay");
              				
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
    // InternalGaml.g:2970:1: entryRuleS_OtherLayer returns [EObject current=null] : iv_ruleS_OtherLayer= ruleS_OtherLayer EOF ;
    public final EObject entryRuleS_OtherLayer() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_OtherLayer = null;


        try {
            // InternalGaml.g:2970:53: (iv_ruleS_OtherLayer= ruleS_OtherLayer EOF )
            // InternalGaml.g:2971:2: iv_ruleS_OtherLayer= ruleS_OtherLayer EOF
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
    // InternalGaml.g:2977:1: ruleS_OtherLayer returns [EObject current=null] : ( ( (lv_key_0_0= rule_LayerKey ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) ;
    public final EObject ruleS_OtherLayer() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:2983:2: ( ( ( (lv_key_0_0= rule_LayerKey ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) )
            // InternalGaml.g:2984:2: ( ( (lv_key_0_0= rule_LayerKey ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            {
            // InternalGaml.g:2984:2: ( ( (lv_key_0_0= rule_LayerKey ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2985:3: ( (lv_key_0_0= rule_LayerKey ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2985:3: ( (lv_key_0_0= rule_LayerKey ) )
            // InternalGaml.g:2986:4: (lv_key_0_0= rule_LayerKey )
            {
            // InternalGaml.g:2986:4: (lv_key_0_0= rule_LayerKey )
            // InternalGaml.g:2987:5: lv_key_0_0= rule_LayerKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherLayerAccess().getKey_LayerKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_36);
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

            // InternalGaml.g:3004:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            int alt38=2;
            alt38 = dfa38.predict(input);
            switch (alt38) {
                case 1 :
                    // InternalGaml.g:3005:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    {
                    // InternalGaml.g:3005:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    // InternalGaml.g:3006:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    {
                    // InternalGaml.g:3015:5: ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    // InternalGaml.g:3016:6: ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
                    {
                    // InternalGaml.g:3016:6: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:3017:7: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:3017:7: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:3018:8: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getS_OtherLayerAccess().getExprExpressionParserRuleCall_1_0_0_0_0());
                      							
                    }
                    pushFollow(FOLLOW_17);
                    lv_expr_1_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      								if (current==null) {
                      									current = createModelElementForParent(grammarAccess.getS_OtherLayerRule());
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
                      							current = createModelElement(grammarAccess.getS_OtherLayerRule());
                      						}
                      						newCompositeNode(grammarAccess.getS_OtherLayerAccess().getFacetsAndBlockParserRuleCall_1_0_0_1());
                      					
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


                    }
                    break;
                case 2 :
                    // InternalGaml.g:3049:4: this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
                    {
                    if ( state.backtracking==0 ) {

                      				if (current==null) {
                      					current = createModelElement(grammarAccess.getS_OtherLayerRule());
                      				}
                      				newCompositeNode(grammarAccess.getS_OtherLayerAccess().getFacetsAndBlockParserRuleCall_1_1());
                      			
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
    // $ANTLR end "ruleS_OtherLayer"


    // $ANTLR start "entryRuleK_Solve"
    // InternalGaml.g:3065:1: entryRuleK_Solve returns [String current=null] : iv_ruleK_Solve= ruleK_Solve EOF ;
    public final String entryRuleK_Solve() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Solve = null;


        try {
            // InternalGaml.g:3065:47: (iv_ruleK_Solve= ruleK_Solve EOF )
            // InternalGaml.g:3066:2: iv_ruleK_Solve= ruleK_Solve EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_SolveRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Solve=ruleK_Solve();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Solve.getText(); 
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
    // $ANTLR end "entryRuleK_Solve"


    // $ANTLR start "ruleK_Solve"
    // InternalGaml.g:3072:1: ruleK_Solve returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'solve' ;
    public final AntlrDatatypeRuleToken ruleK_Solve() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3078:2: (kw= 'solve' )
            // InternalGaml.g:3079:2: kw= 'solve'
            {
            kw=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_SolveAccess().getSolveKeyword());
              	
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
    // $ANTLR end "ruleK_Solve"


    // $ANTLR start "entryRule_SpeciesKey"
    // InternalGaml.g:3087:1: entryRule_SpeciesKey returns [String current=null] : iv_rule_SpeciesKey= rule_SpeciesKey EOF ;
    public final String entryRule_SpeciesKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_SpeciesKey = null;


        try {
            // InternalGaml.g:3087:51: (iv_rule_SpeciesKey= rule_SpeciesKey EOF )
            // InternalGaml.g:3088:2: iv_rule_SpeciesKey= rule_SpeciesKey EOF
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
    // InternalGaml.g:3094:1: rule_SpeciesKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'species' | kw= 'grid' ) ;
    public final AntlrDatatypeRuleToken rule_SpeciesKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3100:2: ( (kw= 'species' | kw= 'grid' ) )
            // InternalGaml.g:3101:2: (kw= 'species' | kw= 'grid' )
            {
            // InternalGaml.g:3101:2: (kw= 'species' | kw= 'grid' )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==48) ) {
                alt39=1;
            }
            else if ( (LA39_0==49) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // InternalGaml.g:3102:3: kw= 'species'
                    {
                    kw=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_SpeciesKeyAccess().getSpeciesKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3108:3: kw= 'grid'
                    {
                    kw=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRule_ExperimentKey"
    // InternalGaml.g:3117:1: entryRule_ExperimentKey returns [String current=null] : iv_rule_ExperimentKey= rule_ExperimentKey EOF ;
    public final String entryRule_ExperimentKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_ExperimentKey = null;


        try {
            // InternalGaml.g:3117:54: (iv_rule_ExperimentKey= rule_ExperimentKey EOF )
            // InternalGaml.g:3118:2: iv_rule_ExperimentKey= rule_ExperimentKey EOF
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
    // InternalGaml.g:3124:1: rule_ExperimentKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'experiment' ;
    public final AntlrDatatypeRuleToken rule_ExperimentKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3130:2: (kw= 'experiment' )
            // InternalGaml.g:3131:2: kw= 'experiment'
            {
            kw=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3139:1: entryRule_GeneralKey returns [String current=null] : iv_rule_GeneralKey= rule_GeneralKey EOF ;
    public final String entryRule_GeneralKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_GeneralKey = null;


        try {
            // InternalGaml.g:3139:51: (iv_rule_GeneralKey= rule_GeneralKey EOF )
            // InternalGaml.g:3140:2: iv_rule_GeneralKey= rule_GeneralKey EOF
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
    // InternalGaml.g:3146:1: rule_GeneralKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) ;
    public final AntlrDatatypeRuleToken rule_GeneralKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this__LayerKey_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3152:2: ( (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) )
            // InternalGaml.g:3153:2: (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            {
            // InternalGaml.g:3153:2: (this__LayerKey_0= rule_LayerKey | kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            int alt40=28;
            switch ( input.LA(1) ) {
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
                {
                alt40=1;
                }
                break;
            case 51:
                {
                alt40=2;
                }
                break;
            case 52:
                {
                alt40=3;
                }
                break;
            case 53:
                {
                alt40=4;
                }
                break;
            case 54:
                {
                alt40=5;
                }
                break;
            case 55:
                {
                alt40=6;
                }
                break;
            case 56:
                {
                alt40=7;
                }
                break;
            case 57:
                {
                alt40=8;
                }
                break;
            case 58:
                {
                alt40=9;
                }
                break;
            case 59:
                {
                alt40=10;
                }
                break;
            case 60:
                {
                alt40=11;
                }
                break;
            case 61:
                {
                alt40=12;
                }
                break;
            case 62:
                {
                alt40=13;
                }
                break;
            case 63:
                {
                alt40=14;
                }
                break;
            case 64:
                {
                alt40=15;
                }
                break;
            case 65:
                {
                alt40=16;
                }
                break;
            case 66:
                {
                alt40=17;
                }
                break;
            case 67:
                {
                alt40=18;
                }
                break;
            case 68:
                {
                alt40=19;
                }
                break;
            case 69:
                {
                alt40=20;
                }
                break;
            case 70:
                {
                alt40=21;
                }
                break;
            case 71:
                {
                alt40=22;
                }
                break;
            case 72:
                {
                alt40=23;
                }
                break;
            case 73:
                {
                alt40=24;
                }
                break;
            case 74:
                {
                alt40=25;
                }
                break;
            case 75:
                {
                alt40=26;
                }
                break;
            case 76:
                {
                alt40=27;
                }
                break;
            case 77:
                {
                alt40=28;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // InternalGaml.g:3154:3: this__LayerKey_0= rule_LayerKey
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
                    // InternalGaml.g:3165:3: kw= 'ask'
                    {
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAskKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3171:3: kw= 'release'
                    {
                    kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getReleaseKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3177:3: kw= 'capture'
                    {
                    kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCaptureKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3183:3: kw= 'create'
                    {
                    kw=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCreateKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3189:3: kw= 'write'
                    {
                    kw=(Token)match(input,55,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWriteKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3195:3: kw= 'error'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getErrorKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3201:3: kw= 'warn'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWarnKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3207:3: kw= 'exception'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getExceptionKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3213:3: kw= 'save'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSaveKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3219:3: kw= 'assert'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAssertKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:3225:3: kw= 'inspect'
                    {
                    kw=(Token)match(input,61,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getInspectKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:3231:3: kw= 'browse'
                    {
                    kw=(Token)match(input,62,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getBrowseKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:3237:3: kw= 'restore'
                    {
                    kw=(Token)match(input,63,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRestoreKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:3243:3: kw= 'draw'
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getDrawKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:3249:3: kw= 'using'
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getUsingKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:3255:3: kw= 'switch'
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSwitchKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:3261:3: kw= 'put'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getPutKeyword_17());
                      		
                    }

                    }
                    break;
                case 19 :
                    // InternalGaml.g:3267:3: kw= 'add'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAddKeyword_18());
                      		
                    }

                    }
                    break;
                case 20 :
                    // InternalGaml.g:3273:3: kw= 'remove'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRemoveKeyword_19());
                      		
                    }

                    }
                    break;
                case 21 :
                    // InternalGaml.g:3279:3: kw= 'match'
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatchKeyword_20());
                      		
                    }

                    }
                    break;
                case 22 :
                    // InternalGaml.g:3285:3: kw= 'match_between'
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_betweenKeyword_21());
                      		
                    }

                    }
                    break;
                case 23 :
                    // InternalGaml.g:3291:3: kw= 'match_one'
                    {
                    kw=(Token)match(input,72,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_oneKeyword_22());
                      		
                    }

                    }
                    break;
                case 24 :
                    // InternalGaml.g:3297:3: kw= 'parameter'
                    {
                    kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getParameterKeyword_23());
                      		
                    }

                    }
                    break;
                case 25 :
                    // InternalGaml.g:3303:3: kw= 'status'
                    {
                    kw=(Token)match(input,74,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getStatusKeyword_24());
                      		
                    }

                    }
                    break;
                case 26 :
                    // InternalGaml.g:3309:3: kw= 'highlight'
                    {
                    kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getHighlightKeyword_25());
                      		
                    }

                    }
                    break;
                case 27 :
                    // InternalGaml.g:3315:3: kw= 'focus_on'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getFocus_onKeyword_26());
                      		
                    }

                    }
                    break;
                case 28 :
                    // InternalGaml.g:3321:3: kw= 'layout'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3330:1: entryRule_LayerKey returns [String current=null] : iv_rule_LayerKey= rule_LayerKey EOF ;
    public final String entryRule_LayerKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_LayerKey = null;


        try {
            // InternalGaml.g:3330:49: (iv_rule_LayerKey= rule_LayerKey EOF )
            // InternalGaml.g:3331:2: iv_rule_LayerKey= rule_LayerKey EOF
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
    // InternalGaml.g:3337:1: rule_LayerKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' ) ;
    public final AntlrDatatypeRuleToken rule_LayerKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3343:2: ( (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' ) )
            // InternalGaml.g:3344:2: (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' )
            {
            // InternalGaml.g:3344:2: (kw= 'light' | kw= 'camera' | kw= 'text' | kw= 'data' | kw= 'chart' | kw= 'agents' | kw= 'display_population' | kw= 'display_grid' | kw= 'datalist' | kw= 'mesh' | kw= 'rotation' )
            int alt41=11;
            switch ( input.LA(1) ) {
            case 78:
                {
                alt41=1;
                }
                break;
            case 79:
                {
                alt41=2;
                }
                break;
            case 80:
                {
                alt41=3;
                }
                break;
            case 81:
                {
                alt41=4;
                }
                break;
            case 82:
                {
                alt41=5;
                }
                break;
            case 83:
                {
                alt41=6;
                }
                break;
            case 84:
                {
                alt41=7;
                }
                break;
            case 85:
                {
                alt41=8;
                }
                break;
            case 86:
                {
                alt41=9;
                }
                break;
            case 87:
                {
                alt41=10;
                }
                break;
            case 88:
                {
                alt41=11;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }

            switch (alt41) {
                case 1 :
                    // InternalGaml.g:3345:3: kw= 'light'
                    {
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getLightKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3351:3: kw= 'camera'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getCameraKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3357:3: kw= 'text'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getTextKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3363:3: kw= 'data'
                    {
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDataKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3369:3: kw= 'chart'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getChartKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3375:3: kw= 'agents'
                    {
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getAgentsKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3381:3: kw= 'display_population'
                    {
                    kw=(Token)match(input,84,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDisplay_populationKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3387:3: kw= 'display_grid'
                    {
                    kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDisplay_gridKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3393:3: kw= 'datalist'
                    {
                    kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getDatalistKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3399:3: kw= 'mesh'
                    {
                    kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getMeshKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3405:3: kw= 'rotation'
                    {
                    kw=(Token)match(input,88,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_LayerKeyAccess().getRotationKeyword_10());
                      		
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


    // $ANTLR start "entryRuleK_Reflex"
    // InternalGaml.g:3414:1: entryRuleK_Reflex returns [String current=null] : iv_ruleK_Reflex= ruleK_Reflex EOF ;
    public final String entryRuleK_Reflex() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Reflex = null;


        try {
            // InternalGaml.g:3414:48: (iv_ruleK_Reflex= ruleK_Reflex EOF )
            // InternalGaml.g:3415:2: iv_ruleK_Reflex= ruleK_Reflex EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_ReflexRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Reflex=ruleK_Reflex();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Reflex.getText(); 
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
    // $ANTLR end "entryRuleK_Reflex"


    // $ANTLR start "ruleK_Reflex"
    // InternalGaml.g:3421:1: ruleK_Reflex returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'init' | kw= 'reflex' | kw= 'aspect' ) ;
    public final AntlrDatatypeRuleToken ruleK_Reflex() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3427:2: ( (kw= 'init' | kw= 'reflex' | kw= 'aspect' ) )
            // InternalGaml.g:3428:2: (kw= 'init' | kw= 'reflex' | kw= 'aspect' )
            {
            // InternalGaml.g:3428:2: (kw= 'init' | kw= 'reflex' | kw= 'aspect' )
            int alt42=3;
            switch ( input.LA(1) ) {
            case 89:
                {
                alt42=1;
                }
                break;
            case 90:
                {
                alt42=2;
                }
                break;
            case 91:
                {
                alt42=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }

            switch (alt42) {
                case 1 :
                    // InternalGaml.g:3429:3: kw= 'init'
                    {
                    kw=(Token)match(input,89,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_ReflexAccess().getInitKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3435:3: kw= 'reflex'
                    {
                    kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_ReflexAccess().getReflexKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3441:3: kw= 'aspect'
                    {
                    kw=(Token)match(input,91,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_ReflexAccess().getAspectKeyword_2());
                      		
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
    // $ANTLR end "ruleK_Reflex"


    // $ANTLR start "entryRuleK_Assignment"
    // InternalGaml.g:3450:1: entryRuleK_Assignment returns [String current=null] : iv_ruleK_Assignment= ruleK_Assignment EOF ;
    public final String entryRuleK_Assignment() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Assignment = null;


        try {
            // InternalGaml.g:3450:52: (iv_ruleK_Assignment= ruleK_Assignment EOF )
            // InternalGaml.g:3451:2: iv_ruleK_Assignment= ruleK_Assignment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_AssignmentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Assignment=ruleK_Assignment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Assignment.getText(); 
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
    // $ANTLR end "entryRuleK_Assignment"


    // $ANTLR start "ruleK_Assignment"
    // InternalGaml.g:3457:1: ruleK_Assignment returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) ;
    public final AntlrDatatypeRuleToken ruleK_Assignment() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3463:2: ( (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) )
            // InternalGaml.g:3464:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            {
            // InternalGaml.g:3464:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            int alt43=8;
            alt43 = dfa43.predict(input);
            switch (alt43) {
                case 1 :
                    // InternalGaml.g:3465:3: kw= '<-'
                    {
                    kw=(Token)match(input,15,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignHyphenMinusKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3471:3: kw= '<<'
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3477:3: (kw= '>' kw= '>' )
                    {
                    // InternalGaml.g:3477:3: (kw= '>' kw= '>' )
                    // InternalGaml.g:3478:4: kw= '>' kw= '>'
                    {
                    kw=(Token)match(input,93,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:3490:3: kw= '<<+'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignPlusSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3496:3: (kw= '>' kw= '>-' )
                    {
                    // InternalGaml.g:3496:3: (kw= '>' kw= '>-' )
                    // InternalGaml.g:3497:4: kw= '>' kw= '>-'
                    {
                    kw=(Token)match(input,93,FOLLOW_38); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_4_0());
                      			
                    }
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignHyphenMinusKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalGaml.g:3509:3: kw= '+<-'
                    {
                    kw=(Token)match(input,96,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getPlusSignLessThanSignHyphenMinusKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3515:3: kw= '<+'
                    {
                    kw=(Token)match(input,97,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignPlusSignKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3521:3: kw= '>-'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignHyphenMinusKeyword_7());
                      		
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
    // $ANTLR end "ruleK_Assignment"


    // $ANTLR start "entryRuleActionArguments"
    // InternalGaml.g:3530:1: entryRuleActionArguments returns [EObject current=null] : iv_ruleActionArguments= ruleActionArguments EOF ;
    public final EObject entryRuleActionArguments() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionArguments = null;


        try {
            // InternalGaml.g:3530:56: (iv_ruleActionArguments= ruleActionArguments EOF )
            // InternalGaml.g:3531:2: iv_ruleActionArguments= ruleActionArguments EOF
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
    // InternalGaml.g:3537:1: ruleActionArguments returns [EObject current=null] : ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) ;
    public final EObject ruleActionArguments() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_args_0_0 = null;

        EObject lv_args_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3543:2: ( ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) )
            // InternalGaml.g:3544:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            {
            // InternalGaml.g:3544:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            // InternalGaml.g:3545:3: ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            {
            // InternalGaml.g:3545:3: ( (lv_args_0_0= ruleArgumentDefinition ) )
            // InternalGaml.g:3546:4: (lv_args_0_0= ruleArgumentDefinition )
            {
            // InternalGaml.g:3546:4: (lv_args_0_0= ruleArgumentDefinition )
            // InternalGaml.g:3547:5: lv_args_0_0= ruleArgumentDefinition
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_39);
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

            // InternalGaml.g:3564:3: (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==98) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // InternalGaml.g:3565:4: otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    {
            	    otherlv_1=(Token)match(input,98,FOLLOW_29); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_1, grammarAccess.getActionArgumentsAccess().getCommaKeyword_1_0());
            	      			
            	    }
            	    // InternalGaml.g:3569:4: ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    // InternalGaml.g:3570:5: (lv_args_2_0= ruleArgumentDefinition )
            	    {
            	    // InternalGaml.g:3570:5: (lv_args_2_0= ruleArgumentDefinition )
            	    // InternalGaml.g:3571:6: lv_args_2_0= ruleArgumentDefinition
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_39);
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
            	    break loop44;
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
    // InternalGaml.g:3593:1: entryRuleArgumentDefinition returns [EObject current=null] : iv_ruleArgumentDefinition= ruleArgumentDefinition EOF ;
    public final EObject entryRuleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArgumentDefinition = null;


        try {
            // InternalGaml.g:3593:59: (iv_ruleArgumentDefinition= ruleArgumentDefinition EOF )
            // InternalGaml.g:3594:2: iv_ruleArgumentDefinition= ruleArgumentDefinition EOF
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
    // InternalGaml.g:3600:1: ruleArgumentDefinition returns [EObject current=null] : ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) ;
    public final EObject ruleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject lv_type_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_default_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3606:2: ( ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) )
            // InternalGaml.g:3607:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            {
            // InternalGaml.g:3607:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            // InternalGaml.g:3608:3: ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            {
            // InternalGaml.g:3608:3: ( (lv_type_0_0= ruleTypeRef ) )
            // InternalGaml.g:3609:4: (lv_type_0_0= ruleTypeRef )
            {
            // InternalGaml.g:3609:4: (lv_type_0_0= ruleTypeRef )
            // InternalGaml.g:3610:5: lv_type_0_0= ruleTypeRef
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

            // InternalGaml.g:3627:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:3628:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:3628:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:3629:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_40);
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

            // InternalGaml.g:3646:3: (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==15) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // InternalGaml.g:3647:4: otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) )
                    {
                    otherlv_2=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getArgumentDefinitionAccess().getLessThanSignHyphenMinusKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:3651:4: ( (lv_default_3_0= ruleExpression ) )
                    // InternalGaml.g:3652:5: (lv_default_3_0= ruleExpression )
                    {
                    // InternalGaml.g:3652:5: (lv_default_3_0= ruleExpression )
                    // InternalGaml.g:3653:6: lv_default_3_0= ruleExpression
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
    // InternalGaml.g:3675:1: entryRuleFacet returns [EObject current=null] : iv_ruleFacet= ruleFacet EOF ;
    public final EObject entryRuleFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFacet = null;


        try {
            // InternalGaml.g:3675:46: (iv_ruleFacet= ruleFacet EOF )
            // InternalGaml.g:3676:2: iv_ruleFacet= ruleFacet EOF
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
    // InternalGaml.g:3682:1: ruleFacet returns [EObject current=null] : (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) ;
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
            // InternalGaml.g:3688:2: ( (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) )
            // InternalGaml.g:3689:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            {
            // InternalGaml.g:3689:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            int alt46=6;
            switch ( input.LA(1) ) {
            case 123:
            case 124:
                {
                alt46=1;
                }
                break;
            case 99:
            case 100:
                {
                alt46=2;
                }
                break;
            case RULE_ID:
            case 15:
            case 33:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
                {
                alt46=3;
                }
                break;
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
                {
                alt46=4;
                }
                break;
            case 125:
                {
                alt46=5;
                }
                break;
            case 126:
                {
                alt46=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }

            switch (alt46) {
                case 1 :
                    // InternalGaml.g:3690:3: this_ActionFacet_0= ruleActionFacet
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
                    // InternalGaml.g:3699:3: this_DefinitionFacet_1= ruleDefinitionFacet
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
                    // InternalGaml.g:3708:3: this_ClassicFacet_2= ruleClassicFacet
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
                    // InternalGaml.g:3717:3: this_TypeFacet_3= ruleTypeFacet
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
                    // InternalGaml.g:3726:3: this_VarFacet_4= ruleVarFacet
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
                    // InternalGaml.g:3735:3: this_FunctionFacet_5= ruleFunctionFacet
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


    // $ANTLR start "entryRuleClassicFacetKey"
    // InternalGaml.g:3747:1: entryRuleClassicFacetKey returns [String current=null] : iv_ruleClassicFacetKey= ruleClassicFacetKey EOF ;
    public final String entryRuleClassicFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleClassicFacetKey = null;


        try {
            // InternalGaml.g:3747:55: (iv_ruleClassicFacetKey= ruleClassicFacetKey EOF )
            // InternalGaml.g:3748:2: iv_ruleClassicFacetKey= ruleClassicFacetKey EOF
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
    // InternalGaml.g:3754:1: ruleClassicFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID kw= ':' ) ;
    public final AntlrDatatypeRuleToken ruleClassicFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3760:2: ( (this_ID_0= RULE_ID kw= ':' ) )
            // InternalGaml.g:3761:2: (this_ID_0= RULE_ID kw= ':' )
            {
            // InternalGaml.g:3761:2: (this_ID_0= RULE_ID kw= ':' )
            // InternalGaml.g:3762:3: this_ID_0= RULE_ID kw= ':'
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_27); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(this_ID_0);
              		
            }
            if ( state.backtracking==0 ) {

              			newLeafNode(this_ID_0, grammarAccess.getClassicFacetKeyAccess().getIDTerminalRuleCall_0());
              		
            }
            kw=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3778:1: entryRuleDefinitionFacetKey returns [String current=null] : iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF ;
    public final String entryRuleDefinitionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleDefinitionFacetKey = null;


        try {
            // InternalGaml.g:3778:58: (iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF )
            // InternalGaml.g:3779:2: iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF
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
    // InternalGaml.g:3785:1: ruleDefinitionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'name:' | kw= 'returns:' ) ;
    public final AntlrDatatypeRuleToken ruleDefinitionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3791:2: ( (kw= 'name:' | kw= 'returns:' ) )
            // InternalGaml.g:3792:2: (kw= 'name:' | kw= 'returns:' )
            {
            // InternalGaml.g:3792:2: (kw= 'name:' | kw= 'returns:' )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==99) ) {
                alt47=1;
            }
            else if ( (LA47_0==100) ) {
                alt47=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // InternalGaml.g:3793:3: kw= 'name:'
                    {
                    kw=(Token)match(input,99,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getDefinitionFacetKeyAccess().getNameKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3799:3: kw= 'returns:'
                    {
                    kw=(Token)match(input,100,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3808:1: entryRuleTypeFacetKey returns [String current=null] : iv_ruleTypeFacetKey= ruleTypeFacetKey EOF ;
    public final String entryRuleTypeFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleTypeFacetKey = null;


        try {
            // InternalGaml.g:3808:52: (iv_ruleTypeFacetKey= ruleTypeFacetKey EOF )
            // InternalGaml.g:3809:2: iv_ruleTypeFacetKey= ruleTypeFacetKey EOF
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
    // InternalGaml.g:3815:1: ruleTypeFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) ;
    public final AntlrDatatypeRuleToken ruleTypeFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3821:2: ( (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) )
            // InternalGaml.g:3822:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            {
            // InternalGaml.g:3822:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            int alt48=5;
            switch ( input.LA(1) ) {
            case 101:
                {
                alt48=1;
                }
                break;
            case 102:
                {
                alt48=2;
                }
                break;
            case 103:
                {
                alt48=3;
                }
                break;
            case 104:
                {
                alt48=4;
                }
                break;
            case 105:
                {
                alt48=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }

            switch (alt48) {
                case 1 :
                    // InternalGaml.g:3823:3: kw= 'as:'
                    {
                    kw=(Token)match(input,101,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getAsKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3829:3: kw= 'of:'
                    {
                    kw=(Token)match(input,102,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getOfKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3835:3: kw= 'parent:'
                    {
                    kw=(Token)match(input,103,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getParentKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:3841:3: kw= 'species:'
                    {
                    kw=(Token)match(input,104,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getSpeciesKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3847:3: kw= 'type:'
                    {
                    kw=(Token)match(input,105,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3856:1: entryRuleSpecialFacetKey returns [String current=null] : iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF ;
    public final String entryRuleSpecialFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSpecialFacetKey = null;


        try {
            // InternalGaml.g:3856:55: (iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF )
            // InternalGaml.g:3857:2: iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF
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
    // InternalGaml.g:3863:1: ruleSpecialFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) ;
    public final AntlrDatatypeRuleToken ruleSpecialFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3869:2: ( (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) )
            // InternalGaml.g:3870:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            {
            // InternalGaml.g:3870:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            int alt49=18;
            switch ( input.LA(1) ) {
            case 106:
                {
                alt49=1;
                }
                break;
            case 107:
                {
                alt49=2;
                }
                break;
            case 33:
                {
                alt49=3;
                }
                break;
            case 108:
                {
                alt49=4;
                }
                break;
            case 109:
                {
                alt49=5;
                }
                break;
            case 110:
                {
                alt49=6;
                }
                break;
            case 111:
                {
                alt49=7;
                }
                break;
            case 112:
                {
                alt49=8;
                }
                break;
            case 113:
                {
                alt49=9;
                }
                break;
            case 114:
                {
                alt49=10;
                }
                break;
            case 115:
                {
                alt49=11;
                }
                break;
            case 116:
                {
                alt49=12;
                }
                break;
            case 117:
                {
                alt49=13;
                }
                break;
            case 118:
                {
                alt49=14;
                }
                break;
            case 119:
                {
                alt49=15;
                }
                break;
            case 120:
                {
                alt49=16;
                }
                break;
            case 121:
                {
                alt49=17;
                }
                break;
            case 122:
                {
                alt49=18;
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
                    // InternalGaml.g:3871:3: kw= 'camera:'
                    {
                    kw=(Token)match(input,106,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getCameraKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3877:3: kw= 'data:'
                    {
                    kw=(Token)match(input,107,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getDataKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3883:3: (kw= 'when' kw= ':' )
                    {
                    // InternalGaml.g:3883:3: (kw= 'when' kw= ':' )
                    // InternalGaml.g:3884:4: kw= 'when' kw= ':'
                    {
                    kw=(Token)match(input,33,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getWhenKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getColonKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:3896:3: kw= 'const:'
                    {
                    kw=(Token)match(input,108,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getConstKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3902:3: kw= 'value:'
                    {
                    kw=(Token)match(input,109,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getValueKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3908:3: kw= 'topology:'
                    {
                    kw=(Token)match(input,110,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTopologyKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3914:3: kw= 'item:'
                    {
                    kw=(Token)match(input,111,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getItemKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3920:3: kw= 'init:'
                    {
                    kw=(Token)match(input,112,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getInitKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3926:3: kw= 'message:'
                    {
                    kw=(Token)match(input,113,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getMessageKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3932:3: kw= 'control:'
                    {
                    kw=(Token)match(input,114,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getControlKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3938:3: kw= 'layout:'
                    {
                    kw=(Token)match(input,115,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getLayoutKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:3944:3: kw= 'environment:'
                    {
                    kw=(Token)match(input,116,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getEnvironmentKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:3950:3: kw= 'text:'
                    {
                    kw=(Token)match(input,117,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTextKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:3956:3: kw= 'image:'
                    {
                    kw=(Token)match(input,118,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getImageKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:3962:3: kw= 'using:'
                    {
                    kw=(Token)match(input,119,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getUsingKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:3968:3: kw= 'parameter:'
                    {
                    kw=(Token)match(input,120,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getParameterKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:3974:3: kw= 'aspect:'
                    {
                    kw=(Token)match(input,121,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getAspectKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:3980:3: kw= 'light:'
                    {
                    kw=(Token)match(input,122,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3989:1: entryRuleActionFacetKey returns [String current=null] : iv_ruleActionFacetKey= ruleActionFacetKey EOF ;
    public final String entryRuleActionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleActionFacetKey = null;


        try {
            // InternalGaml.g:3989:54: (iv_ruleActionFacetKey= ruleActionFacetKey EOF )
            // InternalGaml.g:3990:2: iv_ruleActionFacetKey= ruleActionFacetKey EOF
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
    // InternalGaml.g:3996:1: ruleActionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'action:' | kw= 'on_change:' ) ;
    public final AntlrDatatypeRuleToken ruleActionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4002:2: ( (kw= 'action:' | kw= 'on_change:' ) )
            // InternalGaml.g:4003:2: (kw= 'action:' | kw= 'on_change:' )
            {
            // InternalGaml.g:4003:2: (kw= 'action:' | kw= 'on_change:' )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==123) ) {
                alt50=1;
            }
            else if ( (LA50_0==124) ) {
                alt50=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // InternalGaml.g:4004:3: kw= 'action:'
                    {
                    kw=(Token)match(input,123,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getActionFacetKeyAccess().getActionKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:4010:3: kw= 'on_change:'
                    {
                    kw=(Token)match(input,124,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4019:1: entryRuleVarFacetKey returns [String current=null] : iv_ruleVarFacetKey= ruleVarFacetKey EOF ;
    public final String entryRuleVarFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleVarFacetKey = null;


        try {
            // InternalGaml.g:4019:51: (iv_ruleVarFacetKey= ruleVarFacetKey EOF )
            // InternalGaml.g:4020:2: iv_ruleVarFacetKey= ruleVarFacetKey EOF
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
    // InternalGaml.g:4026:1: ruleVarFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'var:' ;
    public final AntlrDatatypeRuleToken ruleVarFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:4032:2: (kw= 'var:' )
            // InternalGaml.g:4033:2: kw= 'var:'
            {
            kw=(Token)match(input,125,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4041:1: entryRuleClassicFacet returns [EObject current=null] : iv_ruleClassicFacet= ruleClassicFacet EOF ;
    public final EObject entryRuleClassicFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassicFacet = null;


        try {
            // InternalGaml.g:4041:53: (iv_ruleClassicFacet= ruleClassicFacet EOF )
            // InternalGaml.g:4042:2: iv_ruleClassicFacet= ruleClassicFacet EOF
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
    // InternalGaml.g:4048:1: ruleClassicFacet returns [EObject current=null] : ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) ;
    public final EObject ruleClassicFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_key_2_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4054:2: ( ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) )
            // InternalGaml.g:4055:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            {
            // InternalGaml.g:4055:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            // InternalGaml.g:4056:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) )
            {
            // InternalGaml.g:4056:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) )
            int alt51=3;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt51=1;
                }
                break;
            case 15:
                {
                alt51=2;
                }
                break;
            case 33:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
                {
                alt51=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }

            switch (alt51) {
                case 1 :
                    // InternalGaml.g:4057:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    {
                    // InternalGaml.g:4057:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    // InternalGaml.g:4058:5: (lv_key_0_0= ruleClassicFacetKey )
                    {
                    // InternalGaml.g:4058:5: (lv_key_0_0= ruleClassicFacetKey )
                    // InternalGaml.g:4059:6: lv_key_0_0= ruleClassicFacetKey
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
                    // InternalGaml.g:4077:4: ( (lv_key_1_0= '<-' ) )
                    {
                    // InternalGaml.g:4077:4: ( (lv_key_1_0= '<-' ) )
                    // InternalGaml.g:4078:5: (lv_key_1_0= '<-' )
                    {
                    // InternalGaml.g:4078:5: (lv_key_1_0= '<-' )
                    // InternalGaml.g:4079:6: lv_key_1_0= '<-'
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
                    // InternalGaml.g:4092:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    {
                    // InternalGaml.g:4092:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    // InternalGaml.g:4093:5: (lv_key_2_0= ruleSpecialFacetKey )
                    {
                    // InternalGaml.g:4093:5: (lv_key_2_0= ruleSpecialFacetKey )
                    // InternalGaml.g:4094:6: lv_key_2_0= ruleSpecialFacetKey
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

            // InternalGaml.g:4112:3: ( (lv_expr_3_0= ruleExpression ) )
            // InternalGaml.g:4113:4: (lv_expr_3_0= ruleExpression )
            {
            // InternalGaml.g:4113:4: (lv_expr_3_0= ruleExpression )
            // InternalGaml.g:4114:5: lv_expr_3_0= ruleExpression
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
    // InternalGaml.g:4135:1: entryRuleDefinitionFacet returns [EObject current=null] : iv_ruleDefinitionFacet= ruleDefinitionFacet EOF ;
    public final EObject entryRuleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDefinitionFacet = null;


        try {
            // InternalGaml.g:4135:56: (iv_ruleDefinitionFacet= ruleDefinitionFacet EOF )
            // InternalGaml.g:4136:2: iv_ruleDefinitionFacet= ruleDefinitionFacet EOF
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
    // InternalGaml.g:4142:1: ruleDefinitionFacet returns [EObject current=null] : ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) ;
    public final EObject ruleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:4148:2: ( ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) )
            // InternalGaml.g:4149:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            {
            // InternalGaml.g:4149:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            // InternalGaml.g:4150:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            {
            // InternalGaml.g:4150:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) )
            // InternalGaml.g:4151:4: ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey )
            {
            // InternalGaml.g:4152:4: (lv_key_0_0= ruleDefinitionFacetKey )
            // InternalGaml.g:4153:5: lv_key_0_0= ruleDefinitionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getDefinitionFacetAccess().getKeyDefinitionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_16);
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

            // InternalGaml.g:4170:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:4171:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:4171:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:4172:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:4172:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==RULE_ID||(LA52_0>=48 && LA52_0<=91)) ) {
                alt52=1;
            }
            else if ( (LA52_0==RULE_STRING) ) {
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
                    // InternalGaml.g:4173:6: lv_name_1_1= ruleValid_ID
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
                    // InternalGaml.g:4189:6: lv_name_1_2= RULE_STRING
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
    // InternalGaml.g:4210:1: entryRuleFunctionFacet returns [EObject current=null] : iv_ruleFunctionFacet= ruleFunctionFacet EOF ;
    public final EObject entryRuleFunctionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunctionFacet = null;


        try {
            // InternalGaml.g:4210:54: (iv_ruleFunctionFacet= ruleFunctionFacet EOF )
            // InternalGaml.g:4211:2: iv_ruleFunctionFacet= ruleFunctionFacet EOF
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
    // InternalGaml.g:4217:1: ruleFunctionFacet returns [EObject current=null] : ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) ;
    public final EObject ruleFunctionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_expr_1_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4223:2: ( ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) )
            // InternalGaml.g:4224:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            {
            // InternalGaml.g:4224:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            // InternalGaml.g:4225:3: ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            {
            // InternalGaml.g:4225:3: ( (lv_key_0_0= '->' ) )
            // InternalGaml.g:4226:4: (lv_key_0_0= '->' )
            {
            // InternalGaml.g:4226:4: (lv_key_0_0= '->' )
            // InternalGaml.g:4227:5: lv_key_0_0= '->'
            {
            lv_key_0_0=(Token)match(input,126,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:4239:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            int alt53=2;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // InternalGaml.g:4240:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    {
                    // InternalGaml.g:4240:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    // InternalGaml.g:4241:5: ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) )
                    {
                    // InternalGaml.g:4247:5: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:4248:6: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:4248:6: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:4249:7: lv_expr_1_0= ruleExpression
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
                    // InternalGaml.g:4268:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    {
                    // InternalGaml.g:4268:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    // InternalGaml.g:4269:5: otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}'
                    {
                    otherlv_2=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getFunctionFacetAccess().getLeftCurlyBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:4273:5: ( (lv_expr_3_0= ruleExpression ) )
                    // InternalGaml.g:4274:6: (lv_expr_3_0= ruleExpression )
                    {
                    // InternalGaml.g:4274:6: (lv_expr_3_0= ruleExpression )
                    // InternalGaml.g:4275:7: lv_expr_3_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getFunctionFacetAccess().getExprExpressionParserRuleCall_1_1_1_0());
                      						
                    }
                    pushFollow(FOLLOW_41);
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

                    otherlv_4=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4302:1: entryRuleTypeFacet returns [EObject current=null] : iv_ruleTypeFacet= ruleTypeFacet EOF ;
    public final EObject entryRuleTypeFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFacet = null;


        try {
            // InternalGaml.g:4302:50: (iv_ruleTypeFacet= ruleTypeFacet EOF )
            // InternalGaml.g:4303:2: iv_ruleTypeFacet= ruleTypeFacet EOF
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
    // InternalGaml.g:4309:1: ruleTypeFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) ;
    public final EObject ruleTypeFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4315:2: ( ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) )
            // InternalGaml.g:4316:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            {
            // InternalGaml.g:4316:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:4317:3: ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:4317:3: ( (lv_key_0_0= ruleTypeFacetKey ) )
            // InternalGaml.g:4318:4: (lv_key_0_0= ruleTypeFacetKey )
            {
            // InternalGaml.g:4318:4: (lv_key_0_0= ruleTypeFacetKey )
            // InternalGaml.g:4319:5: lv_key_0_0= ruleTypeFacetKey
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

            // InternalGaml.g:4336:3: ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            int alt54=2;
            alt54 = dfa54.predict(input);
            switch (alt54) {
                case 1 :
                    // InternalGaml.g:4337:4: ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    {
                    // InternalGaml.g:4337:4: ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    // InternalGaml.g:4338:5: ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) )
                    {
                    // InternalGaml.g:4339:5: ( (lv_expr_1_0= ruleTypeRef ) )
                    // InternalGaml.g:4340:6: (lv_expr_1_0= ruleTypeRef )
                    {
                    // InternalGaml.g:4340:6: (lv_expr_1_0= ruleTypeRef )
                    // InternalGaml.g:4341:7: lv_expr_1_0= ruleTypeRef
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
                    // InternalGaml.g:4360:4: ( (lv_expr_2_0= ruleExpression ) )
                    {
                    // InternalGaml.g:4360:4: ( (lv_expr_2_0= ruleExpression ) )
                    // InternalGaml.g:4361:5: (lv_expr_2_0= ruleExpression )
                    {
                    // InternalGaml.g:4361:5: (lv_expr_2_0= ruleExpression )
                    // InternalGaml.g:4362:6: lv_expr_2_0= ruleExpression
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
    // InternalGaml.g:4384:1: entryRuleActionFacet returns [EObject current=null] : iv_ruleActionFacet= ruleActionFacet EOF ;
    public final EObject entryRuleActionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFacet = null;


        try {
            // InternalGaml.g:4384:52: (iv_ruleActionFacet= ruleActionFacet EOF )
            // InternalGaml.g:4385:2: iv_ruleActionFacet= ruleActionFacet EOF
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
    // InternalGaml.g:4391:1: ruleActionFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) ;
    public final EObject ruleActionFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4397:2: ( ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) )
            // InternalGaml.g:4398:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            {
            // InternalGaml.g:4398:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            // InternalGaml.g:4399:3: ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            {
            // InternalGaml.g:4399:3: ( (lv_key_0_0= ruleActionFacetKey ) )
            // InternalGaml.g:4400:4: (lv_key_0_0= ruleActionFacetKey )
            {
            // InternalGaml.g:4400:4: (lv_key_0_0= ruleActionFacetKey )
            // InternalGaml.g:4401:5: lv_key_0_0= ruleActionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionFacetAccess().getKeyActionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_42);
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

            // InternalGaml.g:4418:3: ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==RULE_ID||(LA55_0>=48 && LA55_0<=91)) ) {
                alt55=1;
            }
            else if ( (LA55_0==39) ) {
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
                    // InternalGaml.g:4419:4: ( (lv_expr_1_0= ruleActionRef ) )
                    {
                    // InternalGaml.g:4419:4: ( (lv_expr_1_0= ruleActionRef ) )
                    // InternalGaml.g:4420:5: (lv_expr_1_0= ruleActionRef )
                    {
                    // InternalGaml.g:4420:5: (lv_expr_1_0= ruleActionRef )
                    // InternalGaml.g:4421:6: lv_expr_1_0= ruleActionRef
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
                    // InternalGaml.g:4439:4: ( (lv_block_2_0= ruleBlock ) )
                    {
                    // InternalGaml.g:4439:4: ( (lv_block_2_0= ruleBlock ) )
                    // InternalGaml.g:4440:5: (lv_block_2_0= ruleBlock )
                    {
                    // InternalGaml.g:4440:5: (lv_block_2_0= ruleBlock )
                    // InternalGaml.g:4441:6: lv_block_2_0= ruleBlock
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
    // InternalGaml.g:4463:1: entryRuleVarFacet returns [EObject current=null] : iv_ruleVarFacet= ruleVarFacet EOF ;
    public final EObject entryRuleVarFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFacet = null;


        try {
            // InternalGaml.g:4463:49: (iv_ruleVarFacet= ruleVarFacet EOF )
            // InternalGaml.g:4464:2: iv_ruleVarFacet= ruleVarFacet EOF
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
    // InternalGaml.g:4470:1: ruleVarFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) ;
    public final EObject ruleVarFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4476:2: ( ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) )
            // InternalGaml.g:4477:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            {
            // InternalGaml.g:4477:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            // InternalGaml.g:4478:3: ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) )
            {
            // InternalGaml.g:4478:3: ( (lv_key_0_0= ruleVarFacetKey ) )
            // InternalGaml.g:4479:4: (lv_key_0_0= ruleVarFacetKey )
            {
            // InternalGaml.g:4479:4: (lv_key_0_0= ruleVarFacetKey )
            // InternalGaml.g:4480:5: lv_key_0_0= ruleVarFacetKey
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

            // InternalGaml.g:4497:3: ( (lv_expr_1_0= ruleVariableRef ) )
            // InternalGaml.g:4498:4: (lv_expr_1_0= ruleVariableRef )
            {
            // InternalGaml.g:4498:4: (lv_expr_1_0= ruleVariableRef )
            // InternalGaml.g:4499:5: lv_expr_1_0= ruleVariableRef
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
    // InternalGaml.g:4520:1: entryRuleBlock returns [EObject current=null] : iv_ruleBlock= ruleBlock EOF ;
    public final EObject entryRuleBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBlock = null;


        try {
            // InternalGaml.g:4520:46: (iv_ruleBlock= ruleBlock EOF )
            // InternalGaml.g:4521:2: iv_ruleBlock= ruleBlock EOF
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
    // InternalGaml.g:4527:1: ruleBlock returns [EObject current=null] : ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) ;
    public final EObject ruleBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4533:2: ( ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) )
            // InternalGaml.g:4534:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            {
            // InternalGaml.g:4534:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            // InternalGaml.g:4535:3: () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:4535:3: ()
            // InternalGaml.g:4536:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,39,FOLLOW_43); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:4546:3: ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            // InternalGaml.g:4547:4: ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}'
            {
            // InternalGaml.g:4547:4: ( (lv_statements_2_0= ruleStatement ) )*
            loop56:
            do {
                int alt56=2;
                int LA56_0 = input.LA(1);

                if ( ((LA56_0>=RULE_ID && LA56_0<=RULE_KEYWORD)||LA56_0==20||(LA56_0>=25 && LA56_0<=28)||LA56_0==30||LA56_0==32||LA56_0==35||(LA56_0>=37 && LA56_0<=39)||LA56_0==42||(LA56_0>=47 && LA56_0<=91)||LA56_0==136||(LA56_0>=140 && LA56_0<=142)) ) {
                    alt56=1;
                }


                switch (alt56) {
            	case 1 :
            	    // InternalGaml.g:4548:5: (lv_statements_2_0= ruleStatement )
            	    {
            	    // InternalGaml.g:4548:5: (lv_statements_2_0= ruleStatement )
            	    // InternalGaml.g:4549:6: lv_statements_2_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBlockAccess().getStatementsStatementParserRuleCall_2_0_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_43);
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
            	    break loop56;
                }
            } while (true);

            otherlv_3=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4575:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalGaml.g:4575:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalGaml.g:4576:2: iv_ruleExpression= ruleExpression EOF
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
    // InternalGaml.g:4582:1: ruleExpression returns [EObject current=null] : this_Pair_0= rulePair ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        EObject this_Pair_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4588:2: (this_Pair_0= rulePair )
            // InternalGaml.g:4589:2: this_Pair_0= rulePair
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
    // InternalGaml.g:4600:1: entryRulePair returns [EObject current=null] : iv_rulePair= rulePair EOF ;
    public final EObject entryRulePair() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePair = null;


        try {
            // InternalGaml.g:4600:45: (iv_rulePair= rulePair EOF )
            // InternalGaml.g:4601:2: iv_rulePair= rulePair EOF
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
    // InternalGaml.g:4607:1: rulePair returns [EObject current=null] : (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) ;
    public final EObject rulePair() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_If_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4613:2: ( (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) )
            // InternalGaml.g:4614:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            {
            // InternalGaml.g:4614:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            // InternalGaml.g:4615:3: this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getPairAccess().getIfParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_44);
            this_If_0=ruleIf();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_If_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4623:3: ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==127) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // InternalGaml.g:4624:4: () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) )
                    {
                    // InternalGaml.g:4624:4: ()
                    // InternalGaml.g:4625:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getPairAccess().getBinaryOperatorLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4631:4: ( (lv_op_2_0= '::' ) )
                    // InternalGaml.g:4632:5: (lv_op_2_0= '::' )
                    {
                    // InternalGaml.g:4632:5: (lv_op_2_0= '::' )
                    // InternalGaml.g:4633:6: lv_op_2_0= '::'
                    {
                    lv_op_2_0=(Token)match(input,127,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:4645:4: ( (lv_right_3_0= ruleIf ) )
                    // InternalGaml.g:4646:5: (lv_right_3_0= ruleIf )
                    {
                    // InternalGaml.g:4646:5: (lv_right_3_0= ruleIf )
                    // InternalGaml.g:4647:6: lv_right_3_0= ruleIf
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
    // InternalGaml.g:4669:1: entryRuleIf returns [EObject current=null] : iv_ruleIf= ruleIf EOF ;
    public final EObject entryRuleIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIf = null;


        try {
            // InternalGaml.g:4669:43: (iv_ruleIf= ruleIf EOF )
            // InternalGaml.g:4670:2: iv_ruleIf= ruleIf EOF
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
    // InternalGaml.g:4676:1: ruleIf returns [EObject current=null] : (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) ;
    public final EObject ruleIf() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        EObject this_Or_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_ifFalse_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4682:2: ( (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) )
            // InternalGaml.g:4683:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            {
            // InternalGaml.g:4683:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            // InternalGaml.g:4684:3: this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getIfAccess().getOrParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_45);
            this_Or_0=ruleOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Or_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4692:3: ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==128) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // InternalGaml.g:4693:4: () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    {
                    // InternalGaml.g:4693:4: ()
                    // InternalGaml.g:4694:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getIfAccess().getIfLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4700:4: ( (lv_op_2_0= '?' ) )
                    // InternalGaml.g:4701:5: (lv_op_2_0= '?' )
                    {
                    // InternalGaml.g:4701:5: (lv_op_2_0= '?' )
                    // InternalGaml.g:4702:6: lv_op_2_0= '?'
                    {
                    lv_op_2_0=(Token)match(input,128,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:4714:4: ( (lv_right_3_0= ruleOr ) )
                    // InternalGaml.g:4715:5: (lv_right_3_0= ruleOr )
                    {
                    // InternalGaml.g:4715:5: (lv_right_3_0= ruleOr )
                    // InternalGaml.g:4716:6: lv_right_3_0= ruleOr
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getIfAccess().getRightOrParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_27);
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

                    // InternalGaml.g:4733:4: (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    // InternalGaml.g:4734:5: otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) )
                    {
                    otherlv_4=(Token)match(input,34,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getIfAccess().getColonKeyword_1_3_0());
                      				
                    }
                    // InternalGaml.g:4738:5: ( (lv_ifFalse_5_0= ruleOr ) )
                    // InternalGaml.g:4739:6: (lv_ifFalse_5_0= ruleOr )
                    {
                    // InternalGaml.g:4739:6: (lv_ifFalse_5_0= ruleOr )
                    // InternalGaml.g:4740:7: lv_ifFalse_5_0= ruleOr
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
    // InternalGaml.g:4763:1: entryRuleOr returns [EObject current=null] : iv_ruleOr= ruleOr EOF ;
    public final EObject entryRuleOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOr = null;


        try {
            // InternalGaml.g:4763:43: (iv_ruleOr= ruleOr EOF )
            // InternalGaml.g:4764:2: iv_ruleOr= ruleOr EOF
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
    // InternalGaml.g:4770:1: ruleOr returns [EObject current=null] : (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) ;
    public final EObject ruleOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_And_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4776:2: ( (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) )
            // InternalGaml.g:4777:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            {
            // InternalGaml.g:4777:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            // InternalGaml.g:4778:3: this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getOrAccess().getAndParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_46);
            this_And_0=ruleAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_And_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4786:3: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==129) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // InternalGaml.g:4787:4: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) )
            	    {
            	    // InternalGaml.g:4787:4: ()
            	    // InternalGaml.g:4788:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getOrAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4794:4: ( (lv_op_2_0= 'or' ) )
            	    // InternalGaml.g:4795:5: (lv_op_2_0= 'or' )
            	    {
            	    // InternalGaml.g:4795:5: (lv_op_2_0= 'or' )
            	    // InternalGaml.g:4796:6: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,129,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4808:4: ( (lv_right_3_0= ruleAnd ) )
            	    // InternalGaml.g:4809:5: (lv_right_3_0= ruleAnd )
            	    {
            	    // InternalGaml.g:4809:5: (lv_right_3_0= ruleAnd )
            	    // InternalGaml.g:4810:6: lv_right_3_0= ruleAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOrAccess().getRightAndParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_46);
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
    // $ANTLR end "ruleOr"


    // $ANTLR start "entryRuleAnd"
    // InternalGaml.g:4832:1: entryRuleAnd returns [EObject current=null] : iv_ruleAnd= ruleAnd EOF ;
    public final EObject entryRuleAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAnd = null;


        try {
            // InternalGaml.g:4832:44: (iv_ruleAnd= ruleAnd EOF )
            // InternalGaml.g:4833:2: iv_ruleAnd= ruleAnd EOF
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
    // InternalGaml.g:4839:1: ruleAnd returns [EObject current=null] : (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) ;
    public final EObject ruleAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Cast_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4845:2: ( (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) )
            // InternalGaml.g:4846:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            {
            // InternalGaml.g:4846:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            // InternalGaml.g:4847:3: this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAndAccess().getCastParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_47);
            this_Cast_0=ruleCast();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Cast_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4855:3: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==130) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // InternalGaml.g:4856:4: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) )
            	    {
            	    // InternalGaml.g:4856:4: ()
            	    // InternalGaml.g:4857:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAndAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4863:4: ( (lv_op_2_0= 'and' ) )
            	    // InternalGaml.g:4864:5: (lv_op_2_0= 'and' )
            	    {
            	    // InternalGaml.g:4864:5: (lv_op_2_0= 'and' )
            	    // InternalGaml.g:4865:6: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,130,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4877:4: ( (lv_right_3_0= ruleCast ) )
            	    // InternalGaml.g:4878:5: (lv_right_3_0= ruleCast )
            	    {
            	    // InternalGaml.g:4878:5: (lv_right_3_0= ruleCast )
            	    // InternalGaml.g:4879:6: lv_right_3_0= ruleCast
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAndAccess().getRightCastParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_47);
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
            	    break loop60;
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
    // InternalGaml.g:4901:1: entryRuleCast returns [EObject current=null] : iv_ruleCast= ruleCast EOF ;
    public final EObject entryRuleCast() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCast = null;


        try {
            // InternalGaml.g:4901:45: (iv_ruleCast= ruleCast EOF )
            // InternalGaml.g:4902:2: iv_ruleCast= ruleCast EOF
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
    // InternalGaml.g:4908:1: ruleCast returns [EObject current=null] : (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) ;
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
            // InternalGaml.g:4914:2: ( (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) )
            // InternalGaml.g:4915:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            {
            // InternalGaml.g:4915:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            // InternalGaml.g:4916:3: this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
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
            // InternalGaml.g:4924:3: ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==18) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // InternalGaml.g:4925:4: ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    {
                    // InternalGaml.g:4925:4: ( () ( (lv_op_2_0= 'as' ) ) )
                    // InternalGaml.g:4926:5: () ( (lv_op_2_0= 'as' ) )
                    {
                    // InternalGaml.g:4926:5: ()
                    // InternalGaml.g:4927:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getCastAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4933:5: ( (lv_op_2_0= 'as' ) )
                    // InternalGaml.g:4934:6: (lv_op_2_0= 'as' )
                    {
                    // InternalGaml.g:4934:6: (lv_op_2_0= 'as' )
                    // InternalGaml.g:4935:7: lv_op_2_0= 'as'
                    {
                    lv_op_2_0=(Token)match(input,18,FOLLOW_48); if (state.failed) return current;
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

                    // InternalGaml.g:4948:4: ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    int alt61=2;
                    int LA61_0 = input.LA(1);

                    if ( (LA61_0==RULE_ID||LA61_0==48) ) {
                        alt61=1;
                    }
                    else if ( (LA61_0==35) ) {
                        alt61=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 61, 0, input);

                        throw nvae;
                    }
                    switch (alt61) {
                        case 1 :
                            // InternalGaml.g:4949:5: ( (lv_right_3_0= ruleTypeRef ) )
                            {
                            // InternalGaml.g:4949:5: ( (lv_right_3_0= ruleTypeRef ) )
                            // InternalGaml.g:4950:6: (lv_right_3_0= ruleTypeRef )
                            {
                            // InternalGaml.g:4950:6: (lv_right_3_0= ruleTypeRef )
                            // InternalGaml.g:4951:7: lv_right_3_0= ruleTypeRef
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
                            // InternalGaml.g:4969:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            {
                            // InternalGaml.g:4969:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            // InternalGaml.g:4970:6: otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')'
                            {
                            otherlv_4=(Token)match(input,35,FOLLOW_29); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(otherlv_4, grammarAccess.getCastAccess().getLeftParenthesisKeyword_1_1_1_0());
                              					
                            }
                            // InternalGaml.g:4974:6: ( (lv_right_5_0= ruleTypeRef ) )
                            // InternalGaml.g:4975:7: (lv_right_5_0= ruleTypeRef )
                            {
                            // InternalGaml.g:4975:7: (lv_right_5_0= ruleTypeRef )
                            // InternalGaml.g:4976:8: lv_right_5_0= ruleTypeRef
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getCastAccess().getRightTypeRefParserRuleCall_1_1_1_1_0());
                              							
                            }
                            pushFollow(FOLLOW_30);
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

                            otherlv_6=(Token)match(input,36,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5004:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
    public final EObject entryRuleComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComparison = null;


        try {
            // InternalGaml.g:5004:51: (iv_ruleComparison= ruleComparison EOF )
            // InternalGaml.g:5005:2: iv_ruleComparison= ruleComparison EOF
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
    // InternalGaml.g:5011:1: ruleComparison returns [EObject current=null] : (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) ;
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
            // InternalGaml.g:5017:2: ( (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) )
            // InternalGaml.g:5018:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            {
            // InternalGaml.g:5018:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            // InternalGaml.g:5019:3: this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getComparisonAccess().getAdditionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_49);
            this_Addition_0=ruleAddition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Addition_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5027:3: ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==41||(LA64_0>=131 && LA64_0<=134)) ) {
                alt64=1;
            }
            else if ( (LA64_0==93) ) {
                int LA64_2 = input.LA(2);

                if ( ((LA64_2>=RULE_ID && LA64_2<=RULE_KEYWORD)||LA64_2==20||LA64_2==35||LA64_2==39||(LA64_2>=48 && LA64_2<=91)||LA64_2==136||(LA64_2>=140 && LA64_2<=142)) ) {
                    alt64=1;
                }
            }
            switch (alt64) {
                case 1 :
                    // InternalGaml.g:5028:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) )
                    {
                    // InternalGaml.g:5028:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) )
                    // InternalGaml.g:5029:5: () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    {
                    // InternalGaml.g:5029:5: ()
                    // InternalGaml.g:5030:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getComparisonAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:5036:5: ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    // InternalGaml.g:5037:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    {
                    // InternalGaml.g:5037:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    // InternalGaml.g:5038:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    {
                    // InternalGaml.g:5038:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    int alt63=6;
                    switch ( input.LA(1) ) {
                    case 131:
                        {
                        alt63=1;
                        }
                        break;
                    case 41:
                        {
                        alt63=2;
                        }
                        break;
                    case 132:
                        {
                        alt63=3;
                        }
                        break;
                    case 133:
                        {
                        alt63=4;
                        }
                        break;
                    case 134:
                        {
                        alt63=5;
                        }
                        break;
                    case 93:
                        {
                        alt63=6;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 63, 0, input);

                        throw nvae;
                    }

                    switch (alt63) {
                        case 1 :
                            // InternalGaml.g:5039:8: lv_op_2_1= '!='
                            {
                            lv_op_2_1=(Token)match(input,131,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:5050:8: lv_op_2_2= '='
                            {
                            lv_op_2_2=(Token)match(input,41,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:5061:8: lv_op_2_3= '>='
                            {
                            lv_op_2_3=(Token)match(input,132,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:5072:8: lv_op_2_4= '<='
                            {
                            lv_op_2_4=(Token)match(input,133,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:5083:8: lv_op_2_5= '<'
                            {
                            lv_op_2_5=(Token)match(input,134,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:5094:8: lv_op_2_6= '>'
                            {
                            lv_op_2_6=(Token)match(input,93,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:5108:4: ( (lv_right_3_0= ruleAddition ) )
                    // InternalGaml.g:5109:5: (lv_right_3_0= ruleAddition )
                    {
                    // InternalGaml.g:5109:5: (lv_right_3_0= ruleAddition )
                    // InternalGaml.g:5110:6: lv_right_3_0= ruleAddition
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
    // InternalGaml.g:5132:1: entryRuleAddition returns [EObject current=null] : iv_ruleAddition= ruleAddition EOF ;
    public final EObject entryRuleAddition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddition = null;


        try {
            // InternalGaml.g:5132:49: (iv_ruleAddition= ruleAddition EOF )
            // InternalGaml.g:5133:2: iv_ruleAddition= ruleAddition EOF
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
    // InternalGaml.g:5139:1: ruleAddition returns [EObject current=null] : (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) ;
    public final EObject ruleAddition() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_Multiplication_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5145:2: ( (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) )
            // InternalGaml.g:5146:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            {
            // InternalGaml.g:5146:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            // InternalGaml.g:5147:3: this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAdditionAccess().getMultiplicationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_Multiplication_0=ruleMultiplication();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Multiplication_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5155:3: ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            loop66:
            do {
                int alt66=2;
                int LA66_0 = input.LA(1);

                if ( ((LA66_0>=135 && LA66_0<=136)) ) {
                    alt66=1;
                }


                switch (alt66) {
            	case 1 :
            	    // InternalGaml.g:5156:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) )
            	    {
            	    // InternalGaml.g:5156:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) )
            	    // InternalGaml.g:5157:5: () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    {
            	    // InternalGaml.g:5157:5: ()
            	    // InternalGaml.g:5158:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getAdditionAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5164:5: ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    // InternalGaml.g:5165:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    {
            	    // InternalGaml.g:5165:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    // InternalGaml.g:5166:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    {
            	    // InternalGaml.g:5166:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    int alt65=2;
            	    int LA65_0 = input.LA(1);

            	    if ( (LA65_0==135) ) {
            	        alt65=1;
            	    }
            	    else if ( (LA65_0==136) ) {
            	        alt65=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 65, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt65) {
            	        case 1 :
            	            // InternalGaml.g:5167:8: lv_op_2_1= '+'
            	            {
            	            lv_op_2_1=(Token)match(input,135,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:5178:8: lv_op_2_2= '-'
            	            {
            	            lv_op_2_2=(Token)match(input,136,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:5192:4: ( (lv_right_3_0= ruleMultiplication ) )
            	    // InternalGaml.g:5193:5: (lv_right_3_0= ruleMultiplication )
            	    {
            	    // InternalGaml.g:5193:5: (lv_right_3_0= ruleMultiplication )
            	    // InternalGaml.g:5194:6: lv_right_3_0= ruleMultiplication
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAdditionAccess().getRightMultiplicationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_50);
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
            	    break loop66;
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
    // InternalGaml.g:5216:1: entryRuleMultiplication returns [EObject current=null] : iv_ruleMultiplication= ruleMultiplication EOF ;
    public final EObject entryRuleMultiplication() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMultiplication = null;


        try {
            // InternalGaml.g:5216:55: (iv_ruleMultiplication= ruleMultiplication EOF )
            // InternalGaml.g:5217:2: iv_ruleMultiplication= ruleMultiplication EOF
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
    // InternalGaml.g:5223:1: ruleMultiplication returns [EObject current=null] : (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) ;
    public final EObject ruleMultiplication() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        EObject this_Binary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5229:2: ( (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) )
            // InternalGaml.g:5230:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            {
            // InternalGaml.g:5230:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            // InternalGaml.g:5231:3: this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getMultiplicationAccess().getBinaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_Binary_0=ruleBinary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Binary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5239:3: ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( ((LA68_0>=137 && LA68_0<=139)) ) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // InternalGaml.g:5240:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) )
            	    {
            	    // InternalGaml.g:5240:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) )
            	    // InternalGaml.g:5241:5: () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    {
            	    // InternalGaml.g:5241:5: ()
            	    // InternalGaml.g:5242:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getMultiplicationAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5248:5: ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    // InternalGaml.g:5249:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    {
            	    // InternalGaml.g:5249:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    // InternalGaml.g:5250:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    {
            	    // InternalGaml.g:5250:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    int alt67=3;
            	    switch ( input.LA(1) ) {
            	    case 137:
            	        {
            	        alt67=1;
            	        }
            	        break;
            	    case 138:
            	        {
            	        alt67=2;
            	        }
            	        break;
            	    case 139:
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
            	            // InternalGaml.g:5251:8: lv_op_2_1= '*'
            	            {
            	            lv_op_2_1=(Token)match(input,137,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:5262:8: lv_op_2_2= '/'
            	            {
            	            lv_op_2_2=(Token)match(input,138,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:5273:8: lv_op_2_3= '^'
            	            {
            	            lv_op_2_3=(Token)match(input,139,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:5287:4: ( (lv_right_3_0= ruleBinary ) )
            	    // InternalGaml.g:5288:5: (lv_right_3_0= ruleBinary )
            	    {
            	    // InternalGaml.g:5288:5: (lv_right_3_0= ruleBinary )
            	    // InternalGaml.g:5289:6: lv_right_3_0= ruleBinary
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getMultiplicationAccess().getRightBinaryParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_51);
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
            	    break loop68;
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
    // InternalGaml.g:5311:1: entryRuleBinary returns [EObject current=null] : iv_ruleBinary= ruleBinary EOF ;
    public final EObject entryRuleBinary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBinary = null;


        try {
            // InternalGaml.g:5311:47: (iv_ruleBinary= ruleBinary EOF )
            // InternalGaml.g:5312:2: iv_ruleBinary= ruleBinary EOF
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
    // InternalGaml.g:5318:1: ruleBinary returns [EObject current=null] : (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) ;
    public final EObject ruleBinary() throws RecognitionException {
        EObject current = null;

        EObject this_Unit_0 = null;

        AntlrDatatypeRuleToken lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5324:2: ( (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) )
            // InternalGaml.g:5325:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            {
            // InternalGaml.g:5325:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            // InternalGaml.g:5326:3: this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getBinaryAccess().getUnitParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_Unit_0=ruleUnit();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unit_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5334:3: ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            loop69:
            do {
                int alt69=2;
                int LA69_0 = input.LA(1);

                if ( (LA69_0==RULE_ID) ) {
                    int LA69_2 = input.LA(2);

                    if ( ((LA69_2>=RULE_ID && LA69_2<=RULE_KEYWORD)||LA69_2==20||LA69_2==35||LA69_2==39||(LA69_2>=48 && LA69_2<=91)||LA69_2==136||(LA69_2>=140 && LA69_2<=142)) ) {
                        alt69=1;
                    }


                }
                else if ( ((LA69_0>=48 && LA69_0<=91)) ) {
                    alt69=1;
                }


                switch (alt69) {
            	case 1 :
            	    // InternalGaml.g:5335:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) )
            	    {
            	    // InternalGaml.g:5335:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) )
            	    // InternalGaml.g:5336:5: () ( (lv_op_2_0= ruleValid_ID ) )
            	    {
            	    // InternalGaml.g:5336:5: ()
            	    // InternalGaml.g:5337:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getBinaryAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:5343:5: ( (lv_op_2_0= ruleValid_ID ) )
            	    // InternalGaml.g:5344:6: (lv_op_2_0= ruleValid_ID )
            	    {
            	    // InternalGaml.g:5344:6: (lv_op_2_0= ruleValid_ID )
            	    // InternalGaml.g:5345:7: lv_op_2_0= ruleValid_ID
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

            	    // InternalGaml.g:5363:4: ( (lv_right_3_0= ruleUnit ) )
            	    // InternalGaml.g:5364:5: (lv_right_3_0= ruleUnit )
            	    {
            	    // InternalGaml.g:5364:5: (lv_right_3_0= ruleUnit )
            	    // InternalGaml.g:5365:6: lv_right_3_0= ruleUnit
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBinaryAccess().getRightUnitParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_52);
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
            	    break loop69;
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
    // InternalGaml.g:5387:1: entryRuleUnit returns [EObject current=null] : iv_ruleUnit= ruleUnit EOF ;
    public final EObject entryRuleUnit() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnit = null;


        try {
            // InternalGaml.g:5387:45: (iv_ruleUnit= ruleUnit EOF )
            // InternalGaml.g:5388:2: iv_ruleUnit= ruleUnit EOF
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
    // InternalGaml.g:5394:1: ruleUnit returns [EObject current=null] : (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) ;
    public final EObject ruleUnit() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Unary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5400:2: ( (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) )
            // InternalGaml.g:5401:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            {
            // InternalGaml.g:5401:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            // InternalGaml.g:5402:3: this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getUnitAccess().getUnaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_53);
            this_Unary_0=ruleUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5410:3: ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==140) ) {
                alt70=1;
            }
            switch (alt70) {
                case 1 :
                    // InternalGaml.g:5411:4: ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) )
                    {
                    // InternalGaml.g:5411:4: ( () ( (lv_op_2_0= '#' ) ) )
                    // InternalGaml.g:5412:5: () ( (lv_op_2_0= '#' ) )
                    {
                    // InternalGaml.g:5412:5: ()
                    // InternalGaml.g:5413:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getUnitAccess().getUnitLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:5419:5: ( (lv_op_2_0= '#' ) )
                    // InternalGaml.g:5420:6: (lv_op_2_0= '#' )
                    {
                    // InternalGaml.g:5420:6: (lv_op_2_0= '#' )
                    // InternalGaml.g:5421:7: lv_op_2_0= '#'
                    {
                    lv_op_2_0=(Token)match(input,140,FOLLOW_12); if (state.failed) return current;
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

                    // InternalGaml.g:5434:4: ( (lv_right_3_0= ruleUnitRef ) )
                    // InternalGaml.g:5435:5: (lv_right_3_0= ruleUnitRef )
                    {
                    // InternalGaml.g:5435:5: (lv_right_3_0= ruleUnitRef )
                    // InternalGaml.g:5436:6: lv_right_3_0= ruleUnitRef
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
    // InternalGaml.g:5458:1: entryRuleUnary returns [EObject current=null] : iv_ruleUnary= ruleUnary EOF ;
    public final EObject entryRuleUnary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnary = null;


        try {
            // InternalGaml.g:5458:46: (iv_ruleUnary= ruleUnary EOF )
            // InternalGaml.g:5459:2: iv_ruleUnary= ruleUnary EOF
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
    // InternalGaml.g:5465:1: ruleUnary returns [EObject current=null] : (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) ;
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
            // InternalGaml.g:5471:2: ( (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) )
            // InternalGaml.g:5472:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            {
            // InternalGaml.g:5472:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( ((LA73_0>=RULE_ID && LA73_0<=RULE_KEYWORD)||LA73_0==20||LA73_0==35||LA73_0==39||(LA73_0>=48 && LA73_0<=91)) ) {
                alt73=1;
            }
            else if ( (LA73_0==136||(LA73_0>=140 && LA73_0<=142)) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }
            switch (alt73) {
                case 1 :
                    // InternalGaml.g:5473:3: this_Access_0= ruleAccess
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
                    // InternalGaml.g:5482:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    {
                    // InternalGaml.g:5482:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    // InternalGaml.g:5483:4: () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    {
                    // InternalGaml.g:5483:4: ()
                    // InternalGaml.g:5484:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getUnaryAccess().getUnaryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5490:4: ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    int alt72=2;
                    int LA72_0 = input.LA(1);

                    if ( (LA72_0==140) ) {
                        alt72=1;
                    }
                    else if ( (LA72_0==136||(LA72_0>=141 && LA72_0<=142)) ) {
                        alt72=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 72, 0, input);

                        throw nvae;
                    }
                    switch (alt72) {
                        case 1 :
                            // InternalGaml.g:5491:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            {
                            // InternalGaml.g:5491:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            // InternalGaml.g:5492:6: ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) )
                            {
                            // InternalGaml.g:5492:6: ( (lv_op_2_0= '#' ) )
                            // InternalGaml.g:5493:7: (lv_op_2_0= '#' )
                            {
                            // InternalGaml.g:5493:7: (lv_op_2_0= '#' )
                            // InternalGaml.g:5494:8: lv_op_2_0= '#'
                            {
                            lv_op_2_0=(Token)match(input,140,FOLLOW_12); if (state.failed) return current;
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

                            // InternalGaml.g:5506:6: ( (lv_right_3_0= ruleUnitRef ) )
                            // InternalGaml.g:5507:7: (lv_right_3_0= ruleUnitRef )
                            {
                            // InternalGaml.g:5507:7: (lv_right_3_0= ruleUnitRef )
                            // InternalGaml.g:5508:8: lv_right_3_0= ruleUnitRef
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
                            // InternalGaml.g:5527:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            {
                            // InternalGaml.g:5527:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            // InternalGaml.g:5528:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) )
                            {
                            // InternalGaml.g:5528:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) )
                            // InternalGaml.g:5529:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            {
                            // InternalGaml.g:5529:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            // InternalGaml.g:5530:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            {
                            // InternalGaml.g:5530:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            int alt71=3;
                            switch ( input.LA(1) ) {
                            case 136:
                                {
                                alt71=1;
                                }
                                break;
                            case 141:
                                {
                                alt71=2;
                                }
                                break;
                            case 142:
                                {
                                alt71=3;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return current;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 71, 0, input);

                                throw nvae;
                            }

                            switch (alt71) {
                                case 1 :
                                    // InternalGaml.g:5531:9: lv_op_4_1= '-'
                                    {
                                    lv_op_4_1=(Token)match(input,136,FOLLOW_5); if (state.failed) return current;
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
                                    // InternalGaml.g:5542:9: lv_op_4_2= '!'
                                    {
                                    lv_op_4_2=(Token)match(input,141,FOLLOW_5); if (state.failed) return current;
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
                                    // InternalGaml.g:5553:9: lv_op_4_3= 'not'
                                    {
                                    lv_op_4_3=(Token)match(input,142,FOLLOW_5); if (state.failed) return current;
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

                            // InternalGaml.g:5566:6: ( (lv_right_5_0= ruleUnary ) )
                            // InternalGaml.g:5567:7: (lv_right_5_0= ruleUnary )
                            {
                            // InternalGaml.g:5567:7: (lv_right_5_0= ruleUnary )
                            // InternalGaml.g:5568:8: lv_right_5_0= ruleUnary
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
    // InternalGaml.g:5592:1: entryRuleAccess returns [EObject current=null] : iv_ruleAccess= ruleAccess EOF ;
    public final EObject entryRuleAccess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAccess = null;


        try {
            // InternalGaml.g:5592:47: (iv_ruleAccess= ruleAccess EOF )
            // InternalGaml.g:5593:2: iv_ruleAccess= ruleAccess EOF
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
    // InternalGaml.g:5599:1: ruleAccess returns [EObject current=null] : (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) ;
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
            // InternalGaml.g:5605:2: ( (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) )
            // InternalGaml.g:5606:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            {
            // InternalGaml.g:5606:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            // InternalGaml.g:5607:3: this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAccessAccess().getPrimaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_54);
            this_Primary_0=rulePrimary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Primary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:5615:3: ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            loop76:
            do {
                int alt76=2;
                int LA76_0 = input.LA(1);

                if ( (LA76_0==20||LA76_0==143) ) {
                    alt76=1;
                }


                switch (alt76) {
            	case 1 :
            	    // InternalGaml.g:5616:4: () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    {
            	    // InternalGaml.g:5616:4: ()
            	    // InternalGaml.g:5617:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAccessAccess().getAccessLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:5623:4: ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    int alt75=2;
            	    int LA75_0 = input.LA(1);

            	    if ( (LA75_0==20) ) {
            	        alt75=1;
            	    }
            	    else if ( (LA75_0==143) ) {
            	        alt75=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 75, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt75) {
            	        case 1 :
            	            // InternalGaml.g:5624:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            {
            	            // InternalGaml.g:5624:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            // InternalGaml.g:5625:6: ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']'
            	            {
            	            // InternalGaml.g:5625:6: ( (lv_op_2_0= '[' ) )
            	            // InternalGaml.g:5626:7: (lv_op_2_0= '[' )
            	            {
            	            // InternalGaml.g:5626:7: (lv_op_2_0= '[' )
            	            // InternalGaml.g:5627:8: lv_op_2_0= '['
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

            	            // InternalGaml.g:5639:6: ( (lv_right_3_0= ruleExpressionList ) )?
            	            int alt74=2;
            	            int LA74_0 = input.LA(1);

            	            if ( ((LA74_0>=RULE_ID && LA74_0<=RULE_KEYWORD)||LA74_0==20||LA74_0==33||LA74_0==35||LA74_0==39||(LA74_0>=48 && LA74_0<=91)||(LA74_0>=99 && LA74_0<=125)||LA74_0==136||(LA74_0>=140 && LA74_0<=142)) ) {
            	                alt74=1;
            	            }
            	            switch (alt74) {
            	                case 1 :
            	                    // InternalGaml.g:5640:7: (lv_right_3_0= ruleExpressionList )
            	                    {
            	                    // InternalGaml.g:5640:7: (lv_right_3_0= ruleExpressionList )
            	                    // InternalGaml.g:5641:8: lv_right_3_0= ruleExpressionList
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

            	            otherlv_4=(Token)match(input,21,FOLLOW_54); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						newLeafNode(otherlv_4, grammarAccess.getAccessAccess().getRightSquareBracketKeyword_1_1_0_2());
            	              					
            	            }

            	            }


            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:5664:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            {
            	            // InternalGaml.g:5664:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            // InternalGaml.g:5665:6: ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) )
            	            {
            	            // InternalGaml.g:5665:6: ( (lv_op_5_0= '.' ) )
            	            // InternalGaml.g:5666:7: (lv_op_5_0= '.' )
            	            {
            	            // InternalGaml.g:5666:7: (lv_op_5_0= '.' )
            	            // InternalGaml.g:5667:8: lv_op_5_0= '.'
            	            {
            	            lv_op_5_0=(Token)match(input,143,FOLLOW_55); if (state.failed) return current;
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

            	            // InternalGaml.g:5679:6: ( (lv_right_6_0= rulePrimary ) )
            	            // InternalGaml.g:5680:7: (lv_right_6_0= rulePrimary )
            	            {
            	            // InternalGaml.g:5680:7: (lv_right_6_0= rulePrimary )
            	            // InternalGaml.g:5681:8: lv_right_6_0= rulePrimary
            	            {
            	            if ( state.backtracking==0 ) {

            	              								newCompositeNode(grammarAccess.getAccessAccess().getRightPrimaryParserRuleCall_1_1_1_1_0());
            	              							
            	            }
            	            pushFollow(FOLLOW_54);
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
    // $ANTLR end "ruleAccess"


    // $ANTLR start "entryRulePrimary"
    // InternalGaml.g:5705:1: entryRulePrimary returns [EObject current=null] : iv_rulePrimary= rulePrimary EOF ;
    public final EObject entryRulePrimary() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePrimary = null;


        try {
            // InternalGaml.g:5705:48: (iv_rulePrimary= rulePrimary EOF )
            // InternalGaml.g:5706:2: iv_rulePrimary= rulePrimary EOF
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
    // InternalGaml.g:5712:1: rulePrimary returns [EObject current=null] : (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) ;
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
            // InternalGaml.g:5718:2: ( (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) )
            // InternalGaml.g:5719:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            {
            // InternalGaml.g:5719:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            int alt79=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
            case RULE_INTEGER:
            case RULE_DOUBLE:
            case RULE_BOOLEAN:
            case RULE_KEYWORD:
                {
                alt79=1;
                }
                break;
            case RULE_ID:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
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
                {
                alt79=2;
                }
                break;
            case 35:
                {
                alt79=3;
                }
                break;
            case 20:
                {
                alt79=4;
                }
                break;
            case 39:
                {
                alt79=5;
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
                    // InternalGaml.g:5720:3: this_TerminalExpression_0= ruleTerminalExpression
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
                    // InternalGaml.g:5729:3: this_AbstractRef_1= ruleAbstractRef
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
                    // InternalGaml.g:5738:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    {
                    // InternalGaml.g:5738:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    // InternalGaml.g:5739:4: otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,35,FOLLOW_56); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getPrimaryAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getPrimaryAccess().getExpressionListParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_30);
                    this_ExpressionList_3=ruleExpressionList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ExpressionList_3;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_4=(Token)match(input,36,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getPrimaryAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:5757:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    {
                    // InternalGaml.g:5757:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    // InternalGaml.g:5758:4: otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']'
                    {
                    otherlv_5=(Token)match(input,20,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getPrimaryAccess().getLeftSquareBracketKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:5762:4: ()
                    // InternalGaml.g:5763:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getArrayAction_3_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5769:4: ( (lv_exprs_7_0= ruleExpressionList ) )?
                    int alt77=2;
                    int LA77_0 = input.LA(1);

                    if ( ((LA77_0>=RULE_ID && LA77_0<=RULE_KEYWORD)||LA77_0==20||LA77_0==33||LA77_0==35||LA77_0==39||(LA77_0>=48 && LA77_0<=91)||(LA77_0>=99 && LA77_0<=125)||LA77_0==136||(LA77_0>=140 && LA77_0<=142)) ) {
                        alt77=1;
                    }
                    switch (alt77) {
                        case 1 :
                            // InternalGaml.g:5770:5: (lv_exprs_7_0= ruleExpressionList )
                            {
                            // InternalGaml.g:5770:5: (lv_exprs_7_0= ruleExpressionList )
                            // InternalGaml.g:5771:6: lv_exprs_7_0= ruleExpressionList
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
                    // InternalGaml.g:5794:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    {
                    // InternalGaml.g:5794:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    // InternalGaml.g:5795:4: otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}'
                    {
                    otherlv_9=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getPrimaryAccess().getLeftCurlyBracketKeyword_4_0());
                      			
                    }
                    // InternalGaml.g:5799:4: ()
                    // InternalGaml.g:5800:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getPointAction_4_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5806:4: ( (lv_left_11_0= ruleExpression ) )
                    // InternalGaml.g:5807:5: (lv_left_11_0= ruleExpression )
                    {
                    // InternalGaml.g:5807:5: (lv_left_11_0= ruleExpression )
                    // InternalGaml.g:5808:6: lv_left_11_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getLeftExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_57);
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

                    // InternalGaml.g:5825:4: ( (lv_op_12_0= ',' ) )
                    // InternalGaml.g:5826:5: (lv_op_12_0= ',' )
                    {
                    // InternalGaml.g:5826:5: (lv_op_12_0= ',' )
                    // InternalGaml.g:5827:6: lv_op_12_0= ','
                    {
                    lv_op_12_0=(Token)match(input,98,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:5839:4: ( (lv_right_13_0= ruleExpression ) )
                    // InternalGaml.g:5840:5: (lv_right_13_0= ruleExpression )
                    {
                    // InternalGaml.g:5840:5: (lv_right_13_0= ruleExpression )
                    // InternalGaml.g:5841:6: lv_right_13_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getRightExpressionParserRuleCall_4_4_0());
                      					
                    }
                    pushFollow(FOLLOW_58);
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

                    // InternalGaml.g:5858:4: (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )?
                    int alt78=2;
                    int LA78_0 = input.LA(1);

                    if ( (LA78_0==98) ) {
                        alt78=1;
                    }
                    switch (alt78) {
                        case 1 :
                            // InternalGaml.g:5859:5: otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) )
                            {
                            otherlv_14=(Token)match(input,98,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getPrimaryAccess().getCommaKeyword_4_5_0());
                              				
                            }
                            // InternalGaml.g:5863:5: ( (lv_z_15_0= ruleExpression ) )
                            // InternalGaml.g:5864:6: (lv_z_15_0= ruleExpression )
                            {
                            // InternalGaml.g:5864:6: (lv_z_15_0= ruleExpression )
                            // InternalGaml.g:5865:7: lv_z_15_0= ruleExpression
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPrimaryAccess().getZExpressionParserRuleCall_4_5_1_0());
                              						
                            }
                            pushFollow(FOLLOW_41);
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

                    otherlv_16=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5892:1: entryRuleAbstractRef returns [EObject current=null] : iv_ruleAbstractRef= ruleAbstractRef EOF ;
    public final EObject entryRuleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAbstractRef = null;


        try {
            // InternalGaml.g:5892:52: (iv_ruleAbstractRef= ruleAbstractRef EOF )
            // InternalGaml.g:5893:2: iv_ruleAbstractRef= ruleAbstractRef EOF
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
    // InternalGaml.g:5899:1: ruleAbstractRef returns [EObject current=null] : ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) ;
    public final EObject ruleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject this_Function_0 = null;

        EObject this_VariableRef_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5905:2: ( ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) )
            // InternalGaml.g:5906:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            {
            // InternalGaml.g:5906:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            int alt80=2;
            alt80 = dfa80.predict(input);
            switch (alt80) {
                case 1 :
                    // InternalGaml.g:5907:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    {
                    // InternalGaml.g:5907:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    // InternalGaml.g:5908:4: ( ruleFunction )=>this_Function_0= ruleFunction
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
                    // InternalGaml.g:5919:3: this_VariableRef_1= ruleVariableRef
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
    // InternalGaml.g:5931:1: entryRuleFunction returns [EObject current=null] : iv_ruleFunction= ruleFunction EOF ;
    public final EObject entryRuleFunction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunction = null;


        try {
            // InternalGaml.g:5931:49: (iv_ruleFunction= ruleFunction EOF )
            // InternalGaml.g:5932:2: iv_ruleFunction= ruleFunction EOF
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
    // InternalGaml.g:5938:1: ruleFunction returns [EObject current=null] : ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) ;
    public final EObject ruleFunction() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_left_1_0 = null;

        EObject lv_type_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5944:2: ( ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) )
            // InternalGaml.g:5945:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            {
            // InternalGaml.g:5945:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            // InternalGaml.g:5946:3: () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')'
            {
            // InternalGaml.g:5946:3: ()
            // InternalGaml.g:5947:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getFunctionAccess().getFunctionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5953:3: ( (lv_left_1_0= ruleActionRef ) )
            // InternalGaml.g:5954:4: (lv_left_1_0= ruleActionRef )
            {
            // InternalGaml.g:5954:4: (lv_left_1_0= ruleActionRef )
            // InternalGaml.g:5955:5: lv_left_1_0= ruleActionRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFunctionAccess().getLeftActionRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_59);
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

            // InternalGaml.g:5972:3: ( (lv_type_2_0= ruleTypeInfo ) )?
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==134) ) {
                alt81=1;
            }
            switch (alt81) {
                case 1 :
                    // InternalGaml.g:5973:4: (lv_type_2_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:5973:4: (lv_type_2_0= ruleTypeInfo )
                    // InternalGaml.g:5974:5: lv_type_2_0= ruleTypeInfo
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getTypeTypeInfoParserRuleCall_2_0());
                      				
                    }
                    pushFollow(FOLLOW_60);
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

            otherlv_3=(Token)match(input,35,FOLLOW_61); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getFunctionAccess().getLeftParenthesisKeyword_3());
              		
            }
            // InternalGaml.g:5995:3: ( (lv_right_4_0= ruleExpressionList ) )?
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( ((LA82_0>=RULE_ID && LA82_0<=RULE_KEYWORD)||LA82_0==20||LA82_0==33||LA82_0==35||LA82_0==39||(LA82_0>=48 && LA82_0<=91)||(LA82_0>=99 && LA82_0<=125)||LA82_0==136||(LA82_0>=140 && LA82_0<=142)) ) {
                alt82=1;
            }
            switch (alt82) {
                case 1 :
                    // InternalGaml.g:5996:4: (lv_right_4_0= ruleExpressionList )
                    {
                    // InternalGaml.g:5996:4: (lv_right_4_0= ruleExpressionList )
                    // InternalGaml.g:5997:5: lv_right_4_0= ruleExpressionList
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getRightExpressionListParserRuleCall_4_0());
                      				
                    }
                    pushFollow(FOLLOW_30);
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

            otherlv_5=(Token)match(input,36,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:6022:1: entryRuleExpressionList returns [EObject current=null] : iv_ruleExpressionList= ruleExpressionList EOF ;
    public final EObject entryRuleExpressionList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpressionList = null;


        try {
            // InternalGaml.g:6022:55: (iv_ruleExpressionList= ruleExpressionList EOF )
            // InternalGaml.g:6023:2: iv_ruleExpressionList= ruleExpressionList EOF
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
    // InternalGaml.g:6029:1: ruleExpressionList returns [EObject current=null] : ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) ;
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
            // InternalGaml.g:6035:2: ( ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) )
            // InternalGaml.g:6036:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            {
            // InternalGaml.g:6036:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            int alt85=2;
            alt85 = dfa85.predict(input);
            switch (alt85) {
                case 1 :
                    // InternalGaml.g:6037:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    {
                    // InternalGaml.g:6037:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    // InternalGaml.g:6038:4: ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    {
                    // InternalGaml.g:6038:4: ( (lv_exprs_0_0= ruleExpression ) )
                    // InternalGaml.g:6039:5: (lv_exprs_0_0= ruleExpression )
                    {
                    // InternalGaml.g:6039:5: (lv_exprs_0_0= ruleExpression )
                    // InternalGaml.g:6040:6: lv_exprs_0_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_39);
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

                    // InternalGaml.g:6057:4: (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    loop83:
                    do {
                        int alt83=2;
                        int LA83_0 = input.LA(1);

                        if ( (LA83_0==98) ) {
                            alt83=1;
                        }


                        switch (alt83) {
                    	case 1 :
                    	    // InternalGaml.g:6058:5: otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) )
                    	    {
                    	    otherlv_1=(Token)match(input,98,FOLLOW_5); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_1, grammarAccess.getExpressionListAccess().getCommaKeyword_0_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:6062:5: ( (lv_exprs_2_0= ruleExpression ) )
                    	    // InternalGaml.g:6063:6: (lv_exprs_2_0= ruleExpression )
                    	    {
                    	    // InternalGaml.g:6063:6: (lv_exprs_2_0= ruleExpression )
                    	    // InternalGaml.g:6064:7: lv_exprs_2_0= ruleExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_39);
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
                    	    break loop83;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:6084:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    {
                    // InternalGaml.g:6084:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    // InternalGaml.g:6085:4: ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    {
                    // InternalGaml.g:6085:4: ( (lv_exprs_3_0= ruleParameter ) )
                    // InternalGaml.g:6086:5: (lv_exprs_3_0= ruleParameter )
                    {
                    // InternalGaml.g:6086:5: (lv_exprs_3_0= ruleParameter )
                    // InternalGaml.g:6087:6: lv_exprs_3_0= ruleParameter
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_39);
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

                    // InternalGaml.g:6104:4: (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    loop84:
                    do {
                        int alt84=2;
                        int LA84_0 = input.LA(1);

                        if ( (LA84_0==98) ) {
                            alt84=1;
                        }


                        switch (alt84) {
                    	case 1 :
                    	    // InternalGaml.g:6105:5: otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) )
                    	    {
                    	    otherlv_4=(Token)match(input,98,FOLLOW_56); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_4, grammarAccess.getExpressionListAccess().getCommaKeyword_1_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:6109:5: ( (lv_exprs_5_0= ruleParameter ) )
                    	    // InternalGaml.g:6110:6: (lv_exprs_5_0= ruleParameter )
                    	    {
                    	    // InternalGaml.g:6110:6: (lv_exprs_5_0= ruleParameter )
                    	    // InternalGaml.g:6111:7: lv_exprs_5_0= ruleParameter
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_39);
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
                    	    break loop84;
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
    // InternalGaml.g:6134:1: entryRuleParameter returns [EObject current=null] : iv_ruleParameter= ruleParameter EOF ;
    public final EObject entryRuleParameter() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameter = null;


        try {
            // InternalGaml.g:6134:50: (iv_ruleParameter= ruleParameter EOF )
            // InternalGaml.g:6135:2: iv_ruleParameter= ruleParameter EOF
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
    // InternalGaml.g:6141:1: ruleParameter returns [EObject current=null] : ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) ;
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
            // InternalGaml.g:6147:2: ( ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) )
            // InternalGaml.g:6148:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            {
            // InternalGaml.g:6148:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            // InternalGaml.g:6149:3: () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) )
            {
            // InternalGaml.g:6149:3: ()
            // InternalGaml.g:6150:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getParameterAccess().getParameterAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6156:3: ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) )
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==33||(LA87_0>=99 && LA87_0<=125)) ) {
                alt87=1;
            }
            else if ( (LA87_0==RULE_ID||(LA87_0>=48 && LA87_0<=91)) ) {
                alt87=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // InternalGaml.g:6157:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    {
                    // InternalGaml.g:6157:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    // InternalGaml.g:6158:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    {
                    // InternalGaml.g:6158:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    // InternalGaml.g:6159:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    {
                    // InternalGaml.g:6159:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    int alt86=5;
                    switch ( input.LA(1) ) {
                    case 99:
                    case 100:
                        {
                        alt86=1;
                        }
                        break;
                    case 101:
                    case 102:
                    case 103:
                    case 104:
                    case 105:
                        {
                        alt86=2;
                        }
                        break;
                    case 33:
                    case 106:
                    case 107:
                    case 108:
                    case 109:
                    case 110:
                    case 111:
                    case 112:
                    case 113:
                    case 114:
                    case 115:
                    case 116:
                    case 117:
                    case 118:
                    case 119:
                    case 120:
                    case 121:
                    case 122:
                        {
                        alt86=3;
                        }
                        break;
                    case 123:
                    case 124:
                        {
                        alt86=4;
                        }
                        break;
                    case 125:
                        {
                        alt86=5;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 86, 0, input);

                        throw nvae;
                    }

                    switch (alt86) {
                        case 1 :
                            // InternalGaml.g:6160:7: lv_builtInFacetKey_1_1= ruleDefinitionFacetKey
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
                            // InternalGaml.g:6176:7: lv_builtInFacetKey_1_2= ruleTypeFacetKey
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
                            // InternalGaml.g:6192:7: lv_builtInFacetKey_1_3= ruleSpecialFacetKey
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
                            // InternalGaml.g:6208:7: lv_builtInFacetKey_1_4= ruleActionFacetKey
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
                            // InternalGaml.g:6224:7: lv_builtInFacetKey_1_5= ruleVarFacetKey
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
                    // InternalGaml.g:6243:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    {
                    // InternalGaml.g:6243:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    // InternalGaml.g:6244:5: ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':'
                    {
                    // InternalGaml.g:6244:5: ( (lv_left_2_0= ruleVariableRef ) )
                    // InternalGaml.g:6245:6: (lv_left_2_0= ruleVariableRef )
                    {
                    // InternalGaml.g:6245:6: (lv_left_2_0= ruleVariableRef )
                    // InternalGaml.g:6246:7: lv_left_2_0= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getParameterAccess().getLeftVariableRefParserRuleCall_1_1_0_0());
                      						
                    }
                    pushFollow(FOLLOW_27);
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

                    otherlv_3=(Token)match(input,34,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getParameterAccess().getColonKeyword_1_1_1());
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:6269:3: ( (lv_right_4_0= ruleExpression ) )
            // InternalGaml.g:6270:4: (lv_right_4_0= ruleExpression )
            {
            // InternalGaml.g:6270:4: (lv_right_4_0= ruleExpression )
            // InternalGaml.g:6271:5: lv_right_4_0= ruleExpression
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
    // InternalGaml.g:6292:1: entryRuleUnitRef returns [EObject current=null] : iv_ruleUnitRef= ruleUnitRef EOF ;
    public final EObject entryRuleUnitRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitRef = null;


        try {
            // InternalGaml.g:6292:48: (iv_ruleUnitRef= ruleUnitRef EOF )
            // InternalGaml.g:6293:2: iv_ruleUnitRef= ruleUnitRef EOF
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
    // InternalGaml.g:6299:1: ruleUnitRef returns [EObject current=null] : ( () ( (otherlv_1= RULE_ID ) ) ) ;
    public final EObject ruleUnitRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;


        	enterRule();

        try {
            // InternalGaml.g:6305:2: ( ( () ( (otherlv_1= RULE_ID ) ) ) )
            // InternalGaml.g:6306:2: ( () ( (otherlv_1= RULE_ID ) ) )
            {
            // InternalGaml.g:6306:2: ( () ( (otherlv_1= RULE_ID ) ) )
            // InternalGaml.g:6307:3: () ( (otherlv_1= RULE_ID ) )
            {
            // InternalGaml.g:6307:3: ()
            // InternalGaml.g:6308:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getUnitRefAccess().getUnitNameAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6314:3: ( (otherlv_1= RULE_ID ) )
            // InternalGaml.g:6315:4: (otherlv_1= RULE_ID )
            {
            // InternalGaml.g:6315:4: (otherlv_1= RULE_ID )
            // InternalGaml.g:6316:5: otherlv_1= RULE_ID
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
    // InternalGaml.g:6331:1: entryRuleVariableRef returns [EObject current=null] : iv_ruleVariableRef= ruleVariableRef EOF ;
    public final EObject entryRuleVariableRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVariableRef = null;


        try {
            // InternalGaml.g:6331:52: (iv_ruleVariableRef= ruleVariableRef EOF )
            // InternalGaml.g:6332:2: iv_ruleVariableRef= ruleVariableRef EOF
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
    // InternalGaml.g:6338:1: ruleVariableRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleVariableRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:6344:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:6345:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:6345:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:6346:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:6346:3: ()
            // InternalGaml.g:6347:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getVariableRefAccess().getVariableRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6353:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:6354:4: ( ruleValid_ID )
            {
            // InternalGaml.g:6354:4: ( ruleValid_ID )
            // InternalGaml.g:6355:5: ruleValid_ID
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
    // InternalGaml.g:6373:1: entryRuleTypeRef returns [EObject current=null] : iv_ruleTypeRef= ruleTypeRef EOF ;
    public final EObject entryRuleTypeRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeRef = null;


        try {
            // InternalGaml.g:6373:48: (iv_ruleTypeRef= ruleTypeRef EOF )
            // InternalGaml.g:6374:2: iv_ruleTypeRef= ruleTypeRef EOF
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
    // InternalGaml.g:6380:1: ruleTypeRef returns [EObject current=null] : ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) ;
    public final EObject ruleTypeRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_parameter_2_0 = null;

        EObject lv_parameter_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6386:2: ( ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) )
            // InternalGaml.g:6387:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            {
            // InternalGaml.g:6387:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0==RULE_ID) ) {
                alt89=1;
            }
            else if ( (LA89_0==48) ) {
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
                    // InternalGaml.g:6388:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    {
                    // InternalGaml.g:6388:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    // InternalGaml.g:6389:4: () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    {
                    // InternalGaml.g:6389:4: ()
                    // InternalGaml.g:6390:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6396:4: ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    // InternalGaml.g:6397:5: ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    {
                    // InternalGaml.g:6397:5: ( (otherlv_1= RULE_ID ) )
                    // InternalGaml.g:6398:6: (otherlv_1= RULE_ID )
                    {
                    // InternalGaml.g:6398:6: (otherlv_1= RULE_ID )
                    // InternalGaml.g:6399:7: otherlv_1= RULE_ID
                    {
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElement(grammarAccess.getTypeRefRule());
                      							}
                      						
                    }
                    otherlv_1=(Token)match(input,RULE_ID,FOLLOW_62); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							newLeafNode(otherlv_1, grammarAccess.getTypeRefAccess().getRefTypeDefinitionCrossReference_0_1_0_0());
                      						
                    }

                    }


                    }

                    // InternalGaml.g:6410:5: ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    int alt88=2;
                    int LA88_0 = input.LA(1);

                    if ( (LA88_0==134) ) {
                        alt88=1;
                    }
                    switch (alt88) {
                        case 1 :
                            // InternalGaml.g:6411:6: (lv_parameter_2_0= ruleTypeInfo )
                            {
                            // InternalGaml.g:6411:6: (lv_parameter_2_0= ruleTypeInfo )
                            // InternalGaml.g:6412:7: lv_parameter_2_0= ruleTypeInfo
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
                    // InternalGaml.g:6432:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    {
                    // InternalGaml.g:6432:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    // InternalGaml.g:6433:4: () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    {
                    // InternalGaml.g:6433:4: ()
                    // InternalGaml.g:6434:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6440:4: (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    // InternalGaml.g:6441:5: otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) )
                    {
                    otherlv_4=(Token)match(input,48,FOLLOW_63); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getTypeRefAccess().getSpeciesKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:6445:5: ( (lv_parameter_5_0= ruleTypeInfo ) )
                    // InternalGaml.g:6446:6: (lv_parameter_5_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:6446:6: (lv_parameter_5_0= ruleTypeInfo )
                    // InternalGaml.g:6447:7: lv_parameter_5_0= ruleTypeInfo
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
    // InternalGaml.g:6470:1: entryRuleTypeInfo returns [EObject current=null] : iv_ruleTypeInfo= ruleTypeInfo EOF ;
    public final EObject entryRuleTypeInfo() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeInfo = null;


        try {
            // InternalGaml.g:6470:49: (iv_ruleTypeInfo= ruleTypeInfo EOF )
            // InternalGaml.g:6471:2: iv_ruleTypeInfo= ruleTypeInfo EOF
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
    // InternalGaml.g:6477:1: ruleTypeInfo returns [EObject current=null] : (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) ;
    public final EObject ruleTypeInfo() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_first_1_0 = null;

        EObject lv_second_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6483:2: ( (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) )
            // InternalGaml.g:6484:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            {
            // InternalGaml.g:6484:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            // InternalGaml.g:6485:3: otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' )
            {
            otherlv_0=(Token)match(input,134,FOLLOW_29); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeInfoAccess().getLessThanSignKeyword_0());
              		
            }
            // InternalGaml.g:6489:3: ( (lv_first_1_0= ruleTypeRef ) )
            // InternalGaml.g:6490:4: (lv_first_1_0= ruleTypeRef )
            {
            // InternalGaml.g:6490:4: (lv_first_1_0= ruleTypeRef )
            // InternalGaml.g:6491:5: lv_first_1_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getTypeInfoAccess().getFirstTypeRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_64);
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

            // InternalGaml.g:6508:3: (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )?
            int alt90=2;
            int LA90_0 = input.LA(1);

            if ( (LA90_0==98) ) {
                alt90=1;
            }
            switch (alt90) {
                case 1 :
                    // InternalGaml.g:6509:4: otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) )
                    {
                    otherlv_2=(Token)match(input,98,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getTypeInfoAccess().getCommaKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:6513:4: ( (lv_second_3_0= ruleTypeRef ) )
                    // InternalGaml.g:6514:5: (lv_second_3_0= ruleTypeRef )
                    {
                    // InternalGaml.g:6514:5: (lv_second_3_0= ruleTypeRef )
                    // InternalGaml.g:6515:6: lv_second_3_0= ruleTypeRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getTypeInfoAccess().getSecondTypeRefParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_37);
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

            // InternalGaml.g:6533:3: ( ( '>' )=>otherlv_4= '>' )
            // InternalGaml.g:6534:4: ( '>' )=>otherlv_4= '>'
            {
            otherlv_4=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:6544:1: entryRuleActionRef returns [EObject current=null] : iv_ruleActionRef= ruleActionRef EOF ;
    public final EObject entryRuleActionRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionRef = null;


        try {
            // InternalGaml.g:6544:50: (iv_ruleActionRef= ruleActionRef EOF )
            // InternalGaml.g:6545:2: iv_ruleActionRef= ruleActionRef EOF
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
    // InternalGaml.g:6551:1: ruleActionRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleActionRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:6557:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:6558:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:6558:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:6559:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:6559:3: ()
            // InternalGaml.g:6560:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getActionRefAccess().getActionRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6566:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:6567:4: ( ruleValid_ID )
            {
            // InternalGaml.g:6567:4: ( ruleValid_ID )
            // InternalGaml.g:6568:5: ruleValid_ID
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
    // InternalGaml.g:6586:1: entryRuleEquationRef returns [EObject current=null] : iv_ruleEquationRef= ruleEquationRef EOF ;
    public final EObject entryRuleEquationRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationRef = null;


        try {
            // InternalGaml.g:6586:52: (iv_ruleEquationRef= ruleEquationRef EOF )
            // InternalGaml.g:6587:2: iv_ruleEquationRef= ruleEquationRef EOF
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
    // InternalGaml.g:6593:1: ruleEquationRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleEquationRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:6599:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:6600:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:6600:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:6601:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:6601:3: ()
            // InternalGaml.g:6602:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getEquationRefAccess().getEquationRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:6608:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:6609:4: ( ruleValid_ID )
            {
            // InternalGaml.g:6609:4: ( ruleValid_ID )
            // InternalGaml.g:6610:5: ruleValid_ID
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
    // InternalGaml.g:6628:1: entryRuleEquationDefinition returns [EObject current=null] : iv_ruleEquationDefinition= ruleEquationDefinition EOF ;
    public final EObject entryRuleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationDefinition = null;


        try {
            // InternalGaml.g:6628:59: (iv_ruleEquationDefinition= ruleEquationDefinition EOF )
            // InternalGaml.g:6629:2: iv_ruleEquationDefinition= ruleEquationDefinition EOF
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
    // InternalGaml.g:6635:1: ruleEquationDefinition returns [EObject current=null] : (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) ;
    public final EObject ruleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Equations_0 = null;

        EObject this_EquationFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:6641:2: ( (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) )
            // InternalGaml.g:6642:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            {
            // InternalGaml.g:6642:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            int alt91=2;
            int LA91_0 = input.LA(1);

            if ( (LA91_0==38) ) {
                alt91=1;
            }
            else if ( (LA91_0==149) ) {
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
                    // InternalGaml.g:6643:3: this_S_Equations_0= ruleS_Equations
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
                    // InternalGaml.g:6652:3: this_EquationFakeDefinition_1= ruleEquationFakeDefinition
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
    // InternalGaml.g:6664:1: entryRuleTypeDefinition returns [EObject current=null] : iv_ruleTypeDefinition= ruleTypeDefinition EOF ;
    public final EObject entryRuleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDefinition = null;


        try {
            // InternalGaml.g:6664:55: (iv_ruleTypeDefinition= ruleTypeDefinition EOF )
            // InternalGaml.g:6665:2: iv_ruleTypeDefinition= ruleTypeDefinition EOF
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
    // InternalGaml.g:6671:1: ruleTypeDefinition returns [EObject current=null] : (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) ;
    public final EObject ruleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Species_0 = null;

        EObject this_TypeFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:6677:2: ( (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) )
            // InternalGaml.g:6678:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            {
            // InternalGaml.g:6678:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            int alt92=2;
            int LA92_0 = input.LA(1);

            if ( ((LA92_0>=48 && LA92_0<=49)) ) {
                alt92=1;
            }
            else if ( (LA92_0==145) ) {
                alt92=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                throw nvae;
            }
            switch (alt92) {
                case 1 :
                    // InternalGaml.g:6679:3: this_S_Species_0= ruleS_Species
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
                    // InternalGaml.g:6688:3: this_TypeFakeDefinition_1= ruleTypeFakeDefinition
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
    // InternalGaml.g:6700:1: entryRuleVarDefinition returns [EObject current=null] : iv_ruleVarDefinition= ruleVarDefinition EOF ;
    public final EObject entryRuleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDefinition = null;


        try {
            // InternalGaml.g:6700:54: (iv_ruleVarDefinition= ruleVarDefinition EOF )
            // InternalGaml.g:6701:2: iv_ruleVarDefinition= ruleVarDefinition EOF
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
    // InternalGaml.g:6707:1: ruleVarDefinition returns [EObject current=null] : ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment ) ;
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
            // InternalGaml.g:6713:2: ( ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment ) )
            // InternalGaml.g:6714:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )
            {
            // InternalGaml.g:6714:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )
            int alt93=7;
            alt93 = dfa93.predict(input);
            switch (alt93) {
                case 1 :
                    // InternalGaml.g:6715:3: ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration )
                    {
                    // InternalGaml.g:6715:3: ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration )
                    // InternalGaml.g:6716:4: ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration
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
                    // InternalGaml.g:6727:3: this_Model_1= ruleModel
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
                    // InternalGaml.g:6736:3: this_ArgumentDefinition_2= ruleArgumentDefinition
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
                    // InternalGaml.g:6745:3: this_DefinitionFacet_3= ruleDefinitionFacet
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
                    // InternalGaml.g:6754:3: this_VarFakeDefinition_4= ruleVarFakeDefinition
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
                    // InternalGaml.g:6763:3: this_Import_5= ruleImport
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
                    // InternalGaml.g:6772:3: this_S_Experiment_6= ruleS_Experiment
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
    // InternalGaml.g:6784:1: entryRuleActionDefinition returns [EObject current=null] : iv_ruleActionDefinition= ruleActionDefinition EOF ;
    public final EObject entryRuleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionDefinition = null;


        try {
            // InternalGaml.g:6784:57: (iv_ruleActionDefinition= ruleActionDefinition EOF )
            // InternalGaml.g:6785:2: iv_ruleActionDefinition= ruleActionDefinition EOF
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
    // InternalGaml.g:6791:1: ruleActionDefinition returns [EObject current=null] : (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) ;
    public final EObject ruleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Action_0 = null;

        EObject this_ActionFakeDefinition_1 = null;

        EObject this_S_Definition_2 = null;

        EObject this_TypeDefinition_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:6797:2: ( (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) )
            // InternalGaml.g:6798:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            {
            // InternalGaml.g:6798:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            int alt94=4;
            switch ( input.LA(1) ) {
            case 37:
                {
                alt94=1;
                }
                break;
            case 146:
                {
                alt94=2;
                }
                break;
            case RULE_ID:
                {
                alt94=3;
                }
                break;
            case 48:
                {
                int LA94_4 = input.LA(2);

                if ( (LA94_4==134) ) {
                    alt94=3;
                }
                else if ( (LA94_4==RULE_ID) ) {
                    alt94=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 94, 4, input);

                    throw nvae;
                }
                }
                break;
            case 49:
            case 145:
                {
                alt94=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 94, 0, input);

                throw nvae;
            }

            switch (alt94) {
                case 1 :
                    // InternalGaml.g:6799:3: this_S_Action_0= ruleS_Action
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
                    // InternalGaml.g:6808:3: this_ActionFakeDefinition_1= ruleActionFakeDefinition
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
                    // InternalGaml.g:6817:3: this_S_Definition_2= ruleS_Definition
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
                    // InternalGaml.g:6826:3: this_TypeDefinition_3= ruleTypeDefinition
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


    // $ANTLR start "entryRuleValid_ID"
    // InternalGaml.g:6838:1: entryRuleValid_ID returns [String current=null] : iv_ruleValid_ID= ruleValid_ID EOF ;
    public final String entryRuleValid_ID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValid_ID = null;


        try {
            // InternalGaml.g:6838:48: (iv_ruleValid_ID= ruleValid_ID EOF )
            // InternalGaml.g:6839:2: iv_ruleValid_ID= ruleValid_ID EOF
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
    // InternalGaml.g:6845:1: ruleValid_ID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this__SpeciesKey_0= rule_SpeciesKey | this_K_Reflex_1= ruleK_Reflex | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID ) ;
    public final AntlrDatatypeRuleToken ruleValid_ID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_4=null;
        AntlrDatatypeRuleToken this__SpeciesKey_0 = null;

        AntlrDatatypeRuleToken this_K_Reflex_1 = null;

        AntlrDatatypeRuleToken this__GeneralKey_2 = null;

        AntlrDatatypeRuleToken this__ExperimentKey_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:6851:2: ( (this__SpeciesKey_0= rule_SpeciesKey | this_K_Reflex_1= ruleK_Reflex | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID ) )
            // InternalGaml.g:6852:2: (this__SpeciesKey_0= rule_SpeciesKey | this_K_Reflex_1= ruleK_Reflex | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID )
            {
            // InternalGaml.g:6852:2: (this__SpeciesKey_0= rule_SpeciesKey | this_K_Reflex_1= ruleK_Reflex | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID )
            int alt95=5;
            switch ( input.LA(1) ) {
            case 48:
            case 49:
                {
                alt95=1;
                }
                break;
            case 89:
            case 90:
            case 91:
                {
                alt95=2;
                }
                break;
            case 51:
            case 52:
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
                {
                alt95=3;
                }
                break;
            case 50:
                {
                alt95=4;
                }
                break;
            case RULE_ID:
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
                    // InternalGaml.g:6853:3: this__SpeciesKey_0= rule_SpeciesKey
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
                    // InternalGaml.g:6864:3: this_K_Reflex_1= ruleK_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_ReflexParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Reflex_1=ruleK_Reflex();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Reflex_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:6875:3: this__GeneralKey_2= rule_GeneralKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_GeneralKeyParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__GeneralKey_2=rule_GeneralKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__GeneralKey_2);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:6886:3: this__ExperimentKey_3= rule_ExperimentKey
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().get_ExperimentKeyParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this__ExperimentKey_3=rule_ExperimentKey();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this__ExperimentKey_3);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:6897:3: this_ID_4= RULE_ID
                    {
                    this_ID_4=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ID_4);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_ID_4, grammarAccess.getValid_IDAccess().getIDTerminalRuleCall_4());
                      		
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


    // $ANTLR start "entryRuleUnitFakeDefinition"
    // InternalGaml.g:6908:1: entryRuleUnitFakeDefinition returns [EObject current=null] : iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF ;
    public final EObject entryRuleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitFakeDefinition = null;


        try {
            // InternalGaml.g:6908:59: (iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF )
            // InternalGaml.g:6909:2: iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF
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
    // InternalGaml.g:6915:1: ruleUnitFakeDefinition returns [EObject current=null] : (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6921:2: ( (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6922:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6922:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6923:3: otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,144,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getUnitFakeDefinitionAccess().getUnitKeyword_0());
              		
            }
            // InternalGaml.g:6927:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6928:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6928:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6929:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6949:1: entryRuleTypeFakeDefinition returns [EObject current=null] : iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF ;
    public final EObject entryRuleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFakeDefinition = null;


        try {
            // InternalGaml.g:6949:59: (iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF )
            // InternalGaml.g:6950:2: iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF
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
    // InternalGaml.g:6956:1: ruleTypeFakeDefinition returns [EObject current=null] : (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6962:2: ( (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6963:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6963:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6964:3: otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,145,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeFakeDefinitionAccess().getTypeKeyword_0());
              		
            }
            // InternalGaml.g:6968:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6969:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6969:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6970:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6990:1: entryRuleActionFakeDefinition returns [EObject current=null] : iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF ;
    public final EObject entryRuleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFakeDefinition = null;


        try {
            // InternalGaml.g:6990:61: (iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF )
            // InternalGaml.g:6991:2: iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF
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
    // InternalGaml.g:6997:1: ruleActionFakeDefinition returns [EObject current=null] : (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7003:2: ( (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7004:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7004:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7005:3: otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,146,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getActionFakeDefinitionAccess().getActionKeyword_0());
              		
            }
            // InternalGaml.g:7009:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7010:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7010:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7011:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:7032:1: entryRuleSkillFakeDefinition returns [EObject current=null] : iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF ;
    public final EObject entryRuleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSkillFakeDefinition = null;


        try {
            // InternalGaml.g:7032:60: (iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF )
            // InternalGaml.g:7033:2: iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF
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
    // InternalGaml.g:7039:1: ruleSkillFakeDefinition returns [EObject current=null] : (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7045:2: ( (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:7046:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:7046:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:7047:3: otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,147,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSkillFakeDefinitionAccess().getSkillKeyword_0());
              		
            }
            // InternalGaml.g:7051:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:7052:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:7052:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:7053:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:7073:1: entryRuleVarFakeDefinition returns [EObject current=null] : iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF ;
    public final EObject entryRuleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFakeDefinition = null;


        try {
            // InternalGaml.g:7073:58: (iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF )
            // InternalGaml.g:7074:2: iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF
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
    // InternalGaml.g:7080:1: ruleVarFakeDefinition returns [EObject current=null] : (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7086:2: ( (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7087:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7087:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7088:3: otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,148,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getVarFakeDefinitionAccess().getVarKeyword_0());
              		
            }
            // InternalGaml.g:7092:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7093:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7093:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7094:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:7115:1: entryRuleEquationFakeDefinition returns [EObject current=null] : iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF ;
    public final EObject entryRuleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationFakeDefinition = null;


        try {
            // InternalGaml.g:7115:63: (iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF )
            // InternalGaml.g:7116:2: iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF
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
    // InternalGaml.g:7122:1: ruleEquationFakeDefinition returns [EObject current=null] : (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7128:2: ( (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:7129:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:7129:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:7130:3: otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,149,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getEquationFakeDefinitionAccess().getEquationKeyword_0());
              		
            }
            // InternalGaml.g:7134:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:7135:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:7135:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:7136:5: lv_name_1_0= ruleValid_ID
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


    // $ANTLR start "entryRuleTerminalExpression"
    // InternalGaml.g:7157:1: entryRuleTerminalExpression returns [EObject current=null] : iv_ruleTerminalExpression= ruleTerminalExpression EOF ;
    public final EObject entryRuleTerminalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTerminalExpression = null;


        try {
            // InternalGaml.g:7157:59: (iv_ruleTerminalExpression= ruleTerminalExpression EOF )
            // InternalGaml.g:7158:2: iv_ruleTerminalExpression= ruleTerminalExpression EOF
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
    // InternalGaml.g:7164:1: ruleTerminalExpression returns [EObject current=null] : (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) ;
    public final EObject ruleTerminalExpression() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token lv_op_4_0=null;
        Token lv_op_6_0=null;
        Token lv_op_8_0=null;
        EObject this_StringLiteral_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:7170:2: ( (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) )
            // InternalGaml.g:7171:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            {
            // InternalGaml.g:7171:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            int alt96=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt96=1;
                }
                break;
            case RULE_INTEGER:
                {
                alt96=2;
                }
                break;
            case RULE_DOUBLE:
                {
                alt96=3;
                }
                break;
            case RULE_BOOLEAN:
                {
                alt96=4;
                }
                break;
            case RULE_KEYWORD:
                {
                alt96=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                throw nvae;
            }

            switch (alt96) {
                case 1 :
                    // InternalGaml.g:7172:3: this_StringLiteral_0= ruleStringLiteral
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
                    // InternalGaml.g:7181:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    {
                    // InternalGaml.g:7181:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    // InternalGaml.g:7182:4: () ( (lv_op_2_0= RULE_INTEGER ) )
                    {
                    // InternalGaml.g:7182:4: ()
                    // InternalGaml.g:7183:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getIntLiteralAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7189:4: ( (lv_op_2_0= RULE_INTEGER ) )
                    // InternalGaml.g:7190:5: (lv_op_2_0= RULE_INTEGER )
                    {
                    // InternalGaml.g:7190:5: (lv_op_2_0= RULE_INTEGER )
                    // InternalGaml.g:7191:6: lv_op_2_0= RULE_INTEGER
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
                    // InternalGaml.g:7209:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    {
                    // InternalGaml.g:7209:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    // InternalGaml.g:7210:4: () ( (lv_op_4_0= RULE_DOUBLE ) )
                    {
                    // InternalGaml.g:7210:4: ()
                    // InternalGaml.g:7211:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getDoubleLiteralAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7217:4: ( (lv_op_4_0= RULE_DOUBLE ) )
                    // InternalGaml.g:7218:5: (lv_op_4_0= RULE_DOUBLE )
                    {
                    // InternalGaml.g:7218:5: (lv_op_4_0= RULE_DOUBLE )
                    // InternalGaml.g:7219:6: lv_op_4_0= RULE_DOUBLE
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
                    // InternalGaml.g:7237:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    {
                    // InternalGaml.g:7237:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    // InternalGaml.g:7238:4: () ( (lv_op_6_0= RULE_BOOLEAN ) )
                    {
                    // InternalGaml.g:7238:4: ()
                    // InternalGaml.g:7239:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getBooleanLiteralAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7245:4: ( (lv_op_6_0= RULE_BOOLEAN ) )
                    // InternalGaml.g:7246:5: (lv_op_6_0= RULE_BOOLEAN )
                    {
                    // InternalGaml.g:7246:5: (lv_op_6_0= RULE_BOOLEAN )
                    // InternalGaml.g:7247:6: lv_op_6_0= RULE_BOOLEAN
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
                    // InternalGaml.g:7265:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    {
                    // InternalGaml.g:7265:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    // InternalGaml.g:7266:4: () ( (lv_op_8_0= RULE_KEYWORD ) )
                    {
                    // InternalGaml.g:7266:4: ()
                    // InternalGaml.g:7267:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getReservedLiteralAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:7273:4: ( (lv_op_8_0= RULE_KEYWORD ) )
                    // InternalGaml.g:7274:5: (lv_op_8_0= RULE_KEYWORD )
                    {
                    // InternalGaml.g:7274:5: (lv_op_8_0= RULE_KEYWORD )
                    // InternalGaml.g:7275:6: lv_op_8_0= RULE_KEYWORD
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
    // InternalGaml.g:7296:1: entryRuleStringLiteral returns [EObject current=null] : iv_ruleStringLiteral= ruleStringLiteral EOF ;
    public final EObject entryRuleStringLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStringLiteral = null;


        try {
            // InternalGaml.g:7296:54: (iv_ruleStringLiteral= ruleStringLiteral EOF )
            // InternalGaml.g:7297:2: iv_ruleStringLiteral= ruleStringLiteral EOF
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
    // InternalGaml.g:7303:1: ruleStringLiteral returns [EObject current=null] : ( (lv_op_0_0= RULE_STRING ) ) ;
    public final EObject ruleStringLiteral() throws RecognitionException {
        EObject current = null;

        Token lv_op_0_0=null;


        	enterRule();

        try {
            // InternalGaml.g:7309:2: ( ( (lv_op_0_0= RULE_STRING ) ) )
            // InternalGaml.g:7310:2: ( (lv_op_0_0= RULE_STRING ) )
            {
            // InternalGaml.g:7310:2: ( (lv_op_0_0= RULE_STRING ) )
            // InternalGaml.g:7311:3: (lv_op_0_0= RULE_STRING )
            {
            // InternalGaml.g:7311:3: (lv_op_0_0= RULE_STRING )
            // InternalGaml.g:7312:4: lv_op_0_0= RULE_STRING
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
        // InternalGaml.g:1028:4: ( ruleS_Equations )
        // InternalGaml.g:1028:5: ruleS_Equations
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
        // InternalGaml.g:1040:4: ( ruleS_Action )
        // InternalGaml.g:1040:5: ruleS_Action
        {
        pushFollow(FOLLOW_2);
        ruleS_Action();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_InternalGaml

    // $ANTLR start synpred4_InternalGaml
    public final void synpred4_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1052:4: ( ruleS_Species )
        // InternalGaml.g:1052:5: ruleS_Species
        {
        pushFollow(FOLLOW_2);
        ruleS_Species();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_InternalGaml

    // $ANTLR start synpred5_InternalGaml
    public final void synpred5_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1064:4: ( ruleS_Reflex )
        // InternalGaml.g:1064:5: ruleS_Reflex
        {
        pushFollow(FOLLOW_2);
        ruleS_Reflex();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_InternalGaml

    // $ANTLR start synpred6_InternalGaml
    public final void synpred6_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1076:4: ( ruleS_Assignment )
        // InternalGaml.g:1076:5: ruleS_Assignment
        {
        pushFollow(FOLLOW_2);
        ruleS_Assignment();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_InternalGaml

    // $ANTLR start synpred7_InternalGaml
    public final void synpred7_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1088:4: ( ruleS_General )
        // InternalGaml.g:1088:5: ruleS_General
        {
        pushFollow(FOLLOW_2);
        ruleS_General();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_InternalGaml

    // $ANTLR start synpred8_InternalGaml
    public final void synpred8_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1100:4: ( ruleS_Declaration )
        // InternalGaml.g:1100:5: ruleS_Declaration
        {
        pushFollow(FOLLOW_2);
        ruleS_Declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_InternalGaml

    // $ANTLR start synpred9_InternalGaml
    public final void synpred9_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1112:4: ( ruleS_Definition )
        // InternalGaml.g:1112:5: ruleS_Definition
        {
        pushFollow(FOLLOW_2);
        ruleS_Definition();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_InternalGaml

    // $ANTLR start synpred10_InternalGaml
    public final void synpred10_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1437:5: ( 'else' )
        // InternalGaml.g:1437:6: 'else'
        {
        match(input,29,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_InternalGaml

    // $ANTLR start synpred11_InternalGaml
    public final void synpred11_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1535:5: ( 'catch' )
        // InternalGaml.g:1535:6: 'catch'
        {
        match(input,31,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_InternalGaml

    // $ANTLR start synpred12_InternalGaml
    public final void synpred12_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1686:4: ( 'species' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==48 ) {
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
    // $ANTLR end synpred12_InternalGaml

    // $ANTLR start synpred13_InternalGaml
    public final void synpred13_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:3006:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )
        // InternalGaml.g:3006:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        {
        // InternalGaml.g:3006:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        // InternalGaml.g:3007:6: ( ( ruleExpression ) ) ruleFacetsAndBlock[null]
        {
        // InternalGaml.g:3007:6: ( ( ruleExpression ) )
        // InternalGaml.g:3008:7: ( ruleExpression )
        {
        // InternalGaml.g:3008:7: ( ruleExpression )
        // InternalGaml.g:3009:8: ruleExpression
        {
        pushFollow(FOLLOW_17);
        ruleExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }

        pushFollow(FOLLOW_2);
        ruleFacetsAndBlock(null);

        state._fsp--;
        if (state.failed) return ;

        }


        }
    }
    // $ANTLR end synpred13_InternalGaml

    // $ANTLR start synpred15_InternalGaml
    public final void synpred15_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:4241:5: ( ( ( ruleExpression ) ) )
        // InternalGaml.g:4241:6: ( ( ruleExpression ) )
        {
        // InternalGaml.g:4241:6: ( ( ruleExpression ) )
        // InternalGaml.g:4242:6: ( ruleExpression )
        {
        // InternalGaml.g:4242:6: ( ruleExpression )
        // InternalGaml.g:4243:7: ruleExpression
        {
        pushFollow(FOLLOW_2);
        ruleExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }
    }
    // $ANTLR end synpred15_InternalGaml

    // $ANTLR start synpred16_InternalGaml
    public final void synpred16_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:4338:5: ( 'species' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==48 ) {
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
    // $ANTLR end synpred16_InternalGaml

    // $ANTLR start synpred17_InternalGaml
    public final void synpred17_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5908:4: ( ruleFunction )
        // InternalGaml.g:5908:5: ruleFunction
        {
        pushFollow(FOLLOW_2);
        ruleFunction();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_InternalGaml

    // $ANTLR start synpred18_InternalGaml
    public final void synpred18_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:6534:4: ( '>' )
        // InternalGaml.g:6534:5: '>'
        {
        match(input,93,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_InternalGaml

    // $ANTLR start synpred19_InternalGaml
    public final void synpred19_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:6716:4: ( ruleS_Declaration )
        // InternalGaml.g:6716:5: ruleS_Declaration
        {
        pushFollow(FOLLOW_2);
        ruleS_Declaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_InternalGaml

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
    public final boolean synpred16_InternalGaml() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred16_InternalGaml_fragment(); // can never throw exception
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


    protected DFA14 dfa14 = new DFA14(this);
    protected DFA32 dfa32 = new DFA32(this);
    protected DFA38 dfa38 = new DFA38(this);
    protected DFA43 dfa43 = new DFA43(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA80 dfa80 = new DFA80(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA93 dfa93 = new DFA93(this);
    static final String dfa_1s = "\114\uffff";
    static final String dfa_2s = "\1\4\7\uffff\1\0\1\uffff\6\0\5\uffff\50\0\17\uffff";
    static final String dfa_3s = "\1\u008e\7\uffff\1\0\1\uffff\6\0\5\uffff\50\0\17\uffff";
    static final String dfa_4s = "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\10\6\uffff\5\14\50\uffff\7\14\1\7\1\16\1\11\1\12\1\17\1\20\1\13\1\15";
    static final String dfa_5s = "\1\0\7\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7\5\uffff\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\17\uffff}>";
    static final String[] dfa_6s = {
            "\1\74\1\20\1\21\1\22\1\23\1\24\12\uffff\1\76\4\uffff\2\6\1\10\1\4\1\uffff\1\5\1\uffff\1\2\2\uffff\1\75\1\uffff\1\12\1\11\1\77\2\uffff\1\1\4\uffff\1\3\1\13\1\14\1\73\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\15\1\16\1\17\54\uffff\1\101\3\uffff\1\100\1\102\1\103",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
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

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = dfa_1;
            this.eof = dfa_1;
            this.min = dfa_2;
            this.max = dfa_3;
            this.accept = dfa_4;
            this.special = dfa_5;
            this.transition = dfa_6;
        }
        public String getDescription() {
            return "963:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_General )=>this_S_General_12= ruleS_General ) | ( ( ruleS_Declaration )=>this_S_Declaration_13= ruleS_Declaration ) | ( ( ruleS_Definition )=>this_S_Definition_14= ruleS_Definition ) | this_S_Other_15= ruleS_Other )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_0 = input.LA(1);

                         
                        int index14_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_0==42) ) {s = 1;}

                        else if ( (LA14_0==32) ) {s = 2;}

                        else if ( (LA14_0==47) ) {s = 3;}

                        else if ( (LA14_0==28) ) {s = 4;}

                        else if ( (LA14_0==30) ) {s = 5;}

                        else if ( ((LA14_0>=25 && LA14_0<=26)) ) {s = 6;}

                        else if ( (LA14_0==27) ) {s = 8;}

                        else if ( (LA14_0==38) && (synpred2_InternalGaml())) {s = 9;}

                        else if ( (LA14_0==37) ) {s = 10;}

                        else if ( (LA14_0==48) ) {s = 11;}

                        else if ( (LA14_0==49) ) {s = 12;}

                        else if ( (LA14_0==89) ) {s = 13;}

                        else if ( (LA14_0==90) ) {s = 14;}

                        else if ( (LA14_0==91) ) {s = 15;}

                        else if ( (LA14_0==RULE_STRING) && (synpred6_InternalGaml())) {s = 16;}

                        else if ( (LA14_0==RULE_INTEGER) && (synpred6_InternalGaml())) {s = 17;}

                        else if ( (LA14_0==RULE_DOUBLE) && (synpred6_InternalGaml())) {s = 18;}

                        else if ( (LA14_0==RULE_BOOLEAN) && (synpred6_InternalGaml())) {s = 19;}

                        else if ( (LA14_0==RULE_KEYWORD) && (synpred6_InternalGaml())) {s = 20;}

                        else if ( (LA14_0==78) ) {s = 21;}

                        else if ( (LA14_0==79) ) {s = 22;}

                        else if ( (LA14_0==80) ) {s = 23;}

                        else if ( (LA14_0==81) ) {s = 24;}

                        else if ( (LA14_0==82) ) {s = 25;}

                        else if ( (LA14_0==83) ) {s = 26;}

                        else if ( (LA14_0==84) ) {s = 27;}

                        else if ( (LA14_0==85) ) {s = 28;}

                        else if ( (LA14_0==86) ) {s = 29;}

                        else if ( (LA14_0==87) ) {s = 30;}

                        else if ( (LA14_0==88) ) {s = 31;}

                        else if ( (LA14_0==51) ) {s = 32;}

                        else if ( (LA14_0==52) ) {s = 33;}

                        else if ( (LA14_0==53) ) {s = 34;}

                        else if ( (LA14_0==54) ) {s = 35;}

                        else if ( (LA14_0==55) ) {s = 36;}

                        else if ( (LA14_0==56) ) {s = 37;}

                        else if ( (LA14_0==57) ) {s = 38;}

                        else if ( (LA14_0==58) ) {s = 39;}

                        else if ( (LA14_0==59) ) {s = 40;}

                        else if ( (LA14_0==60) ) {s = 41;}

                        else if ( (LA14_0==61) ) {s = 42;}

                        else if ( (LA14_0==62) ) {s = 43;}

                        else if ( (LA14_0==63) ) {s = 44;}

                        else if ( (LA14_0==64) ) {s = 45;}

                        else if ( (LA14_0==65) ) {s = 46;}

                        else if ( (LA14_0==66) ) {s = 47;}

                        else if ( (LA14_0==67) ) {s = 48;}

                        else if ( (LA14_0==68) ) {s = 49;}

                        else if ( (LA14_0==69) ) {s = 50;}

                        else if ( (LA14_0==70) ) {s = 51;}

                        else if ( (LA14_0==71) ) {s = 52;}

                        else if ( (LA14_0==72) ) {s = 53;}

                        else if ( (LA14_0==73) ) {s = 54;}

                        else if ( (LA14_0==74) ) {s = 55;}

                        else if ( (LA14_0==75) ) {s = 56;}

                        else if ( (LA14_0==76) ) {s = 57;}

                        else if ( (LA14_0==77) ) {s = 58;}

                        else if ( (LA14_0==50) ) {s = 59;}

                        else if ( (LA14_0==RULE_ID) ) {s = 60;}

                        else if ( (LA14_0==35) && (synpred6_InternalGaml())) {s = 61;}

                        else if ( (LA14_0==20) && (synpred6_InternalGaml())) {s = 62;}

                        else if ( (LA14_0==39) && (synpred6_InternalGaml())) {s = 63;}

                        else if ( (LA14_0==140) && (synpred6_InternalGaml())) {s = 64;}

                        else if ( (LA14_0==136) && (synpred6_InternalGaml())) {s = 65;}

                        else if ( (LA14_0==141) && (synpred6_InternalGaml())) {s = 66;}

                        else if ( (LA14_0==142) && (synpred6_InternalGaml())) {s = 67;}

                         
                        input.seek(index14_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_8 = input.LA(1);

                         
                        int index14_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (true) ) {s = 68;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                         
                        input.seek(index14_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA14_10 = input.LA(1);

                         
                        int index14_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_InternalGaml()) ) {s = 70;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                         
                        input.seek(index14_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA14_11 = input.LA(1);

                         
                        int index14_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 71;}

                        else if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (synpred9_InternalGaml()) ) {s = 72;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA14_12 = input.LA(1);

                         
                        int index14_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 71;}

                        else if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_12);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA14_13 = input.LA(1);

                         
                        int index14_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_InternalGaml()) ) {s = 74;}

                        else if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_13);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA14_14 = input.LA(1);

                         
                        int index14_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_InternalGaml()) ) {s = 74;}

                        else if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_14);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA14_15 = input.LA(1);

                         
                        int index14_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_InternalGaml()) ) {s = 74;}

                        else if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_15);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA14_21 = input.LA(1);

                         
                        int index14_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_21);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA14_22 = input.LA(1);

                         
                        int index14_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_22);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA14_23 = input.LA(1);

                         
                        int index14_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_23);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA14_24 = input.LA(1);

                         
                        int index14_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_24);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA14_25 = input.LA(1);

                         
                        int index14_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_25);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA14_26 = input.LA(1);

                         
                        int index14_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_26);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA14_27 = input.LA(1);

                         
                        int index14_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_27);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA14_28 = input.LA(1);

                         
                        int index14_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_28);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA14_29 = input.LA(1);

                         
                        int index14_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_29);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA14_30 = input.LA(1);

                         
                        int index14_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_30);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA14_31 = input.LA(1);

                         
                        int index14_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_31);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA14_32 = input.LA(1);

                         
                        int index14_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_32);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA14_33 = input.LA(1);

                         
                        int index14_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_33);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA14_34 = input.LA(1);

                         
                        int index14_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_34);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA14_35 = input.LA(1);

                         
                        int index14_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_35);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA14_36 = input.LA(1);

                         
                        int index14_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_36);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA14_37 = input.LA(1);

                         
                        int index14_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_37);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA14_38 = input.LA(1);

                         
                        int index14_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_38);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA14_39 = input.LA(1);

                         
                        int index14_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_39);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA14_40 = input.LA(1);

                         
                        int index14_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_40);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA14_41 = input.LA(1);

                         
                        int index14_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_41);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA14_42 = input.LA(1);

                         
                        int index14_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_42);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA14_43 = input.LA(1);

                         
                        int index14_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_43);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA14_44 = input.LA(1);

                         
                        int index14_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_44);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA14_45 = input.LA(1);

                         
                        int index14_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_45);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA14_46 = input.LA(1);

                         
                        int index14_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_46);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA14_47 = input.LA(1);

                         
                        int index14_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_47);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA14_48 = input.LA(1);

                         
                        int index14_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_48);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA14_49 = input.LA(1);

                         
                        int index14_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_49);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA14_50 = input.LA(1);

                         
                        int index14_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_50);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA14_51 = input.LA(1);

                         
                        int index14_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_51);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA14_52 = input.LA(1);

                         
                        int index14_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_52);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA14_53 = input.LA(1);

                         
                        int index14_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_53);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA14_54 = input.LA(1);

                         
                        int index14_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_54);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA14_55 = input.LA(1);

                         
                        int index14_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_55);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA14_56 = input.LA(1);

                         
                        int index14_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_56);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA14_57 = input.LA(1);

                         
                        int index14_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_57);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA14_58 = input.LA(1);

                         
                        int index14_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred7_InternalGaml()) ) {s = 75;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_58);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA14_59 = input.LA(1);

                         
                        int index14_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_59);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA14_60 = input.LA(1);

                         
                        int index14_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 67;}

                        else if ( (synpred8_InternalGaml()) ) {s = 69;}

                        else if ( (synpred9_InternalGaml()) ) {s = 72;}

                        else if ( (true) ) {s = 73;}

                         
                        input.seek(index14_60);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_7s = "\60\uffff";
    static final String dfa_8s = "\1\4\55\43\2\uffff";
    static final String dfa_9s = "\1\133\55\u0086\2\uffff";
    static final String dfa_10s = "\56\uffff\1\2\1\1";
    static final String dfa_11s = "\60\uffff}>";
    static final String[] dfa_12s = {
            "\1\55\53\uffff\1\1\1\2\1\54\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\3\1\4\1\5",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "\1\57\5\uffff\1\56\134\uffff\1\57",
            "",
            ""
    };

    static final short[] dfa_7 = DFA.unpackEncodedString(dfa_7s);
    static final char[] dfa_8 = DFA.unpackEncodedStringToUnsignedChars(dfa_8s);
    static final char[] dfa_9 = DFA.unpackEncodedStringToUnsignedChars(dfa_9s);
    static final short[] dfa_10 = DFA.unpackEncodedString(dfa_10s);
    static final short[] dfa_11 = DFA.unpackEncodedString(dfa_11s);
    static final short[][] dfa_12 = unpackEncodedStringArray(dfa_12s);

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_8;
            this.max = dfa_9;
            this.accept = dfa_10;
            this.special = dfa_11;
            this.transition = dfa_12;
        }
        public String getDescription() {
            return "2285:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )";
        }
    }
    static final String dfa_13s = "\131\uffff";
    static final String dfa_14s = "\1\4\61\uffff\1\0\2\uffff\1\0\43\uffff";
    static final String dfa_15s = "\1\u008e\61\uffff\1\0\2\uffff\1\0\43\uffff";
    static final String dfa_16s = "\1\uffff\61\1\1\uffff\2\1\1\uffff\4\1\1\2\36\uffff";
    static final String dfa_17s = "\1\0\61\uffff\1\1\2\uffff\1\2\43\uffff}>";
    static final String[] dfa_18s = {
            "\1\62\1\1\1\2\1\3\1\4\1\5\5\uffff\1\72\4\uffff\1\64\2\uffff\1\72\11\uffff\1\72\1\uffff\1\63\3\uffff\1\65\10\uffff\1\6\1\7\1\61\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\10\1\11\1\12\7\uffff\34\72\11\uffff\1\67\3\uffff\1\66\1\70\1\71",
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
            ""
    };

    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final char[] dfa_14 = DFA.unpackEncodedStringToUnsignedChars(dfa_14s);
    static final char[] dfa_15 = DFA.unpackEncodedStringToUnsignedChars(dfa_15s);
    static final short[] dfa_16 = DFA.unpackEncodedString(dfa_16s);
    static final short[] dfa_17 = DFA.unpackEncodedString(dfa_17s);
    static final short[][] dfa_18 = unpackEncodedStringArray(dfa_18s);

    class DFA38 extends DFA {

        public DFA38(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 38;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_14;
            this.max = dfa_15;
            this.accept = dfa_16;
            this.special = dfa_17;
            this.transition = dfa_18;
        }
        public String getDescription() {
            return "3004:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA38_0 = input.LA(1);

                         
                        int index38_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_0==RULE_STRING) && (synpred13_InternalGaml())) {s = 1;}

                        else if ( (LA38_0==RULE_INTEGER) && (synpred13_InternalGaml())) {s = 2;}

                        else if ( (LA38_0==RULE_DOUBLE) && (synpred13_InternalGaml())) {s = 3;}

                        else if ( (LA38_0==RULE_BOOLEAN) && (synpred13_InternalGaml())) {s = 4;}

                        else if ( (LA38_0==RULE_KEYWORD) && (synpred13_InternalGaml())) {s = 5;}

                        else if ( (LA38_0==48) && (synpred13_InternalGaml())) {s = 6;}

                        else if ( (LA38_0==49) && (synpred13_InternalGaml())) {s = 7;}

                        else if ( (LA38_0==89) && (synpred13_InternalGaml())) {s = 8;}

                        else if ( (LA38_0==90) && (synpred13_InternalGaml())) {s = 9;}

                        else if ( (LA38_0==91) && (synpred13_InternalGaml())) {s = 10;}

                        else if ( (LA38_0==78) && (synpred13_InternalGaml())) {s = 11;}

                        else if ( (LA38_0==79) && (synpred13_InternalGaml())) {s = 12;}

                        else if ( (LA38_0==80) && (synpred13_InternalGaml())) {s = 13;}

                        else if ( (LA38_0==81) && (synpred13_InternalGaml())) {s = 14;}

                        else if ( (LA38_0==82) && (synpred13_InternalGaml())) {s = 15;}

                        else if ( (LA38_0==83) && (synpred13_InternalGaml())) {s = 16;}

                        else if ( (LA38_0==84) && (synpred13_InternalGaml())) {s = 17;}

                        else if ( (LA38_0==85) && (synpred13_InternalGaml())) {s = 18;}

                        else if ( (LA38_0==86) && (synpred13_InternalGaml())) {s = 19;}

                        else if ( (LA38_0==87) && (synpred13_InternalGaml())) {s = 20;}

                        else if ( (LA38_0==88) && (synpred13_InternalGaml())) {s = 21;}

                        else if ( (LA38_0==51) && (synpred13_InternalGaml())) {s = 22;}

                        else if ( (LA38_0==52) && (synpred13_InternalGaml())) {s = 23;}

                        else if ( (LA38_0==53) && (synpred13_InternalGaml())) {s = 24;}

                        else if ( (LA38_0==54) && (synpred13_InternalGaml())) {s = 25;}

                        else if ( (LA38_0==55) && (synpred13_InternalGaml())) {s = 26;}

                        else if ( (LA38_0==56) && (synpred13_InternalGaml())) {s = 27;}

                        else if ( (LA38_0==57) && (synpred13_InternalGaml())) {s = 28;}

                        else if ( (LA38_0==58) && (synpred13_InternalGaml())) {s = 29;}

                        else if ( (LA38_0==59) && (synpred13_InternalGaml())) {s = 30;}

                        else if ( (LA38_0==60) && (synpred13_InternalGaml())) {s = 31;}

                        else if ( (LA38_0==61) && (synpred13_InternalGaml())) {s = 32;}

                        else if ( (LA38_0==62) && (synpred13_InternalGaml())) {s = 33;}

                        else if ( (LA38_0==63) && (synpred13_InternalGaml())) {s = 34;}

                        else if ( (LA38_0==64) && (synpred13_InternalGaml())) {s = 35;}

                        else if ( (LA38_0==65) && (synpred13_InternalGaml())) {s = 36;}

                        else if ( (LA38_0==66) && (synpred13_InternalGaml())) {s = 37;}

                        else if ( (LA38_0==67) && (synpred13_InternalGaml())) {s = 38;}

                        else if ( (LA38_0==68) && (synpred13_InternalGaml())) {s = 39;}

                        else if ( (LA38_0==69) && (synpred13_InternalGaml())) {s = 40;}

                        else if ( (LA38_0==70) && (synpred13_InternalGaml())) {s = 41;}

                        else if ( (LA38_0==71) && (synpred13_InternalGaml())) {s = 42;}

                        else if ( (LA38_0==72) && (synpred13_InternalGaml())) {s = 43;}

                        else if ( (LA38_0==73) && (synpred13_InternalGaml())) {s = 44;}

                        else if ( (LA38_0==74) && (synpred13_InternalGaml())) {s = 45;}

                        else if ( (LA38_0==75) && (synpred13_InternalGaml())) {s = 46;}

                        else if ( (LA38_0==76) && (synpred13_InternalGaml())) {s = 47;}

                        else if ( (LA38_0==77) && (synpred13_InternalGaml())) {s = 48;}

                        else if ( (LA38_0==50) && (synpred13_InternalGaml())) {s = 49;}

                        else if ( (LA38_0==RULE_ID) ) {s = 50;}

                        else if ( (LA38_0==35) && (synpred13_InternalGaml())) {s = 51;}

                        else if ( (LA38_0==20) && (synpred13_InternalGaml())) {s = 52;}

                        else if ( (LA38_0==39) ) {s = 53;}

                        else if ( (LA38_0==140) && (synpred13_InternalGaml())) {s = 54;}

                        else if ( (LA38_0==136) && (synpred13_InternalGaml())) {s = 55;}

                        else if ( (LA38_0==141) && (synpred13_InternalGaml())) {s = 56;}

                        else if ( (LA38_0==142) && (synpred13_InternalGaml())) {s = 57;}

                        else if ( (LA38_0==15||LA38_0==23||LA38_0==33||(LA38_0>=99 && LA38_0<=126)) ) {s = 58;}

                         
                        input.seek(index38_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA38_50 = input.LA(1);

                         
                        int index38_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_InternalGaml()) ) {s = 57;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index38_50);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA38_53 = input.LA(1);

                         
                        int index38_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_InternalGaml()) ) {s = 57;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index38_53);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 38, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_19s = "\12\uffff";
    static final String dfa_20s = "\1\17\2\uffff\1\135\6\uffff";
    static final String dfa_21s = "\1\141\2\uffff\1\137\6\uffff";
    static final String dfa_22s = "\1\uffff\1\1\1\2\1\uffff\1\4\1\6\1\7\1\10\1\5\1\3";
    static final String dfa_23s = "\12\uffff}>";
    static final String[] dfa_24s = {
            "\1\1\114\uffff\1\2\1\3\1\4\1\7\1\5\1\6",
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

    static final short[] dfa_19 = DFA.unpackEncodedString(dfa_19s);
    static final char[] dfa_20 = DFA.unpackEncodedStringToUnsignedChars(dfa_20s);
    static final char[] dfa_21 = DFA.unpackEncodedStringToUnsignedChars(dfa_21s);
    static final short[] dfa_22 = DFA.unpackEncodedString(dfa_22s);
    static final short[] dfa_23 = DFA.unpackEncodedString(dfa_23s);
    static final short[][] dfa_24 = unpackEncodedStringArray(dfa_24s);

    class DFA43 extends DFA {

        public DFA43(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 43;
            this.eot = dfa_19;
            this.eof = dfa_19;
            this.min = dfa_20;
            this.max = dfa_21;
            this.accept = dfa_22;
            this.special = dfa_23;
            this.transition = dfa_24;
        }
        public String getDescription() {
            return "3464:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )";
        }
    }
    static final String dfa_25s = "\73\uffff";
    static final String dfa_26s = "\1\4\64\uffff\1\0\5\uffff";
    static final String dfa_27s = "\1\u008e\64\uffff\1\0\5\uffff";
    static final String dfa_28s = "\1\uffff\64\1\1\uffff\4\1\1\2";
    static final String dfa_29s = "\1\0\64\uffff\1\1\5\uffff}>";
    static final String[] dfa_30s = {
            "\1\62\1\1\1\2\1\3\1\4\1\5\12\uffff\1\64\16\uffff\1\63\3\uffff\1\65\10\uffff\1\6\1\7\1\61\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\10\1\11\1\12\54\uffff\1\67\3\uffff\1\66\1\70\1\71",
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

    static final short[] dfa_25 = DFA.unpackEncodedString(dfa_25s);
    static final char[] dfa_26 = DFA.unpackEncodedStringToUnsignedChars(dfa_26s);
    static final char[] dfa_27 = DFA.unpackEncodedStringToUnsignedChars(dfa_27s);
    static final short[] dfa_28 = DFA.unpackEncodedString(dfa_28s);
    static final short[] dfa_29 = DFA.unpackEncodedString(dfa_29s);
    static final short[][] dfa_30 = unpackEncodedStringArray(dfa_30s);

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = dfa_25;
            this.eof = dfa_25;
            this.min = dfa_26;
            this.max = dfa_27;
            this.accept = dfa_28;
            this.special = dfa_29;
            this.transition = dfa_30;
        }
        public String getDescription() {
            return "4239:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA53_0 = input.LA(1);

                         
                        int index53_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA53_0==RULE_STRING) && (synpred15_InternalGaml())) {s = 1;}

                        else if ( (LA53_0==RULE_INTEGER) && (synpred15_InternalGaml())) {s = 2;}

                        else if ( (LA53_0==RULE_DOUBLE) && (synpred15_InternalGaml())) {s = 3;}

                        else if ( (LA53_0==RULE_BOOLEAN) && (synpred15_InternalGaml())) {s = 4;}

                        else if ( (LA53_0==RULE_KEYWORD) && (synpred15_InternalGaml())) {s = 5;}

                        else if ( (LA53_0==48) && (synpred15_InternalGaml())) {s = 6;}

                        else if ( (LA53_0==49) && (synpred15_InternalGaml())) {s = 7;}

                        else if ( (LA53_0==89) && (synpred15_InternalGaml())) {s = 8;}

                        else if ( (LA53_0==90) && (synpred15_InternalGaml())) {s = 9;}

                        else if ( (LA53_0==91) && (synpred15_InternalGaml())) {s = 10;}

                        else if ( (LA53_0==78) && (synpred15_InternalGaml())) {s = 11;}

                        else if ( (LA53_0==79) && (synpred15_InternalGaml())) {s = 12;}

                        else if ( (LA53_0==80) && (synpred15_InternalGaml())) {s = 13;}

                        else if ( (LA53_0==81) && (synpred15_InternalGaml())) {s = 14;}

                        else if ( (LA53_0==82) && (synpred15_InternalGaml())) {s = 15;}

                        else if ( (LA53_0==83) && (synpred15_InternalGaml())) {s = 16;}

                        else if ( (LA53_0==84) && (synpred15_InternalGaml())) {s = 17;}

                        else if ( (LA53_0==85) && (synpred15_InternalGaml())) {s = 18;}

                        else if ( (LA53_0==86) && (synpred15_InternalGaml())) {s = 19;}

                        else if ( (LA53_0==87) && (synpred15_InternalGaml())) {s = 20;}

                        else if ( (LA53_0==88) && (synpred15_InternalGaml())) {s = 21;}

                        else if ( (LA53_0==51) && (synpred15_InternalGaml())) {s = 22;}

                        else if ( (LA53_0==52) && (synpred15_InternalGaml())) {s = 23;}

                        else if ( (LA53_0==53) && (synpred15_InternalGaml())) {s = 24;}

                        else if ( (LA53_0==54) && (synpred15_InternalGaml())) {s = 25;}

                        else if ( (LA53_0==55) && (synpred15_InternalGaml())) {s = 26;}

                        else if ( (LA53_0==56) && (synpred15_InternalGaml())) {s = 27;}

                        else if ( (LA53_0==57) && (synpred15_InternalGaml())) {s = 28;}

                        else if ( (LA53_0==58) && (synpred15_InternalGaml())) {s = 29;}

                        else if ( (LA53_0==59) && (synpred15_InternalGaml())) {s = 30;}

                        else if ( (LA53_0==60) && (synpred15_InternalGaml())) {s = 31;}

                        else if ( (LA53_0==61) && (synpred15_InternalGaml())) {s = 32;}

                        else if ( (LA53_0==62) && (synpred15_InternalGaml())) {s = 33;}

                        else if ( (LA53_0==63) && (synpred15_InternalGaml())) {s = 34;}

                        else if ( (LA53_0==64) && (synpred15_InternalGaml())) {s = 35;}

                        else if ( (LA53_0==65) && (synpred15_InternalGaml())) {s = 36;}

                        else if ( (LA53_0==66) && (synpred15_InternalGaml())) {s = 37;}

                        else if ( (LA53_0==67) && (synpred15_InternalGaml())) {s = 38;}

                        else if ( (LA53_0==68) && (synpred15_InternalGaml())) {s = 39;}

                        else if ( (LA53_0==69) && (synpred15_InternalGaml())) {s = 40;}

                        else if ( (LA53_0==70) && (synpred15_InternalGaml())) {s = 41;}

                        else if ( (LA53_0==71) && (synpred15_InternalGaml())) {s = 42;}

                        else if ( (LA53_0==72) && (synpred15_InternalGaml())) {s = 43;}

                        else if ( (LA53_0==73) && (synpred15_InternalGaml())) {s = 44;}

                        else if ( (LA53_0==74) && (synpred15_InternalGaml())) {s = 45;}

                        else if ( (LA53_0==75) && (synpred15_InternalGaml())) {s = 46;}

                        else if ( (LA53_0==76) && (synpred15_InternalGaml())) {s = 47;}

                        else if ( (LA53_0==77) && (synpred15_InternalGaml())) {s = 48;}

                        else if ( (LA53_0==50) && (synpred15_InternalGaml())) {s = 49;}

                        else if ( (LA53_0==RULE_ID) && (synpred15_InternalGaml())) {s = 50;}

                        else if ( (LA53_0==35) && (synpred15_InternalGaml())) {s = 51;}

                        else if ( (LA53_0==20) && (synpred15_InternalGaml())) {s = 52;}

                        else if ( (LA53_0==39) ) {s = 53;}

                        else if ( (LA53_0==140) && (synpred15_InternalGaml())) {s = 54;}

                        else if ( (LA53_0==136) && (synpred15_InternalGaml())) {s = 55;}

                        else if ( (LA53_0==141) && (synpred15_InternalGaml())) {s = 56;}

                        else if ( (LA53_0==142) && (synpred15_InternalGaml())) {s = 57;}

                         
                        input.seek(index53_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA53_53 = input.LA(1);

                         
                        int index53_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_InternalGaml()) ) {s = 57;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index53_53);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 53, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_31s = "\1\4\2\0\70\uffff";
    static final String dfa_32s = "\1\u008e\2\0\70\uffff";
    static final String dfa_33s = "\3\uffff\1\2\66\uffff\1\1";
    static final String dfa_34s = "\1\uffff\1\0\1\1\70\uffff}>";
    static final String[] dfa_35s = {
            "\1\1\5\3\12\uffff\1\3\16\uffff\1\3\3\uffff\1\3\10\uffff\1\2\53\3\54\uffff\1\3\3\uffff\3\3",
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
            ""
    };
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final char[] dfa_32 = DFA.unpackEncodedStringToUnsignedChars(dfa_32s);
    static final short[] dfa_33 = DFA.unpackEncodedString(dfa_33s);
    static final short[] dfa_34 = DFA.unpackEncodedString(dfa_34s);
    static final short[][] dfa_35 = unpackEncodedStringArray(dfa_35s);

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = dfa_25;
            this.eof = dfa_25;
            this.min = dfa_31;
            this.max = dfa_32;
            this.accept = dfa_33;
            this.special = dfa_34;
            this.transition = dfa_35;
        }
        public String getDescription() {
            return "4336:3: ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA54_1 = input.LA(1);

                         
                        int index54_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_InternalGaml()) ) {s = 58;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index54_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA54_2 = input.LA(1);

                         
                        int index54_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_InternalGaml()) ) {s = 58;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index54_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 54, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_36s = "\1\4\55\0\2\uffff";
    static final String dfa_37s = "\1\133\55\0\2\uffff";
    static final String dfa_38s = "\56\uffff\1\1\1\2";
    static final String dfa_39s = "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\2\uffff}>";
    static final String[] dfa_40s = {
            "\1\55\53\uffff\1\1\1\2\1\54\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\3\1\4\1\5",
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
    static final char[] dfa_36 = DFA.unpackEncodedStringToUnsignedChars(dfa_36s);
    static final char[] dfa_37 = DFA.unpackEncodedStringToUnsignedChars(dfa_37s);
    static final short[] dfa_38 = DFA.unpackEncodedString(dfa_38s);
    static final short[] dfa_39 = DFA.unpackEncodedString(dfa_39s);
    static final short[][] dfa_40 = unpackEncodedStringArray(dfa_40s);

    class DFA80 extends DFA {

        public DFA80(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 80;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_36;
            this.max = dfa_37;
            this.accept = dfa_38;
            this.special = dfa_39;
            this.transition = dfa_40;
        }
        public String getDescription() {
            return "5906:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA80_1 = input.LA(1);

                         
                        int index80_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA80_2 = input.LA(1);

                         
                        int index80_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA80_3 = input.LA(1);

                         
                        int index80_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA80_4 = input.LA(1);

                         
                        int index80_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA80_5 = input.LA(1);

                         
                        int index80_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA80_6 = input.LA(1);

                         
                        int index80_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA80_7 = input.LA(1);

                         
                        int index80_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA80_8 = input.LA(1);

                         
                        int index80_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA80_9 = input.LA(1);

                         
                        int index80_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA80_10 = input.LA(1);

                         
                        int index80_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA80_11 = input.LA(1);

                         
                        int index80_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA80_12 = input.LA(1);

                         
                        int index80_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA80_13 = input.LA(1);

                         
                        int index80_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA80_14 = input.LA(1);

                         
                        int index80_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA80_15 = input.LA(1);

                         
                        int index80_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA80_16 = input.LA(1);

                         
                        int index80_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA80_17 = input.LA(1);

                         
                        int index80_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA80_18 = input.LA(1);

                         
                        int index80_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA80_19 = input.LA(1);

                         
                        int index80_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA80_20 = input.LA(1);

                         
                        int index80_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA80_21 = input.LA(1);

                         
                        int index80_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA80_22 = input.LA(1);

                         
                        int index80_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA80_23 = input.LA(1);

                         
                        int index80_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA80_24 = input.LA(1);

                         
                        int index80_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA80_25 = input.LA(1);

                         
                        int index80_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA80_26 = input.LA(1);

                         
                        int index80_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA80_27 = input.LA(1);

                         
                        int index80_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA80_28 = input.LA(1);

                         
                        int index80_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA80_29 = input.LA(1);

                         
                        int index80_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA80_30 = input.LA(1);

                         
                        int index80_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_30);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA80_31 = input.LA(1);

                         
                        int index80_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_31);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA80_32 = input.LA(1);

                         
                        int index80_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_32);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA80_33 = input.LA(1);

                         
                        int index80_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_33);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA80_34 = input.LA(1);

                         
                        int index80_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_34);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA80_35 = input.LA(1);

                         
                        int index80_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_35);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA80_36 = input.LA(1);

                         
                        int index80_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_36);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA80_37 = input.LA(1);

                         
                        int index80_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_37);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA80_38 = input.LA(1);

                         
                        int index80_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_38);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA80_39 = input.LA(1);

                         
                        int index80_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_39);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA80_40 = input.LA(1);

                         
                        int index80_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_40);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA80_41 = input.LA(1);

                         
                        int index80_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_41);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA80_42 = input.LA(1);

                         
                        int index80_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_42);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA80_43 = input.LA(1);

                         
                        int index80_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_43);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA80_44 = input.LA(1);

                         
                        int index80_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_44);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA80_45 = input.LA(1);

                         
                        int index80_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_InternalGaml()) ) {s = 46;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index80_45);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 80, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_41s = "\2\uffff\55\1\1\uffff";
    static final String dfa_42s = "\1\4\1\uffff\55\4\1\uffff";
    static final String dfa_43s = "\1\u008e\1\uffff\55\u008f\1\uffff";
    static final String dfa_44s = "\1\uffff\1\1\55\uffff\1\2";
    static final String[] dfa_45s = {
            "\1\56\5\1\12\uffff\1\1\14\uffff\1\57\1\uffff\1\1\3\uffff\1\1\10\uffff\1\2\1\3\1\55\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\4\1\5\1\6\7\uffff\33\57\12\uffff\1\1\3\uffff\3\1",
            "",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\14\uffff\1\57\2\1\4\uffff\1\1\6\uffff\54\1\1\uffff\1\1\4\uffff\1\1\34\uffff\16\1\2\uffff\1\1",
            ""
    };
    static final short[] dfa_41 = DFA.unpackEncodedString(dfa_41s);
    static final char[] dfa_42 = DFA.unpackEncodedStringToUnsignedChars(dfa_42s);
    static final char[] dfa_43 = DFA.unpackEncodedStringToUnsignedChars(dfa_43s);
    static final short[] dfa_44 = DFA.unpackEncodedString(dfa_44s);
    static final short[][] dfa_45 = unpackEncodedStringArray(dfa_45s);

    class DFA85 extends DFA {

        public DFA85(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 85;
            this.eot = dfa_7;
            this.eof = dfa_41;
            this.min = dfa_42;
            this.max = dfa_43;
            this.accept = dfa_44;
            this.special = dfa_11;
            this.transition = dfa_45;
        }
        public String getDescription() {
            return "6036:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )";
        }
    }
    static final String dfa_46s = "\21\uffff";
    static final String dfa_47s = "\1\4\2\0\16\uffff";
    static final String dfa_48s = "\1\u0094\2\0\16\uffff";
    static final String dfa_49s = "\3\uffff\6\1\1\2\1\uffff\1\4\1\uffff\1\5\1\6\1\7\1\3";
    static final String dfa_50s = "\1\0\1\1\1\2\16\uffff}>";
    static final String[] dfa_51s = {
            "\1\1\13\uffff\1\11\1\16\1\uffff\1\11\7\uffff\1\10\11\uffff\1\7\12\uffff\1\2\1\3\1\17\46\uffff\1\4\1\5\1\6\7\uffff\2\13\57\uffff\1\15",
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
            ""
    };

    static final short[] dfa_46 = DFA.unpackEncodedString(dfa_46s);
    static final char[] dfa_47 = DFA.unpackEncodedStringToUnsignedChars(dfa_47s);
    static final char[] dfa_48 = DFA.unpackEncodedStringToUnsignedChars(dfa_48s);
    static final short[] dfa_49 = DFA.unpackEncodedString(dfa_49s);
    static final short[] dfa_50 = DFA.unpackEncodedString(dfa_50s);
    static final short[][] dfa_51 = unpackEncodedStringArray(dfa_51s);

    class DFA93 extends DFA {

        public DFA93(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 93;
            this.eot = dfa_46;
            this.eof = dfa_46;
            this.min = dfa_47;
            this.max = dfa_48;
            this.accept = dfa_49;
            this.special = dfa_50;
            this.transition = dfa_51;
        }
        public String getDescription() {
            return "6714:2: ( ( ( ruleS_Declaration )=>this_S_Declaration_0= ruleS_Declaration ) | this_Model_1= ruleModel | this_ArgumentDefinition_2= ruleArgumentDefinition | this_DefinitionFacet_3= ruleDefinitionFacet | this_VarFakeDefinition_4= ruleVarFakeDefinition | this_Import_5= ruleImport | this_S_Experiment_6= ruleS_Experiment )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA93_0 = input.LA(1);

                         
                        int index93_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA93_0==RULE_ID) ) {s = 1;}

                        else if ( (LA93_0==48) ) {s = 2;}

                        else if ( (LA93_0==49) && (synpred19_InternalGaml())) {s = 3;}

                        else if ( (LA93_0==89) && (synpred19_InternalGaml())) {s = 4;}

                        else if ( (LA93_0==90) && (synpred19_InternalGaml())) {s = 5;}

                        else if ( (LA93_0==91) && (synpred19_InternalGaml())) {s = 6;}

                        else if ( (LA93_0==37) && (synpred19_InternalGaml())) {s = 7;}

                        else if ( (LA93_0==27) && (synpred19_InternalGaml())) {s = 8;}

                        else if ( (LA93_0==16||LA93_0==19) ) {s = 9;}

                        else if ( ((LA93_0>=99 && LA93_0<=100)) ) {s = 11;}

                        else if ( (LA93_0==148) ) {s = 13;}

                        else if ( (LA93_0==17) ) {s = 14;}

                        else if ( (LA93_0==50) ) {s = 15;}

                         
                        input.seek(index93_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA93_1 = input.LA(1);

                         
                        int index93_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 8;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index93_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA93_2 = input.LA(1);

                         
                        int index93_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_InternalGaml()) ) {s = 8;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index93_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 93, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0xFFFF0088001003F0L,0x000000000FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000090000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0xFFFF000000000010L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0007000001020000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0007000001000002L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0xFFFF008A003003F0L,0x3FFFFFF80FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0xFFFF000000000030L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000008200C08010L,0x7FFFFFF800000000L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000008200808010L,0x7FFFFFF800000000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000008200008010L,0x7FFFFFF800000000L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000008010000000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0xFFFF0088009003F0L,0x000000000FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0xFFFF008200000010L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000008200000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0000008A00C08010L,0x7FFFFFF800000000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0001000000000010L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0000000000008000L,0x00000003F0000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000000200808010L,0x7FFFFFF800000000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0xFFFF010000000010L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0003790000000000L,0x0000000001FFC000L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0xFFFF008A00D083F0L,0x7FFFFFF80FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0xFFFF008000000010L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0xFFFF85E95E1003F0L,0x000000000FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000002L,0x8000000000000000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0001000800000010L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000020000000002L,0x0000000020000000L,0x0000000000000078L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000E00L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0xFFFF000000000012L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000100002L,0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0xFFFF0088001003F0L,0x000000000FFFFFFFL});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0xFFFF008A001003F0L,0x3FFFFFF80FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000010000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0xFFFF009A001003F0L,0x3FFFFFF80FFFFFFFL,0x0000000000007100L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_63 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_64 = new BitSet(new long[]{0x0000000000000000L,0x0000000420000000L});

}