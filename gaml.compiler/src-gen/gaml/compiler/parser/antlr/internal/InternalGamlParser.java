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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_STRING", "RULE_INTEGER", "RULE_DOUBLE", "RULE_BOOLEAN", "RULE_KEYWORD", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'__synthetic__'", "'<-'", "'model:'", "'model'", "'import'", "'as'", "'@'", "'['", "']'", "';'", "'global'", "'do'", "'invoke'", "'loop'", "'if'", "'else'", "'try'", "'catch'", "'return'", "'('", "')'", "'action'", "'equation'", "'{'", "'}'", "'='", "'solve'", "'display'", "'species'", "'grid'", "'experiment'", "'ask'", "'release'", "'capture'", "'create'", "'write'", "'error'", "'warn'", "'exception'", "'save'", "'assert'", "'inspect'", "'browse'", "'restore'", "'draw'", "'using'", "'switch'", "'put'", "'add'", "'remove'", "'match'", "'match_between'", "'match_one'", "'parameter'", "'status'", "'highlight'", "'focus_on'", "'layout'", "'init'", "'reflex'", "'<<'", "'>'", "'<<+'", "'>-'", "'+<-'", "'<+'", "','", "':'", "'name:'", "'returns:'", "'as:'", "'of:'", "'parent:'", "'species:'", "'type:'", "'camera:'", "'data:'", "'when'", "'const:'", "'value:'", "'topology:'", "'item:'", "'init:'", "'message:'", "'control:'", "'layout:'", "'environment:'", "'text:'", "'image:'", "'using:'", "'parameter:'", "'aspect:'", "'light:'", "'action:'", "'on_change:'", "'var:'", "'->'", "'::'", "'?'", "'or'", "'and'", "'!='", "'>='", "'<='", "'<'", "'+'", "'-'", "'*'", "'/'", "'^'", "'#'", "'!'", "'not'", "'.'", "'**unit*'", "'**type*'", "'**action*'", "'**skill*'", "'**var*'", "'**equation*'"
    };
    public static final int T__50=50;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__133=133;
    public static final int T__132=132;
    public static final int T__60=60;
    public static final int T__61=61;
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
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
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
    // InternalGaml.g:71:1: ruleEntry returns [EObject current=null] : ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StandaloneExpression_1= ruleStandaloneExpression | this_StandaloneBlock_2= ruleStandaloneBlock | this_StandaloneExperiment_3= ruleStandaloneExperiment ) ;
    public final EObject ruleEntry() throws RecognitionException {
        EObject current = null;

        EObject this_Model_0 = null;

        EObject this_StandaloneExpression_1 = null;

        EObject this_StandaloneBlock_2 = null;

        EObject this_StandaloneExperiment_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:77:2: ( ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StandaloneExpression_1= ruleStandaloneExpression | this_StandaloneBlock_2= ruleStandaloneBlock | this_StandaloneExperiment_3= ruleStandaloneExperiment ) )
            // InternalGaml.g:78:2: ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StandaloneExpression_1= ruleStandaloneExpression | this_StandaloneBlock_2= ruleStandaloneBlock | this_StandaloneExperiment_3= ruleStandaloneExperiment )
            {
            // InternalGaml.g:78:2: ( ( ( '@' | 'model' )=>this_Model_0= ruleModel ) | this_StandaloneExpression_1= ruleStandaloneExpression | this_StandaloneBlock_2= ruleStandaloneBlock | this_StandaloneExperiment_3= ruleStandaloneExperiment )
            int alt1=4;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==20) && (synpred1_InternalGaml())) {
                alt1=1;
            }
            else if ( (LA1_0==17) && (synpred1_InternalGaml())) {
                alt1=1;
            }
            else if ( (LA1_0==RULE_ID) ) {
                alt1=2;
            }
            else if ( (LA1_0==14) ) {
                alt1=3;
            }
            else if ( (LA1_0==44) ) {
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
                    // InternalGaml.g:91:3: this_StandaloneExpression_1= ruleStandaloneExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEntryAccess().getStandaloneExpressionParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StandaloneExpression_1=ruleStandaloneExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StandaloneExpression_1;
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
                    // InternalGaml.g:109:3: this_StandaloneExperiment_3= ruleStandaloneExperiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getEntryAccess().getStandaloneExperimentParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StandaloneExperiment_3=ruleStandaloneExperiment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StandaloneExperiment_3;
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


    // $ANTLR start "entryRuleStandaloneExpression"
    // InternalGaml.g:163:1: entryRuleStandaloneExpression returns [EObject current=null] : iv_ruleStandaloneExpression= ruleStandaloneExpression EOF ;
    public final EObject entryRuleStandaloneExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStandaloneExpression = null;


        try {
            // InternalGaml.g:163:61: (iv_ruleStandaloneExpression= ruleStandaloneExpression EOF )
            // InternalGaml.g:164:2: iv_ruleStandaloneExpression= ruleStandaloneExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStandaloneExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStandaloneExpression=ruleStandaloneExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStandaloneExpression; 
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
    // $ANTLR end "entryRuleStandaloneExpression"


    // $ANTLR start "ruleStandaloneExpression"
    // InternalGaml.g:170:1: ruleStandaloneExpression returns [EObject current=null] : ( ( (lv_identifier_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) ) ;
    public final EObject ruleStandaloneExpression() throws RecognitionException {
        EObject current = null;

        Token lv_identifier_0_0=null;
        Token otherlv_1=null;
        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:176:2: ( ( ( (lv_identifier_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:177:2: ( ( (lv_identifier_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:177:2: ( ( (lv_identifier_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) ) )
            // InternalGaml.g:178:3: ( (lv_identifier_0_0= RULE_ID ) ) otherlv_1= '<-' ( (lv_expr_2_0= ruleExpression ) )
            {
            // InternalGaml.g:178:3: ( (lv_identifier_0_0= RULE_ID ) )
            // InternalGaml.g:179:4: (lv_identifier_0_0= RULE_ID )
            {
            // InternalGaml.g:179:4: (lv_identifier_0_0= RULE_ID )
            // InternalGaml.g:180:5: lv_identifier_0_0= RULE_ID
            {
            lv_identifier_0_0=(Token)match(input,RULE_ID,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_identifier_0_0, grammarAccess.getStandaloneExpressionAccess().getIdentifierIDTerminalRuleCall_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getStandaloneExpressionRule());
              					}
              					setWithLastConsumed(
              						current,
              						"identifier",
              						lv_identifier_0_0,
              						"gaml.compiler.Gaml.ID");
              				
            }

            }


            }

            otherlv_1=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getStandaloneExpressionAccess().getLessThanSignHyphenMinusKeyword_1());
              		
            }
            // InternalGaml.g:200:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:201:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:201:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:202:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStandaloneExpressionAccess().getExprExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getStandaloneExpressionRule());
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
    // $ANTLR end "ruleStandaloneExpression"


    // $ANTLR start "entryRuleStandaloneExperiment"
    // InternalGaml.g:223:1: entryRuleStandaloneExperiment returns [EObject current=null] : iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF ;
    public final EObject entryRuleStandaloneExperiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStandaloneExperiment = null;


        try {
            // InternalGaml.g:223:61: (iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF )
            // InternalGaml.g:224:2: iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStandaloneExperimentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStandaloneExperiment=ruleStandaloneExperiment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStandaloneExperiment; 
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
    // $ANTLR end "entryRuleStandaloneExperiment"


    // $ANTLR start "ruleStandaloneExperiment"
    // InternalGaml.g:230:1: ruleStandaloneExperiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleStandaloneExperiment() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        Token otherlv_2=null;
        Token lv_importURI_3_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject this_FacetsAndBlock_4 = null;



        	enterRule();

        try {
            // InternalGaml.g:236:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:237:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:237:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:238:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:238:3: ( (lv_key_0_0= rule_ExperimentKey ) )
            // InternalGaml.g:239:4: (lv_key_0_0= rule_ExperimentKey )
            {
            // InternalGaml.g:239:4: (lv_key_0_0= rule_ExperimentKey )
            // InternalGaml.g:240:5: lv_key_0_0= rule_ExperimentKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStandaloneExperimentAccess().getKey_ExperimentKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_6);
            lv_key_0_0=rule_ExperimentKey();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getStandaloneExperimentRule());
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

            // InternalGaml.g:257:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:258:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:258:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:259:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:259:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==RULE_ID||(LA2_0>=42 && LA2_0<=72)) ) {
                alt2=1;
            }
            else if ( (LA2_0==RULE_STRING) ) {
                alt2=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // InternalGaml.g:260:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStandaloneExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_1_1=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStandaloneExperimentRule());
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
                    // InternalGaml.g:276:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_name_1_2, grammarAccess.getStandaloneExperimentAccess().getNameSTRINGTerminalRuleCall_1_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getStandaloneExperimentRule());
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

            // InternalGaml.g:293:3: (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==16) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // InternalGaml.g:294:4: otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) )
                    {
                    otherlv_2=(Token)match(input,16,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getStandaloneExperimentAccess().getModelKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:298:4: ( (lv_importURI_3_0= RULE_STRING ) )
                    // InternalGaml.g:299:5: (lv_importURI_3_0= RULE_STRING )
                    {
                    // InternalGaml.g:299:5: (lv_importURI_3_0= RULE_STRING )
                    // InternalGaml.g:300:6: lv_importURI_3_0= RULE_STRING
                    {
                    lv_importURI_3_0=(Token)match(input,RULE_STRING,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_importURI_3_0, grammarAccess.getStandaloneExperimentAccess().getImportURISTRINGTerminalRuleCall_2_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getStandaloneExperimentRule());
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
              				current = createModelElement(grammarAccess.getStandaloneExperimentRule());
              			}
              			newCompositeNode(grammarAccess.getStandaloneExperimentAccess().getFacetsAndBlockParserRuleCall_3());
              		
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
    // $ANTLR end "ruleStandaloneExperiment"


    // $ANTLR start "entryRuleModel"
    // InternalGaml.g:332:1: entryRuleModel returns [EObject current=null] : iv_ruleModel= ruleModel EOF ;
    public final EObject entryRuleModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModel = null;


        try {
            // InternalGaml.g:332:46: (iv_ruleModel= ruleModel EOF )
            // InternalGaml.g:333:2: iv_ruleModel= ruleModel EOF
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
    // InternalGaml.g:339:1: ruleModel returns [EObject current=null] : ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) ;
    public final EObject ruleModel() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_pragmas_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject lv_imports_3_0 = null;

        EObject lv_block_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:345:2: ( ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) )
            // InternalGaml.g:346:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            {
            // InternalGaml.g:346:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            // InternalGaml.g:347:3: ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) )
            {
            // InternalGaml.g:347:3: ( (lv_pragmas_0_0= rulePragma ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==20) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalGaml.g:348:4: (lv_pragmas_0_0= rulePragma )
            	    {
            	    // InternalGaml.g:348:4: (lv_pragmas_0_0= rulePragma )
            	    // InternalGaml.g:349:5: lv_pragmas_0_0= rulePragma
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getPragmasPragmaParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_9);
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
            	    break loop4;
                }
            } while (true);

            otherlv_1=(Token)match(input,17,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getModelAccess().getModelKeyword_1());
              		
            }
            // InternalGaml.g:370:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:371:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:371:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:372:5: lv_name_2_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getModelAccess().getNameValid_IDParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_11);
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

            // InternalGaml.g:389:3: ( (lv_imports_3_0= ruleImport ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==18) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // InternalGaml.g:390:4: (lv_imports_3_0= ruleImport )
            	    {
            	    // InternalGaml.g:390:4: (lv_imports_3_0= ruleImport )
            	    // InternalGaml.g:391:5: lv_imports_3_0= ruleImport
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getImportsImportParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_11);
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
            	    break loop5;
                }
            } while (true);

            // InternalGaml.g:408:3: ( (lv_block_4_0= ruleModelBlock ) )
            // InternalGaml.g:409:4: (lv_block_4_0= ruleModelBlock )
            {
            // InternalGaml.g:409:4: (lv_block_4_0= ruleModelBlock )
            // InternalGaml.g:410:5: lv_block_4_0= ruleModelBlock
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


    // $ANTLR start "entryRuleImport"
    // InternalGaml.g:431:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // InternalGaml.g:431:47: (iv_ruleImport= ruleImport EOF )
            // InternalGaml.g:432:2: iv_ruleImport= ruleImport EOF
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
    // InternalGaml.g:438:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_importURI_1_0=null;
        Token otherlv_2=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:444:2: ( (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) )
            // InternalGaml.g:445:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            {
            // InternalGaml.g:445:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            // InternalGaml.g:446:3: otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            {
            otherlv_0=(Token)match(input,18,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
              		
            }
            // InternalGaml.g:450:3: ( (lv_importURI_1_0= RULE_STRING ) )
            // InternalGaml.g:451:4: (lv_importURI_1_0= RULE_STRING )
            {
            // InternalGaml.g:451:4: (lv_importURI_1_0= RULE_STRING )
            // InternalGaml.g:452:5: lv_importURI_1_0= RULE_STRING
            {
            lv_importURI_1_0=(Token)match(input,RULE_STRING,FOLLOW_12); if (state.failed) return current;
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

            // InternalGaml.g:468:3: (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==19) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // InternalGaml.g:469:4: otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) )
                    {
                    otherlv_2=(Token)match(input,19,FOLLOW_10); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getImportAccess().getAsKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:473:4: ( (lv_name_3_0= ruleValid_ID ) )
                    // InternalGaml.g:474:5: (lv_name_3_0= ruleValid_ID )
                    {
                    // InternalGaml.g:474:5: (lv_name_3_0= ruleValid_ID )
                    // InternalGaml.g:475:6: lv_name_3_0= ruleValid_ID
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
    // InternalGaml.g:497:1: entryRulePragma returns [EObject current=null] : iv_rulePragma= rulePragma EOF ;
    public final EObject entryRulePragma() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePragma = null;


        try {
            // InternalGaml.g:497:47: (iv_rulePragma= rulePragma EOF )
            // InternalGaml.g:498:2: iv_rulePragma= rulePragma EOF
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
    // InternalGaml.g:504:1: rulePragma returns [EObject current=null] : (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) ;
    public final EObject rulePragma() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_plugins_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:510:2: ( (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) )
            // InternalGaml.g:511:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            {
            // InternalGaml.g:511:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            // InternalGaml.g:512:3: otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            {
            otherlv_0=(Token)match(input,20,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getPragmaAccess().getCommercialAtKeyword_0());
              		
            }
            // InternalGaml.g:516:3: ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            // InternalGaml.g:517:4: ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            {
            // InternalGaml.g:517:4: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:518:5: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:518:5: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:519:6: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_14); if (state.failed) return current;
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

            // InternalGaml.g:535:4: (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==21) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // InternalGaml.g:536:5: otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']'
                    {
                    otherlv_2=(Token)match(input,21,FOLLOW_15); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getPragmaAccess().getLeftSquareBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:540:5: ( (lv_plugins_3_0= ruleExpressionList ) )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( ((LA7_0>=RULE_ID && LA7_0<=RULE_KEYWORD)||LA7_0==21||LA7_0==33||LA7_0==37||(LA7_0>=42 && LA7_0<=72)||(LA7_0>=82 && LA7_0<=109)||LA7_0==120||(LA7_0>=124 && LA7_0<=126)) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // InternalGaml.g:541:6: (lv_plugins_3_0= ruleExpressionList )
                            {
                            // InternalGaml.g:541:6: (lv_plugins_3_0= ruleExpressionList )
                            // InternalGaml.g:542:7: lv_plugins_3_0= ruleExpressionList
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPragmaAccess().getPluginsExpressionListParserRuleCall_1_1_1_0());
                              						
                            }
                            pushFollow(FOLLOW_16);
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

                    otherlv_4=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "ruleFacetsAndBlock"
    // InternalGaml.g:570:1: ruleFacetsAndBlock[EObject in_current] returns [EObject current=in_current] : ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) ;
    public final EObject ruleFacetsAndBlock(EObject in_current) throws RecognitionException {
        EObject current = in_current;

        Token otherlv_2=null;
        EObject lv_facets_0_0 = null;

        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:576:2: ( ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) )
            // InternalGaml.g:577:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            {
            // InternalGaml.g:577:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            // InternalGaml.g:578:3: ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            {
            // InternalGaml.g:578:3: ( (lv_facets_0_0= ruleFacet ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==RULE_ID||LA9_0==15||(LA9_0>=82 && LA9_0<=110)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // InternalGaml.g:579:4: (lv_facets_0_0= ruleFacet )
            	    {
            	    // InternalGaml.g:579:4: (lv_facets_0_0= ruleFacet )
            	    // InternalGaml.g:580:5: lv_facets_0_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getFacetsAndBlockAccess().getFacetsFacetParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
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
            	    break loop9;
                }
            } while (true);

            // InternalGaml.g:597:3: ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==37) ) {
                alt10=1;
            }
            else if ( (LA10_0==23) ) {
                alt10=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // InternalGaml.g:598:4: ( (lv_block_1_0= ruleBlock ) )
                    {
                    // InternalGaml.g:598:4: ( (lv_block_1_0= ruleBlock ) )
                    // InternalGaml.g:599:5: (lv_block_1_0= ruleBlock )
                    {
                    // InternalGaml.g:599:5: (lv_block_1_0= ruleBlock )
                    // InternalGaml.g:600:6: lv_block_1_0= ruleBlock
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
                    // InternalGaml.g:618:4: otherlv_2= ';'
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
    // InternalGaml.g:627:1: entryRuleS_Section returns [EObject current=null] : iv_ruleS_Section= ruleS_Section EOF ;
    public final EObject entryRuleS_Section() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Section = null;


        try {
            // InternalGaml.g:627:50: (iv_ruleS_Section= ruleS_Section EOF )
            // InternalGaml.g:628:2: iv_ruleS_Section= ruleS_Section EOF
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
    // InternalGaml.g:634:1: ruleS_Section returns [EObject current=null] : (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) ;
    public final EObject ruleS_Section() throws RecognitionException {
        EObject current = null;

        EObject this_S_Global_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Experiment_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:640:2: ( (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) )
            // InternalGaml.g:641:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            {
            // InternalGaml.g:641:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            int alt11=3;
            switch ( input.LA(1) ) {
            case 24:
                {
                alt11=1;
                }
                break;
            case 42:
            case 43:
                {
                alt11=2;
                }
                break;
            case 44:
                {
                alt11=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // InternalGaml.g:642:3: this_S_Global_0= ruleS_Global
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
                    // InternalGaml.g:651:3: this_S_Species_1= ruleS_Species
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
                    // InternalGaml.g:660:3: this_S_Experiment_2= ruleS_Experiment
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
    // InternalGaml.g:672:1: entryRuleS_Global returns [EObject current=null] : iv_ruleS_Global= ruleS_Global EOF ;
    public final EObject entryRuleS_Global() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Global = null;


        try {
            // InternalGaml.g:672:49: (iv_ruleS_Global= ruleS_Global EOF )
            // InternalGaml.g:673:2: iv_ruleS_Global= ruleS_Global EOF
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
    // InternalGaml.g:679:1: ruleS_Global returns [EObject current=null] : ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Global() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:685:2: ( ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:686:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:686:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:687:3: ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:687:3: ( (lv_key_0_0= 'global' ) )
            // InternalGaml.g:688:4: (lv_key_0_0= 'global' )
            {
            // InternalGaml.g:688:4: (lv_key_0_0= 'global' )
            // InternalGaml.g:689:5: lv_key_0_0= 'global'
            {
            lv_key_0_0=(Token)match(input,24,FOLLOW_7); if (state.failed) return current;
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
    // InternalGaml.g:716:1: entryRuleS_Species returns [EObject current=null] : iv_ruleS_Species= ruleS_Species EOF ;
    public final EObject entryRuleS_Species() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Species = null;


        try {
            // InternalGaml.g:716:50: (iv_ruleS_Species= ruleS_Species EOF )
            // InternalGaml.g:717:2: iv_ruleS_Species= ruleS_Species EOF
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
    // InternalGaml.g:723:1: ruleS_Species returns [EObject current=null] : ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Species() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:729:2: ( ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:730:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:730:2: ( ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:731:3: ( (lv_key_0_0= rule_SpeciesKey ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:731:3: ( (lv_key_0_0= rule_SpeciesKey ) )
            // InternalGaml.g:732:4: (lv_key_0_0= rule_SpeciesKey )
            {
            // InternalGaml.g:732:4: (lv_key_0_0= rule_SpeciesKey )
            // InternalGaml.g:733:5: lv_key_0_0= rule_SpeciesKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SpeciesAccess().getKey_SpeciesKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_13);
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

            // InternalGaml.g:750:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:751:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:751:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:752:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_7); if (state.failed) return current;
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
    // InternalGaml.g:783:1: entryRuleS_Experiment returns [EObject current=null] : iv_ruleS_Experiment= ruleS_Experiment EOF ;
    public final EObject entryRuleS_Experiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Experiment = null;


        try {
            // InternalGaml.g:783:53: (iv_ruleS_Experiment= ruleS_Experiment EOF )
            // InternalGaml.g:784:2: iv_ruleS_Experiment= ruleS_Experiment EOF
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
    // InternalGaml.g:790:1: ruleS_Experiment returns [EObject current=null] : ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Experiment() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:796:2: ( ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:797:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:797:2: ( ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:798:3: ( (lv_key_0_0= rule_ExperimentKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:798:3: ( (lv_key_0_0= rule_ExperimentKey ) )
            // InternalGaml.g:799:4: (lv_key_0_0= rule_ExperimentKey )
            {
            // InternalGaml.g:799:4: (lv_key_0_0= rule_ExperimentKey )
            // InternalGaml.g:800:5: lv_key_0_0= rule_ExperimentKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ExperimentAccess().getKey_ExperimentKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_6);
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

            // InternalGaml.g:817:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:818:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:818:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:819:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:819:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==RULE_ID||(LA12_0>=42 && LA12_0<=72)) ) {
                alt12=1;
            }
            else if ( (LA12_0==RULE_STRING) ) {
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
                    // InternalGaml.g:820:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
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
                    // InternalGaml.g:836:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_7); if (state.failed) return current;
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
    // InternalGaml.g:868:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalGaml.g:868:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalGaml.g:869:2: iv_ruleStatement= ruleStatement EOF
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
    // InternalGaml.g:875:1: ruleStatement returns [EObject current=null] : (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition ) | this_S_Other_13= ruleS_Other ) ;
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

        EObject this_S_Definition_12 = null;

        EObject this_S_Other_13 = null;



        	enterRule();

        try {
            // InternalGaml.g:881:2: ( (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition ) | this_S_Other_13= ruleS_Other ) )
            // InternalGaml.g:882:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition ) | this_S_Other_13= ruleS_Other )
            {
            // InternalGaml.g:882:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition ) | this_S_Other_13= ruleS_Other )
            int alt13=14;
            alt13 = dfa13.predict(input);
            switch (alt13) {
                case 1 :
                    // InternalGaml.g:883:3: this_S_Display_0= ruleS_Display
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
                    // InternalGaml.g:892:3: this_S_Return_1= ruleS_Return
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
                    // InternalGaml.g:901:3: this_S_Solve_2= ruleS_Solve
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
                    // InternalGaml.g:910:3: this_S_If_3= ruleS_If
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
                    // InternalGaml.g:919:3: this_S_Try_4= ruleS_Try
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
                    // InternalGaml.g:928:3: this_S_Do_5= ruleS_Do
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
                    // InternalGaml.g:937:3: this_S_Loop_6= ruleS_Loop
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
                    // InternalGaml.g:946:3: ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations )
                    {
                    // InternalGaml.g:946:3: ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations )
                    // InternalGaml.g:947:4: ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations
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
                    // InternalGaml.g:958:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    {
                    // InternalGaml.g:958:3: ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action )
                    // InternalGaml.g:959:4: ( ruleS_Action )=>this_S_Action_8= ruleS_Action
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
                    // InternalGaml.g:970:3: ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species )
                    {
                    // InternalGaml.g:970:3: ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species )
                    // InternalGaml.g:971:4: ( ruleS_Species )=>this_S_Species_9= ruleS_Species
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
                    // InternalGaml.g:982:3: ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex )
                    {
                    // InternalGaml.g:982:3: ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex )
                    // InternalGaml.g:983:4: ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex
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
                    // InternalGaml.g:994:3: ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment )
                    {
                    // InternalGaml.g:994:3: ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment )
                    // InternalGaml.g:995:4: ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment
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
                    // InternalGaml.g:1006:3: ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition )
                    {
                    // InternalGaml.g:1006:3: ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition )
                    // InternalGaml.g:1007:4: ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DefinitionParserRuleCall_12());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_12=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Definition_12;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 14 :
                    // InternalGaml.g:1018:3: this_S_Other_13= ruleS_Other
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_OtherParserRuleCall_13());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Other_13=ruleS_Other();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Other_13;
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


    // $ANTLR start "entryRuleS_Do"
    // InternalGaml.g:1030:1: entryRuleS_Do returns [EObject current=null] : iv_ruleS_Do= ruleS_Do EOF ;
    public final EObject entryRuleS_Do() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Do = null;


        try {
            // InternalGaml.g:1030:45: (iv_ruleS_Do= ruleS_Do EOF )
            // InternalGaml.g:1031:2: iv_ruleS_Do= ruleS_Do EOF
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
    // InternalGaml.g:1037:1: ruleS_Do returns [EObject current=null] : ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Do() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_1=null;
        Token lv_key_0_2=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1043:2: ( ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1044:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1044:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1045:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1045:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) )
            // InternalGaml.g:1046:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            {
            // InternalGaml.g:1046:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            // InternalGaml.g:1047:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            {
            // InternalGaml.g:1047:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==25) ) {
                alt14=1;
            }
            else if ( (LA14_0==26) ) {
                alt14=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // InternalGaml.g:1048:6: lv_key_0_1= 'do'
                    {
                    lv_key_0_1=(Token)match(input,25,FOLLOW_10); if (state.failed) return current;
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
                    // InternalGaml.g:1059:6: lv_key_0_2= 'invoke'
                    {
                    lv_key_0_2=(Token)match(input,26,FOLLOW_10); if (state.failed) return current;
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

            // InternalGaml.g:1072:3: ( (lv_expr_1_0= ruleAbstractRef ) )
            // InternalGaml.g:1073:4: (lv_expr_1_0= ruleAbstractRef )
            {
            // InternalGaml.g:1073:4: (lv_expr_1_0= ruleAbstractRef )
            // InternalGaml.g:1074:5: lv_expr_1_0= ruleAbstractRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DoAccess().getExprAbstractRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_7);
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
    // InternalGaml.g:1106:1: entryRuleS_Loop returns [EObject current=null] : iv_ruleS_Loop= ruleS_Loop EOF ;
    public final EObject entryRuleS_Loop() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Loop = null;


        try {
            // InternalGaml.g:1106:47: (iv_ruleS_Loop= ruleS_Loop EOF )
            // InternalGaml.g:1107:2: iv_ruleS_Loop= ruleS_Loop EOF
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
    // InternalGaml.g:1113:1: ruleS_Loop returns [EObject current=null] : ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Loop() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_0=null;
        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1119:2: ( ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) )
            // InternalGaml.g:1120:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1120:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            // InternalGaml.g:1121:3: ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) )
            {
            // InternalGaml.g:1121:3: ( (lv_key_0_0= 'loop' ) )
            // InternalGaml.g:1122:4: (lv_key_0_0= 'loop' )
            {
            // InternalGaml.g:1122:4: (lv_key_0_0= 'loop' )
            // InternalGaml.g:1123:5: lv_key_0_0= 'loop'
            {
            lv_key_0_0=(Token)match(input,27,FOLLOW_18); if (state.failed) return current;
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

            // InternalGaml.g:1135:3: ( (lv_name_1_0= RULE_ID ) )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==RULE_ID) ) {
                int LA15_1 = input.LA(2);

                if ( (LA15_1==RULE_ID||LA15_1==15||LA15_1==37||(LA15_1>=82 && LA15_1<=110)) ) {
                    alt15=1;
                }
            }
            switch (alt15) {
                case 1 :
                    // InternalGaml.g:1136:4: (lv_name_1_0= RULE_ID )
                    {
                    // InternalGaml.g:1136:4: (lv_name_1_0= RULE_ID )
                    // InternalGaml.g:1137:5: lv_name_1_0= RULE_ID
                    {
                    lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_18); if (state.failed) return current;
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

            // InternalGaml.g:1153:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==RULE_ID||LA16_0==15||(LA16_0>=82 && LA16_0<=110)) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // InternalGaml.g:1154:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1154:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:1155:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_LoopAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
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
            	    break loop16;
                }
            } while (true);

            // InternalGaml.g:1172:3: ( (lv_block_3_0= ruleBlock ) )
            // InternalGaml.g:1173:4: (lv_block_3_0= ruleBlock )
            {
            // InternalGaml.g:1173:4: (lv_block_3_0= ruleBlock )
            // InternalGaml.g:1174:5: lv_block_3_0= ruleBlock
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
    // InternalGaml.g:1195:1: entryRuleS_If returns [EObject current=null] : iv_ruleS_If= ruleS_If EOF ;
    public final EObject entryRuleS_If() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_If = null;


        try {
            // InternalGaml.g:1195:45: (iv_ruleS_If= ruleS_If EOF )
            // InternalGaml.g:1196:2: iv_ruleS_If= ruleS_If EOF
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
    // InternalGaml.g:1202:1: ruleS_If returns [EObject current=null] : ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) ;
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
            // InternalGaml.g:1208:2: ( ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) )
            // InternalGaml.g:1209:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            {
            // InternalGaml.g:1209:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            // InternalGaml.g:1210:3: ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            {
            // InternalGaml.g:1210:3: ( (lv_key_0_0= 'if' ) )
            // InternalGaml.g:1211:4: (lv_key_0_0= 'if' )
            {
            // InternalGaml.g:1211:4: (lv_key_0_0= 'if' )
            // InternalGaml.g:1212:5: lv_key_0_0= 'if'
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

            // InternalGaml.g:1224:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1225:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1225:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1226:5: lv_expr_1_0= ruleExpression
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

            // InternalGaml.g:1243:3: ( (lv_block_2_0= ruleBlock ) )
            // InternalGaml.g:1244:4: (lv_block_2_0= ruleBlock )
            {
            // InternalGaml.g:1244:4: (lv_block_2_0= ruleBlock )
            // InternalGaml.g:1245:5: lv_block_2_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_IfAccess().getBlockBlockParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_19);
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

            // InternalGaml.g:1262:3: ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==29) && (synpred8_InternalGaml())) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // InternalGaml.g:1263:4: ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    {
                    // InternalGaml.g:1263:4: ( ( 'else' )=>otherlv_3= 'else' )
                    // InternalGaml.g:1264:5: ( 'else' )=>otherlv_3= 'else'
                    {
                    otherlv_3=(Token)match(input,29,FOLLOW_20); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_IfAccess().getElseKeyword_3_0());
                      				
                    }

                    }

                    // InternalGaml.g:1270:4: ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    // InternalGaml.g:1271:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    {
                    // InternalGaml.g:1271:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    // InternalGaml.g:1272:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    {
                    // InternalGaml.g:1272:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==28) ) {
                        alt17=1;
                    }
                    else if ( (LA17_0==37) ) {
                        alt17=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 0, input);

                        throw nvae;
                    }
                    switch (alt17) {
                        case 1 :
                            // InternalGaml.g:1273:7: lv_else_4_1= ruleS_If
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
                            // InternalGaml.g:1289:7: lv_else_4_2= ruleBlock
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
    // InternalGaml.g:1312:1: entryRuleS_Try returns [EObject current=null] : iv_ruleS_Try= ruleS_Try EOF ;
    public final EObject entryRuleS_Try() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Try = null;


        try {
            // InternalGaml.g:1312:46: (iv_ruleS_Try= ruleS_Try EOF )
            // InternalGaml.g:1313:2: iv_ruleS_Try= ruleS_Try EOF
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
    // InternalGaml.g:1319:1: ruleS_Try returns [EObject current=null] : ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) ;
    public final EObject ruleS_Try() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_block_1_0 = null;

        EObject lv_catch_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1325:2: ( ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) )
            // InternalGaml.g:1326:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            {
            // InternalGaml.g:1326:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            // InternalGaml.g:1327:3: ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            {
            // InternalGaml.g:1327:3: ( (lv_key_0_0= 'try' ) )
            // InternalGaml.g:1328:4: (lv_key_0_0= 'try' )
            {
            // InternalGaml.g:1328:4: (lv_key_0_0= 'try' )
            // InternalGaml.g:1329:5: lv_key_0_0= 'try'
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

            // InternalGaml.g:1341:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:1342:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:1342:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:1343:5: lv_block_1_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_TryAccess().getBlockBlockParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_21);
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

            // InternalGaml.g:1360:3: ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==31) && (synpred9_InternalGaml())) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // InternalGaml.g:1361:4: ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) )
                    {
                    // InternalGaml.g:1361:4: ( ( 'catch' )=>otherlv_2= 'catch' )
                    // InternalGaml.g:1362:5: ( 'catch' )=>otherlv_2= 'catch'
                    {
                    otherlv_2=(Token)match(input,31,FOLLOW_3); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getS_TryAccess().getCatchKeyword_2_0());
                      				
                    }

                    }

                    // InternalGaml.g:1368:4: ( (lv_catch_3_0= ruleBlock ) )
                    // InternalGaml.g:1369:5: (lv_catch_3_0= ruleBlock )
                    {
                    // InternalGaml.g:1369:5: (lv_catch_3_0= ruleBlock )
                    // InternalGaml.g:1370:6: lv_catch_3_0= ruleBlock
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
    // InternalGaml.g:1392:1: entryRuleS_Return returns [EObject current=null] : iv_ruleS_Return= ruleS_Return EOF ;
    public final EObject entryRuleS_Return() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Return = null;


        try {
            // InternalGaml.g:1392:49: (iv_ruleS_Return= ruleS_Return EOF )
            // InternalGaml.g:1393:2: iv_ruleS_Return= ruleS_Return EOF
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
    // InternalGaml.g:1399:1: ruleS_Return returns [EObject current=null] : ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) ;
    public final EObject ruleS_Return() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1405:2: ( ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) )
            // InternalGaml.g:1406:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            {
            // InternalGaml.g:1406:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            // InternalGaml.g:1407:3: ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';'
            {
            // InternalGaml.g:1407:3: ( (lv_key_0_0= 'return' ) )
            // InternalGaml.g:1408:4: (lv_key_0_0= 'return' )
            {
            // InternalGaml.g:1408:4: (lv_key_0_0= 'return' )
            // InternalGaml.g:1409:5: lv_key_0_0= 'return'
            {
            lv_key_0_0=(Token)match(input,32,FOLLOW_22); if (state.failed) return current;
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

            // InternalGaml.g:1421:3: ( (lv_expr_1_0= ruleExpression ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=RULE_ID && LA20_0<=RULE_KEYWORD)||LA20_0==21||LA20_0==33||LA20_0==37||(LA20_0>=42 && LA20_0<=72)||LA20_0==120||(LA20_0>=124 && LA20_0<=126)) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalGaml.g:1422:4: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:1422:4: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:1423:5: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReturnAccess().getExprExpressionParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_23);
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
    // InternalGaml.g:1448:1: entryRuleS_Other returns [EObject current=null] : iv_ruleS_Other= ruleS_Other EOF ;
    public final EObject entryRuleS_Other() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Other = null;


        try {
            // InternalGaml.g:1448:48: (iv_ruleS_Other= ruleS_Other EOF )
            // InternalGaml.g:1449:2: iv_ruleS_Other= ruleS_Other EOF
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
    // InternalGaml.g:1455:1: ruleS_Other returns [EObject current=null] : ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) ;
    public final EObject ruleS_Other() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:1461:2: ( ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) )
            // InternalGaml.g:1462:2: ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            {
            // InternalGaml.g:1462:2: ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1463:3: ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1463:3: ( (lv_key_0_0= ruleValid_ID ) )
            // InternalGaml.g:1464:4: (lv_key_0_0= ruleValid_ID )
            {
            // InternalGaml.g:1464:4: (lv_key_0_0= ruleValid_ID )
            // InternalGaml.g:1465:5: lv_key_0_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherAccess().getKeyValid_IDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_24);
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

            // InternalGaml.g:1482:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            int alt21=2;
            alt21 = dfa21.predict(input);
            switch (alt21) {
                case 1 :
                    // InternalGaml.g:1483:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    {
                    // InternalGaml.g:1483:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    // InternalGaml.g:1484:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    {
                    // InternalGaml.g:1493:5: ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    // InternalGaml.g:1494:6: ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
                    {
                    // InternalGaml.g:1494:6: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:1495:7: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:1495:7: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:1496:8: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getS_OtherAccess().getExprExpressionParserRuleCall_1_0_0_0_0());
                      							
                    }
                    pushFollow(FOLLOW_7);
                    lv_expr_1_0=ruleExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      								if (current==null) {
                      									current = createModelElementForParent(grammarAccess.getS_OtherRule());
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
                      							current = createModelElement(grammarAccess.getS_OtherRule());
                      						}
                      						newCompositeNode(grammarAccess.getS_OtherAccess().getFacetsAndBlockParserRuleCall_1_0_0_1());
                      					
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
                    // InternalGaml.g:1527:4: this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
                    {
                    if ( state.backtracking==0 ) {

                      				if (current==null) {
                      					current = createModelElement(grammarAccess.getS_OtherRule());
                      				}
                      				newCompositeNode(grammarAccess.getS_OtherAccess().getFacetsAndBlockParserRuleCall_1_1());
                      			
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
    // $ANTLR end "ruleS_Other"


    // $ANTLR start "entryRuleS_Reflex"
    // InternalGaml.g:1543:1: entryRuleS_Reflex returns [EObject current=null] : iv_ruleS_Reflex= ruleS_Reflex EOF ;
    public final EObject entryRuleS_Reflex() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Reflex = null;


        try {
            // InternalGaml.g:1543:49: (iv_ruleS_Reflex= ruleS_Reflex EOF )
            // InternalGaml.g:1544:2: iv_ruleS_Reflex= ruleS_Reflex EOF
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
    // InternalGaml.g:1550:1: ruleS_Reflex returns [EObject current=null] : ( ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Reflex() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_1 = null;

        AntlrDatatypeRuleToken lv_key_0_2 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1556:2: ( ( ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1557:2: ( ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1557:2: ( ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1558:3: ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1558:3: ( ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) ) )
            // InternalGaml.g:1559:4: ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) )
            {
            // InternalGaml.g:1559:4: ( (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init ) )
            // InternalGaml.g:1560:5: (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init )
            {
            // InternalGaml.g:1560:5: (lv_key_0_1= ruleK_Reflex | lv_key_0_2= ruleK_Init )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==73) ) {
                alt22=1;
            }
            else if ( (LA22_0==72) ) {
                alt22=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // InternalGaml.g:1561:6: lv_key_0_1= ruleK_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getKeyK_ReflexParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_25);
                    lv_key_0_1=ruleK_Reflex();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_1,
                      							"gaml.compiler.Gaml.K_Reflex");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:1577:6: lv_key_0_2= ruleK_Init
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getKeyK_InitParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_25);
                    lv_key_0_2=ruleK_Init();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_2,
                      							"gaml.compiler.Gaml.K_Init");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:1595:3: ( (lv_name_1_0= ruleValid_ID ) )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( ((LA23_0>=42 && LA23_0<=72)) ) {
                alt23=1;
            }
            else if ( (LA23_0==RULE_ID) ) {
                int LA23_2 = input.LA(2);

                if ( (LA23_2==RULE_ID||LA23_2==15||LA23_2==23||LA23_2==37||(LA23_2>=82 && LA23_2<=110)) ) {
                    alt23=1;
                }
            }
            switch (alt23) {
                case 1 :
                    // InternalGaml.g:1596:4: (lv_name_1_0= ruleValid_ID )
                    {
                    // InternalGaml.g:1596:4: (lv_name_1_0= ruleValid_ID )
                    // InternalGaml.g:1597:5: lv_name_1_0= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReflexAccess().getNameValid_IDParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_7);
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

            if ( state.backtracking==0 ) {

              			if (current==null) {
              				current = createModelElement(grammarAccess.getS_ReflexRule());
              			}
              			newCompositeNode(grammarAccess.getS_ReflexAccess().getFacetsAndBlockParserRuleCall_2());
              		
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
    // $ANTLR end "ruleS_Reflex"


    // $ANTLR start "entryRuleS_Definition"
    // InternalGaml.g:1629:1: entryRuleS_Definition returns [EObject current=null] : iv_ruleS_Definition= ruleS_Definition EOF ;
    public final EObject entryRuleS_Definition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Definition = null;


        try {
            // InternalGaml.g:1629:53: (iv_ruleS_Definition= ruleS_Definition EOF )
            // InternalGaml.g:1630:2: iv_ruleS_Definition= ruleS_Definition EOF
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
    // InternalGaml.g:1636:1: ruleS_Definition returns [EObject current=null] : ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Definition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_tkey_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_args_3_0 = null;

        EObject this_FacetsAndBlock_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:1642:2: ( ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1643:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1643:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1644:3: ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1644:3: ( (lv_tkey_0_0= ruleTypeRef ) )
            // InternalGaml.g:1645:4: (lv_tkey_0_0= ruleTypeRef )
            {
            // InternalGaml.g:1645:4: (lv_tkey_0_0= ruleTypeRef )
            // InternalGaml.g:1646:5: lv_tkey_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DefinitionAccess().getTkeyTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_10);
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

            // InternalGaml.g:1663:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:1664:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:1664:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:1665:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_26);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DefinitionRule());
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

            // InternalGaml.g:1682:3: (otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')' )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==33) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalGaml.g:1683:4: otherlv_2= '(' ( (lv_args_3_0= ruleActionArguments ) ) otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,33,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getS_DefinitionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:1687:4: ( (lv_args_3_0= ruleActionArguments ) )
                    // InternalGaml.g:1688:5: (lv_args_3_0= ruleActionArguments )
                    {
                    // InternalGaml.g:1688:5: (lv_args_3_0= ruleActionArguments )
                    // InternalGaml.g:1689:6: lv_args_3_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DefinitionAccess().getArgsActionArgumentsParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_28);
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

                    otherlv_4=(Token)match(input,34,FOLLOW_7); if (state.failed) return current;
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
    // InternalGaml.g:1726:1: entryRuleS_Action returns [EObject current=null] : iv_ruleS_Action= ruleS_Action EOF ;
    public final EObject entryRuleS_Action() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Action = null;


        try {
            // InternalGaml.g:1726:49: (iv_ruleS_Action= ruleS_Action EOF )
            // InternalGaml.g:1727:2: iv_ruleS_Action= ruleS_Action EOF
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
    // InternalGaml.g:1733:1: ruleS_Action returns [EObject current=null] : ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) ;
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
            // InternalGaml.g:1739:2: ( ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1740:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1740:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1741:3: () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1741:3: ()
            // InternalGaml.g:1742:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getS_ActionAccess().getS_ActionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:1748:3: ( (lv_key_1_0= 'action' ) )
            // InternalGaml.g:1749:4: (lv_key_1_0= 'action' )
            {
            // InternalGaml.g:1749:4: (lv_key_1_0= 'action' )
            // InternalGaml.g:1750:5: lv_key_1_0= 'action'
            {
            lv_key_1_0=(Token)match(input,35,FOLLOW_10); if (state.failed) return current;
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

            // InternalGaml.g:1762:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:1763:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:1763:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:1764:5: lv_name_2_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ActionAccess().getNameValid_IDParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_26);
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

            // InternalGaml.g:1781:3: (otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')' )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==33) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // InternalGaml.g:1782:4: otherlv_3= '(' ( (lv_args_4_0= ruleActionArguments ) ) otherlv_5= ')'
                    {
                    otherlv_3=(Token)match(input,33,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_ActionAccess().getLeftParenthesisKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:1786:4: ( (lv_args_4_0= ruleActionArguments ) )
                    // InternalGaml.g:1787:5: (lv_args_4_0= ruleActionArguments )
                    {
                    // InternalGaml.g:1787:5: (lv_args_4_0= ruleActionArguments )
                    // InternalGaml.g:1788:6: lv_args_4_0= ruleActionArguments
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ActionAccess().getArgsActionArgumentsParserRuleCall_3_1_0());
                      					
                    }
                    pushFollow(FOLLOW_28);
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

                    otherlv_5=(Token)match(input,34,FOLLOW_7); if (state.failed) return current;
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
    // InternalGaml.g:1825:1: entryRuleS_Assignment returns [EObject current=null] : iv_ruleS_Assignment= ruleS_Assignment EOF ;
    public final EObject entryRuleS_Assignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Assignment = null;


        try {
            // InternalGaml.g:1825:53: (iv_ruleS_Assignment= ruleS_Assignment EOF )
            // InternalGaml.g:1826:2: iv_ruleS_Assignment= ruleS_Assignment EOF
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
    // InternalGaml.g:1832:1: ruleS_Assignment returns [EObject current=null] : ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) ;
    public final EObject ruleS_Assignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_4=null;
        EObject lv_expr_0_0 = null;

        AntlrDatatypeRuleToken lv_key_1_0 = null;

        EObject lv_value_2_0 = null;

        EObject lv_facets_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1838:2: ( ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' ) )
            // InternalGaml.g:1839:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            {
            // InternalGaml.g:1839:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';' )
            // InternalGaml.g:1840:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* ) otherlv_4= ';'
            {
            // InternalGaml.g:1840:3: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* )
            // InternalGaml.g:1841:4: ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )*
            {
            // InternalGaml.g:1841:4: ( (lv_expr_0_0= ruleExpression ) )
            // InternalGaml.g:1842:5: (lv_expr_0_0= ruleExpression )
            {
            // InternalGaml.g:1842:5: (lv_expr_0_0= ruleExpression )
            // InternalGaml.g:1843:6: lv_expr_0_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getExprExpressionParserRuleCall_0_0_0());
              					
            }
            pushFollow(FOLLOW_29);
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

            // InternalGaml.g:1860:4: ( (lv_key_1_0= ruleK_Assignment ) )
            // InternalGaml.g:1861:5: (lv_key_1_0= ruleK_Assignment )
            {
            // InternalGaml.g:1861:5: (lv_key_1_0= ruleK_Assignment )
            // InternalGaml.g:1862:6: lv_key_1_0= ruleK_Assignment
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

            // InternalGaml.g:1879:4: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:1880:5: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:1880:5: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:1881:6: lv_value_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              						newCompositeNode(grammarAccess.getS_AssignmentAccess().getValueExpressionParserRuleCall_0_2_0());
              					
            }
            pushFollow(FOLLOW_30);
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

            // InternalGaml.g:1898:4: ( (lv_facets_3_0= ruleFacet ) )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==RULE_ID||LA26_0==15||(LA26_0>=82 && LA26_0<=110)) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // InternalGaml.g:1899:5: (lv_facets_3_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1899:5: (lv_facets_3_0= ruleFacet )
            	    // InternalGaml.g:1900:6: lv_facets_3_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getS_AssignmentAccess().getFacetsFacetParserRuleCall_0_3_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_30);
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
            	    break loop26;
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
    // InternalGaml.g:1926:1: entryRuleS_Equations returns [EObject current=null] : iv_ruleS_Equations= ruleS_Equations EOF ;
    public final EObject entryRuleS_Equations() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equations = null;


        try {
            // InternalGaml.g:1926:52: (iv_ruleS_Equations= ruleS_Equations EOF )
            // InternalGaml.g:1927:2: iv_ruleS_Equations= ruleS_Equations EOF
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
    // InternalGaml.g:1933:1: ruleS_Equations returns [EObject current=null] : ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) ;
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
            // InternalGaml.g:1939:2: ( ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) )
            // InternalGaml.g:1940:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            {
            // InternalGaml.g:1940:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            // InternalGaml.g:1941:3: ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            {
            // InternalGaml.g:1941:3: ( (lv_key_0_0= 'equation' ) )
            // InternalGaml.g:1942:4: (lv_key_0_0= 'equation' )
            {
            // InternalGaml.g:1942:4: (lv_key_0_0= 'equation' )
            // InternalGaml.g:1943:5: lv_key_0_0= 'equation'
            {
            lv_key_0_0=(Token)match(input,36,FOLLOW_10); if (state.failed) return current;
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

            // InternalGaml.g:1955:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:1956:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:1956:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:1957:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationsAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_17);
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

            // InternalGaml.g:1974:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==RULE_ID||LA27_0==15||(LA27_0>=82 && LA27_0<=110)) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // InternalGaml.g:1975:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1975:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:1976:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_EquationsAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
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
            	    break loop27;
                }
            } while (true);

            // InternalGaml.g:1993:3: ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==37) ) {
                alt29=1;
            }
            else if ( (LA29_0==23) ) {
                alt29=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // InternalGaml.g:1994:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    {
                    // InternalGaml.g:1994:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    // InternalGaml.g:1995:5: otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}'
                    {
                    otherlv_3=(Token)match(input,37,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0());
                      				
                    }
                    // InternalGaml.g:1999:5: ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==RULE_ID||(LA28_0>=42 && LA28_0<=72)) ) {
                            alt28=1;
                        }


                        switch (alt28) {
                    	case 1 :
                    	    // InternalGaml.g:2000:6: ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';'
                    	    {
                    	    // InternalGaml.g:2000:6: ( (lv_equations_4_0= ruleS_Equation ) )
                    	    // InternalGaml.g:2001:7: (lv_equations_4_0= ruleS_Equation )
                    	    {
                    	    // InternalGaml.g:2001:7: (lv_equations_4_0= ruleS_Equation )
                    	    // InternalGaml.g:2002:8: lv_equations_4_0= ruleS_Equation
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      								newCompositeNode(grammarAccess.getS_EquationsAccess().getEquationsS_EquationParserRuleCall_3_0_1_0_0());
                    	      							
                    	    }
                    	    pushFollow(FOLLOW_23);
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

                    	    otherlv_5=(Token)match(input,23,FOLLOW_31); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						newLeafNode(otherlv_5, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_0_1_1());
                    	      					
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);

                    otherlv_6=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_6, grammarAccess.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2());
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:2030:4: otherlv_7= ';'
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
    // InternalGaml.g:2039:1: entryRuleS_Equation returns [EObject current=null] : iv_ruleS_Equation= ruleS_Equation EOF ;
    public final EObject entryRuleS_Equation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equation = null;


        try {
            // InternalGaml.g:2039:51: (iv_ruleS_Equation= ruleS_Equation EOF )
            // InternalGaml.g:2040:2: iv_ruleS_Equation= ruleS_Equation EOF
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
    // InternalGaml.g:2046:1: ruleS_Equation returns [EObject current=null] : ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) ;
    public final EObject ruleS_Equation() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        EObject lv_expr_0_1 = null;

        EObject lv_expr_0_2 = null;

        EObject lv_value_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2052:2: ( ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:2053:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:2053:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            // InternalGaml.g:2054:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) )
            {
            // InternalGaml.g:2054:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) )
            // InternalGaml.g:2055:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            {
            // InternalGaml.g:2055:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            // InternalGaml.g:2056:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            {
            // InternalGaml.g:2056:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            int alt30=2;
            alt30 = dfa30.predict(input);
            switch (alt30) {
                case 1 :
                    // InternalGaml.g:2057:6: lv_expr_0_1= ruleFunction
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprFunctionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_32);
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
                    // InternalGaml.g:2073:6: lv_expr_0_2= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprVariableRefParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_32);
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

            // InternalGaml.g:2091:3: ( (lv_key_1_0= '=' ) )
            // InternalGaml.g:2092:4: (lv_key_1_0= '=' )
            {
            // InternalGaml.g:2092:4: (lv_key_1_0= '=' )
            // InternalGaml.g:2093:5: lv_key_1_0= '='
            {
            lv_key_1_0=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:2105:3: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2106:4: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2106:4: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2107:5: lv_value_2_0= ruleExpression
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
    // InternalGaml.g:2128:1: entryRuleS_Solve returns [EObject current=null] : iv_ruleS_Solve= ruleS_Solve EOF ;
    public final EObject entryRuleS_Solve() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Solve = null;


        try {
            // InternalGaml.g:2128:48: (iv_ruleS_Solve= ruleS_Solve EOF )
            // InternalGaml.g:2129:2: iv_ruleS_Solve= ruleS_Solve EOF
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
    // InternalGaml.g:2135:1: ruleS_Solve returns [EObject current=null] : ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Solve() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2141:2: ( ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2142:2: ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2142:2: ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2143:3: ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2143:3: ( (lv_key_0_0= 'solve' ) )
            // InternalGaml.g:2144:4: (lv_key_0_0= 'solve' )
            {
            // InternalGaml.g:2144:4: (lv_key_0_0= 'solve' )
            // InternalGaml.g:2145:5: lv_key_0_0= 'solve'
            {
            lv_key_0_0=(Token)match(input,40,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_SolveAccess().getKeySolveKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_SolveRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "solve");
              				
            }

            }


            }

            // InternalGaml.g:2157:3: ( (lv_expr_1_0= ruleEquationRef ) )
            // InternalGaml.g:2158:4: (lv_expr_1_0= ruleEquationRef )
            {
            // InternalGaml.g:2158:4: (lv_expr_1_0= ruleEquationRef )
            // InternalGaml.g:2159:5: lv_expr_1_0= ruleEquationRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getExprEquationRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_7);
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
    // InternalGaml.g:2191:1: entryRuleS_Display returns [EObject current=null] : iv_ruleS_Display= ruleS_Display EOF ;
    public final EObject entryRuleS_Display() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Display = null;


        try {
            // InternalGaml.g:2191:50: (iv_ruleS_Display= ruleS_Display EOF )
            // InternalGaml.g:2192:2: iv_ruleS_Display= ruleS_Display EOF
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
    // InternalGaml.g:2198:1: ruleS_Display returns [EObject current=null] : ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) ;
    public final EObject ruleS_Display() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2204:2: ( ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) )
            // InternalGaml.g:2205:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            {
            // InternalGaml.g:2205:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            // InternalGaml.g:2206:3: ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) )
            {
            // InternalGaml.g:2206:3: ( (lv_key_0_0= 'display' ) )
            // InternalGaml.g:2207:4: (lv_key_0_0= 'display' )
            {
            // InternalGaml.g:2207:4: (lv_key_0_0= 'display' )
            // InternalGaml.g:2208:5: lv_key_0_0= 'display'
            {
            lv_key_0_0=(Token)match(input,41,FOLLOW_6); if (state.failed) return current;
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

            // InternalGaml.g:2220:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:2221:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:2221:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:2222:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:2222:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==RULE_ID||(LA31_0>=42 && LA31_0<=72)) ) {
                alt31=1;
            }
            else if ( (LA31_0==RULE_STRING) ) {
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
                    // InternalGaml.g:2223:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_DisplayAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_18);
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
                    // InternalGaml.g:2239:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_18); if (state.failed) return current;
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

            // InternalGaml.g:2256:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==RULE_ID||LA32_0==15||(LA32_0>=82 && LA32_0<=110)) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // InternalGaml.g:2257:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2257:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2258:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_DisplayAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
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
            	    break loop32;
                }
            } while (true);

            // InternalGaml.g:2275:3: ( (lv_block_3_0= ruleDisplayBlock ) )
            // InternalGaml.g:2276:4: (lv_block_3_0= ruleDisplayBlock )
            {
            // InternalGaml.g:2276:4: (lv_block_3_0= ruleDisplayBlock )
            // InternalGaml.g:2277:5: lv_block_3_0= ruleDisplayBlock
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


    // $ANTLR start "entryRule_SpeciesKey"
    // InternalGaml.g:2298:1: entryRule_SpeciesKey returns [String current=null] : iv_rule_SpeciesKey= rule_SpeciesKey EOF ;
    public final String entryRule_SpeciesKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_SpeciesKey = null;


        try {
            // InternalGaml.g:2298:51: (iv_rule_SpeciesKey= rule_SpeciesKey EOF )
            // InternalGaml.g:2299:2: iv_rule_SpeciesKey= rule_SpeciesKey EOF
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
    // InternalGaml.g:2305:1: rule_SpeciesKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'species' | kw= 'grid' ) ;
    public final AntlrDatatypeRuleToken rule_SpeciesKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2311:2: ( (kw= 'species' | kw= 'grid' ) )
            // InternalGaml.g:2312:2: (kw= 'species' | kw= 'grid' )
            {
            // InternalGaml.g:2312:2: (kw= 'species' | kw= 'grid' )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==42) ) {
                alt33=1;
            }
            else if ( (LA33_0==43) ) {
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
                    // InternalGaml.g:2313:3: kw= 'species'
                    {
                    kw=(Token)match(input,42,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_SpeciesKeyAccess().getSpeciesKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2319:3: kw= 'grid'
                    {
                    kw=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2328:1: entryRule_ExperimentKey returns [String current=null] : iv_rule_ExperimentKey= rule_ExperimentKey EOF ;
    public final String entryRule_ExperimentKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_ExperimentKey = null;


        try {
            // InternalGaml.g:2328:54: (iv_rule_ExperimentKey= rule_ExperimentKey EOF )
            // InternalGaml.g:2329:2: iv_rule_ExperimentKey= rule_ExperimentKey EOF
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
    // InternalGaml.g:2335:1: rule_ExperimentKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'experiment' ;
    public final AntlrDatatypeRuleToken rule_ExperimentKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2341:2: (kw= 'experiment' )
            // InternalGaml.g:2342:2: kw= 'experiment'
            {
            kw=(Token)match(input,44,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2350:1: entryRule_GeneralKey returns [String current=null] : iv_rule_GeneralKey= rule_GeneralKey EOF ;
    public final String entryRule_GeneralKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_rule_GeneralKey = null;


        try {
            // InternalGaml.g:2350:51: (iv_rule_GeneralKey= rule_GeneralKey EOF )
            // InternalGaml.g:2351:2: iv_rule_GeneralKey= rule_GeneralKey EOF
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
    // InternalGaml.g:2357:1: rule_GeneralKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) ;
    public final AntlrDatatypeRuleToken rule_GeneralKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2363:2: ( (kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' ) )
            // InternalGaml.g:2364:2: (kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            {
            // InternalGaml.g:2364:2: (kw= 'ask' | kw= 'release' | kw= 'capture' | kw= 'create' | kw= 'write' | kw= 'error' | kw= 'warn' | kw= 'exception' | kw= 'save' | kw= 'assert' | kw= 'inspect' | kw= 'browse' | kw= 'restore' | kw= 'draw' | kw= 'using' | kw= 'switch' | kw= 'put' | kw= 'add' | kw= 'remove' | kw= 'match' | kw= 'match_between' | kw= 'match_one' | kw= 'parameter' | kw= 'status' | kw= 'highlight' | kw= 'focus_on' | kw= 'layout' )
            int alt34=27;
            switch ( input.LA(1) ) {
            case 45:
                {
                alt34=1;
                }
                break;
            case 46:
                {
                alt34=2;
                }
                break;
            case 47:
                {
                alt34=3;
                }
                break;
            case 48:
                {
                alt34=4;
                }
                break;
            case 49:
                {
                alt34=5;
                }
                break;
            case 50:
                {
                alt34=6;
                }
                break;
            case 51:
                {
                alt34=7;
                }
                break;
            case 52:
                {
                alt34=8;
                }
                break;
            case 53:
                {
                alt34=9;
                }
                break;
            case 54:
                {
                alt34=10;
                }
                break;
            case 55:
                {
                alt34=11;
                }
                break;
            case 56:
                {
                alt34=12;
                }
                break;
            case 57:
                {
                alt34=13;
                }
                break;
            case 58:
                {
                alt34=14;
                }
                break;
            case 59:
                {
                alt34=15;
                }
                break;
            case 60:
                {
                alt34=16;
                }
                break;
            case 61:
                {
                alt34=17;
                }
                break;
            case 62:
                {
                alt34=18;
                }
                break;
            case 63:
                {
                alt34=19;
                }
                break;
            case 64:
                {
                alt34=20;
                }
                break;
            case 65:
                {
                alt34=21;
                }
                break;
            case 66:
                {
                alt34=22;
                }
                break;
            case 67:
                {
                alt34=23;
                }
                break;
            case 68:
                {
                alt34=24;
                }
                break;
            case 69:
                {
                alt34=25;
                }
                break;
            case 70:
                {
                alt34=26;
                }
                break;
            case 71:
                {
                alt34=27;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }

            switch (alt34) {
                case 1 :
                    // InternalGaml.g:2365:3: kw= 'ask'
                    {
                    kw=(Token)match(input,45,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2371:3: kw= 'release'
                    {
                    kw=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getReleaseKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2377:3: kw= 'capture'
                    {
                    kw=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCaptureKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:2383:3: kw= 'create'
                    {
                    kw=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getCreateKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2389:3: kw= 'write'
                    {
                    kw=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWriteKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:2395:3: kw= 'error'
                    {
                    kw=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getErrorKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:2401:3: kw= 'warn'
                    {
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getWarnKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:2407:3: kw= 'exception'
                    {
                    kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getExceptionKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:2413:3: kw= 'save'
                    {
                    kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSaveKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:2419:3: kw= 'assert'
                    {
                    kw=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAssertKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:2425:3: kw= 'inspect'
                    {
                    kw=(Token)match(input,55,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getInspectKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:2431:3: kw= 'browse'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getBrowseKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:2437:3: kw= 'restore'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRestoreKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:2443:3: kw= 'draw'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getDrawKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:2449:3: kw= 'using'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getUsingKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:2455:3: kw= 'switch'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getSwitchKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:2461:3: kw= 'put'
                    {
                    kw=(Token)match(input,61,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getPutKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:2467:3: kw= 'add'
                    {
                    kw=(Token)match(input,62,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getAddKeyword_17());
                      		
                    }

                    }
                    break;
                case 19 :
                    // InternalGaml.g:2473:3: kw= 'remove'
                    {
                    kw=(Token)match(input,63,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getRemoveKeyword_18());
                      		
                    }

                    }
                    break;
                case 20 :
                    // InternalGaml.g:2479:3: kw= 'match'
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatchKeyword_19());
                      		
                    }

                    }
                    break;
                case 21 :
                    // InternalGaml.g:2485:3: kw= 'match_between'
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_betweenKeyword_20());
                      		
                    }

                    }
                    break;
                case 22 :
                    // InternalGaml.g:2491:3: kw= 'match_one'
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getMatch_oneKeyword_21());
                      		
                    }

                    }
                    break;
                case 23 :
                    // InternalGaml.g:2497:3: kw= 'parameter'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getParameterKeyword_22());
                      		
                    }

                    }
                    break;
                case 24 :
                    // InternalGaml.g:2503:3: kw= 'status'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getStatusKeyword_23());
                      		
                    }

                    }
                    break;
                case 25 :
                    // InternalGaml.g:2509:3: kw= 'highlight'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getHighlightKeyword_24());
                      		
                    }

                    }
                    break;
                case 26 :
                    // InternalGaml.g:2515:3: kw= 'focus_on'
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getFocus_onKeyword_25());
                      		
                    }

                    }
                    break;
                case 27 :
                    // InternalGaml.g:2521:3: kw= 'layout'
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.get_GeneralKeyAccess().getLayoutKeyword_26());
                      		
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


    // $ANTLR start "entryRuleK_Init"
    // InternalGaml.g:2530:1: entryRuleK_Init returns [String current=null] : iv_ruleK_Init= ruleK_Init EOF ;
    public final String entryRuleK_Init() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Init = null;


        try {
            // InternalGaml.g:2530:46: (iv_ruleK_Init= ruleK_Init EOF )
            // InternalGaml.g:2531:2: iv_ruleK_Init= ruleK_Init EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_InitRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Init=ruleK_Init();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Init.getText(); 
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
    // $ANTLR end "entryRuleK_Init"


    // $ANTLR start "ruleK_Init"
    // InternalGaml.g:2537:1: ruleK_Init returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'init' ;
    public final AntlrDatatypeRuleToken ruleK_Init() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2543:2: (kw= 'init' )
            // InternalGaml.g:2544:2: kw= 'init'
            {
            kw=(Token)match(input,72,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_InitAccess().getInitKeyword());
              	
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
    // $ANTLR end "ruleK_Init"


    // $ANTLR start "entryRuleK_Reflex"
    // InternalGaml.g:2552:1: entryRuleK_Reflex returns [String current=null] : iv_ruleK_Reflex= ruleK_Reflex EOF ;
    public final String entryRuleK_Reflex() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Reflex = null;


        try {
            // InternalGaml.g:2552:48: (iv_ruleK_Reflex= ruleK_Reflex EOF )
            // InternalGaml.g:2553:2: iv_ruleK_Reflex= ruleK_Reflex EOF
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
    // InternalGaml.g:2559:1: ruleK_Reflex returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'reflex' ;
    public final AntlrDatatypeRuleToken ruleK_Reflex() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2565:2: (kw= 'reflex' )
            // InternalGaml.g:2566:2: kw= 'reflex'
            {
            kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_ReflexAccess().getReflexKeyword());
              	
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
    // InternalGaml.g:2574:1: entryRuleK_Assignment returns [String current=null] : iv_ruleK_Assignment= ruleK_Assignment EOF ;
    public final String entryRuleK_Assignment() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Assignment = null;


        try {
            // InternalGaml.g:2574:52: (iv_ruleK_Assignment= ruleK_Assignment EOF )
            // InternalGaml.g:2575:2: iv_ruleK_Assignment= ruleK_Assignment EOF
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
    // InternalGaml.g:2581:1: ruleK_Assignment returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) ;
    public final AntlrDatatypeRuleToken ruleK_Assignment() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2587:2: ( (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) )
            // InternalGaml.g:2588:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            {
            // InternalGaml.g:2588:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            int alt35=8;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // InternalGaml.g:2589:3: kw= '<-'
                    {
                    kw=(Token)match(input,15,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignHyphenMinusKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2595:3: kw= '<<'
                    {
                    kw=(Token)match(input,74,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2601:3: (kw= '>' kw= '>' )
                    {
                    // InternalGaml.g:2601:3: (kw= '>' kw= '>' )
                    // InternalGaml.g:2602:4: kw= '>' kw= '>'
                    {
                    kw=(Token)match(input,75,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:2614:3: kw= '<<+'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignPlusSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2620:3: (kw= '>' kw= '>-' )
                    {
                    // InternalGaml.g:2620:3: (kw= '>' kw= '>-' )
                    // InternalGaml.g:2621:4: kw= '>' kw= '>-'
                    {
                    kw=(Token)match(input,75,FOLLOW_34); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_4_0());
                      			
                    }
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignHyphenMinusKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalGaml.g:2633:3: kw= '+<-'
                    {
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getPlusSignLessThanSignHyphenMinusKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:2639:3: kw= '<+'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignPlusSignKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:2645:3: kw= '>-'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2654:1: entryRuleActionArguments returns [EObject current=null] : iv_ruleActionArguments= ruleActionArguments EOF ;
    public final EObject entryRuleActionArguments() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionArguments = null;


        try {
            // InternalGaml.g:2654:56: (iv_ruleActionArguments= ruleActionArguments EOF )
            // InternalGaml.g:2655:2: iv_ruleActionArguments= ruleActionArguments EOF
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
    // InternalGaml.g:2661:1: ruleActionArguments returns [EObject current=null] : ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) ;
    public final EObject ruleActionArguments() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_args_0_0 = null;

        EObject lv_args_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2667:2: ( ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) )
            // InternalGaml.g:2668:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            {
            // InternalGaml.g:2668:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            // InternalGaml.g:2669:3: ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            {
            // InternalGaml.g:2669:3: ( (lv_args_0_0= ruleArgumentDefinition ) )
            // InternalGaml.g:2670:4: (lv_args_0_0= ruleArgumentDefinition )
            {
            // InternalGaml.g:2670:4: (lv_args_0_0= ruleArgumentDefinition )
            // InternalGaml.g:2671:5: lv_args_0_0= ruleArgumentDefinition
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_35);
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

            // InternalGaml.g:2688:3: (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==80) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // InternalGaml.g:2689:4: otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    {
            	    otherlv_1=(Token)match(input,80,FOLLOW_27); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_1, grammarAccess.getActionArgumentsAccess().getCommaKeyword_1_0());
            	      			
            	    }
            	    // InternalGaml.g:2693:4: ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    // InternalGaml.g:2694:5: (lv_args_2_0= ruleArgumentDefinition )
            	    {
            	    // InternalGaml.g:2694:5: (lv_args_2_0= ruleArgumentDefinition )
            	    // InternalGaml.g:2695:6: lv_args_2_0= ruleArgumentDefinition
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_35);
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
            	    break loop36;
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
    // InternalGaml.g:2717:1: entryRuleArgumentDefinition returns [EObject current=null] : iv_ruleArgumentDefinition= ruleArgumentDefinition EOF ;
    public final EObject entryRuleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArgumentDefinition = null;


        try {
            // InternalGaml.g:2717:59: (iv_ruleArgumentDefinition= ruleArgumentDefinition EOF )
            // InternalGaml.g:2718:2: iv_ruleArgumentDefinition= ruleArgumentDefinition EOF
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
    // InternalGaml.g:2724:1: ruleArgumentDefinition returns [EObject current=null] : ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) ;
    public final EObject ruleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject lv_type_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_default_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2730:2: ( ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) )
            // InternalGaml.g:2731:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            {
            // InternalGaml.g:2731:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            // InternalGaml.g:2732:3: ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            {
            // InternalGaml.g:2732:3: ( (lv_type_0_0= ruleTypeRef ) )
            // InternalGaml.g:2733:4: (lv_type_0_0= ruleTypeRef )
            {
            // InternalGaml.g:2733:4: (lv_type_0_0= ruleTypeRef )
            // InternalGaml.g:2734:5: lv_type_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getTypeTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_10);
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

            // InternalGaml.g:2751:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:2752:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:2752:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:2753:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_36);
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

            // InternalGaml.g:2770:3: (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==15) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // InternalGaml.g:2771:4: otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) )
                    {
                    otherlv_2=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getArgumentDefinitionAccess().getLessThanSignHyphenMinusKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:2775:4: ( (lv_default_3_0= ruleExpression ) )
                    // InternalGaml.g:2776:5: (lv_default_3_0= ruleExpression )
                    {
                    // InternalGaml.g:2776:5: (lv_default_3_0= ruleExpression )
                    // InternalGaml.g:2777:6: lv_default_3_0= ruleExpression
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
    // InternalGaml.g:2799:1: entryRuleFacet returns [EObject current=null] : iv_ruleFacet= ruleFacet EOF ;
    public final EObject entryRuleFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFacet = null;


        try {
            // InternalGaml.g:2799:46: (iv_ruleFacet= ruleFacet EOF )
            // InternalGaml.g:2800:2: iv_ruleFacet= ruleFacet EOF
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
    // InternalGaml.g:2806:1: ruleFacet returns [EObject current=null] : (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) ;
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
            // InternalGaml.g:2812:2: ( (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet ) )
            // InternalGaml.g:2813:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            {
            // InternalGaml.g:2813:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_TypeFacet_3= ruleTypeFacet | this_VarFacet_4= ruleVarFacet | this_FunctionFacet_5= ruleFunctionFacet )
            int alt38=6;
            switch ( input.LA(1) ) {
            case 107:
            case 108:
                {
                alt38=1;
                }
                break;
            case 82:
            case 83:
                {
                alt38=2;
                }
                break;
            case RULE_ID:
            case 15:
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
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
                {
                alt38=3;
                }
                break;
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
                {
                alt38=4;
                }
                break;
            case 109:
                {
                alt38=5;
                }
                break;
            case 110:
                {
                alt38=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }

            switch (alt38) {
                case 1 :
                    // InternalGaml.g:2814:3: this_ActionFacet_0= ruleActionFacet
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
                    // InternalGaml.g:2823:3: this_DefinitionFacet_1= ruleDefinitionFacet
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
                    // InternalGaml.g:2832:3: this_ClassicFacet_2= ruleClassicFacet
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
                    // InternalGaml.g:2841:3: this_TypeFacet_3= ruleTypeFacet
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
                    // InternalGaml.g:2850:3: this_VarFacet_4= ruleVarFacet
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
                    // InternalGaml.g:2859:3: this_FunctionFacet_5= ruleFunctionFacet
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
    // InternalGaml.g:2871:1: entryRuleClassicFacetKey returns [String current=null] : iv_ruleClassicFacetKey= ruleClassicFacetKey EOF ;
    public final String entryRuleClassicFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleClassicFacetKey = null;


        try {
            // InternalGaml.g:2871:55: (iv_ruleClassicFacetKey= ruleClassicFacetKey EOF )
            // InternalGaml.g:2872:2: iv_ruleClassicFacetKey= ruleClassicFacetKey EOF
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
    // InternalGaml.g:2878:1: ruleClassicFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID kw= ':' ) ;
    public final AntlrDatatypeRuleToken ruleClassicFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2884:2: ( (this_ID_0= RULE_ID kw= ':' ) )
            // InternalGaml.g:2885:2: (this_ID_0= RULE_ID kw= ':' )
            {
            // InternalGaml.g:2885:2: (this_ID_0= RULE_ID kw= ':' )
            // InternalGaml.g:2886:3: this_ID_0= RULE_ID kw= ':'
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_37); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(this_ID_0);
              		
            }
            if ( state.backtracking==0 ) {

              			newLeafNode(this_ID_0, grammarAccess.getClassicFacetKeyAccess().getIDTerminalRuleCall_0());
              		
            }
            kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2902:1: entryRuleDefinitionFacetKey returns [String current=null] : iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF ;
    public final String entryRuleDefinitionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleDefinitionFacetKey = null;


        try {
            // InternalGaml.g:2902:58: (iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF )
            // InternalGaml.g:2903:2: iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF
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
    // InternalGaml.g:2909:1: ruleDefinitionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'name:' | kw= 'returns:' ) ;
    public final AntlrDatatypeRuleToken ruleDefinitionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2915:2: ( (kw= 'name:' | kw= 'returns:' ) )
            // InternalGaml.g:2916:2: (kw= 'name:' | kw= 'returns:' )
            {
            // InternalGaml.g:2916:2: (kw= 'name:' | kw= 'returns:' )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==82) ) {
                alt39=1;
            }
            else if ( (LA39_0==83) ) {
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
                    // InternalGaml.g:2917:3: kw= 'name:'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getDefinitionFacetKeyAccess().getNameKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2923:3: kw= 'returns:'
                    {
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2932:1: entryRuleTypeFacetKey returns [String current=null] : iv_ruleTypeFacetKey= ruleTypeFacetKey EOF ;
    public final String entryRuleTypeFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleTypeFacetKey = null;


        try {
            // InternalGaml.g:2932:52: (iv_ruleTypeFacetKey= ruleTypeFacetKey EOF )
            // InternalGaml.g:2933:2: iv_ruleTypeFacetKey= ruleTypeFacetKey EOF
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
    // InternalGaml.g:2939:1: ruleTypeFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) ;
    public final AntlrDatatypeRuleToken ruleTypeFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2945:2: ( (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' ) )
            // InternalGaml.g:2946:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            {
            // InternalGaml.g:2946:2: (kw= 'as:' | kw= 'of:' | kw= 'parent:' | kw= 'species:' | kw= 'type:' )
            int alt40=5;
            switch ( input.LA(1) ) {
            case 84:
                {
                alt40=1;
                }
                break;
            case 85:
                {
                alt40=2;
                }
                break;
            case 86:
                {
                alt40=3;
                }
                break;
            case 87:
                {
                alt40=4;
                }
                break;
            case 88:
                {
                alt40=5;
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
                    // InternalGaml.g:2947:3: kw= 'as:'
                    {
                    kw=(Token)match(input,84,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getAsKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2953:3: kw= 'of:'
                    {
                    kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getOfKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2959:3: kw= 'parent:'
                    {
                    kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getParentKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:2965:3: kw= 'species:'
                    {
                    kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getTypeFacetKeyAccess().getSpeciesKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2971:3: kw= 'type:'
                    {
                    kw=(Token)match(input,88,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:2980:1: entryRuleSpecialFacetKey returns [String current=null] : iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF ;
    public final String entryRuleSpecialFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSpecialFacetKey = null;


        try {
            // InternalGaml.g:2980:55: (iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF )
            // InternalGaml.g:2981:2: iv_ruleSpecialFacetKey= ruleSpecialFacetKey EOF
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
    // InternalGaml.g:2987:1: ruleSpecialFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) ;
    public final AntlrDatatypeRuleToken ruleSpecialFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2993:2: ( (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' ) )
            // InternalGaml.g:2994:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            {
            // InternalGaml.g:2994:2: (kw= 'camera:' | kw= 'data:' | (kw= 'when' kw= ':' ) | kw= 'const:' | kw= 'value:' | kw= 'topology:' | kw= 'item:' | kw= 'init:' | kw= 'message:' | kw= 'control:' | kw= 'layout:' | kw= 'environment:' | kw= 'text:' | kw= 'image:' | kw= 'using:' | kw= 'parameter:' | kw= 'aspect:' | kw= 'light:' )
            int alt41=18;
            switch ( input.LA(1) ) {
            case 89:
                {
                alt41=1;
                }
                break;
            case 90:
                {
                alt41=2;
                }
                break;
            case 91:
                {
                alt41=3;
                }
                break;
            case 92:
                {
                alt41=4;
                }
                break;
            case 93:
                {
                alt41=5;
                }
                break;
            case 94:
                {
                alt41=6;
                }
                break;
            case 95:
                {
                alt41=7;
                }
                break;
            case 96:
                {
                alt41=8;
                }
                break;
            case 97:
                {
                alt41=9;
                }
                break;
            case 98:
                {
                alt41=10;
                }
                break;
            case 99:
                {
                alt41=11;
                }
                break;
            case 100:
                {
                alt41=12;
                }
                break;
            case 101:
                {
                alt41=13;
                }
                break;
            case 102:
                {
                alt41=14;
                }
                break;
            case 103:
                {
                alt41=15;
                }
                break;
            case 104:
                {
                alt41=16;
                }
                break;
            case 105:
                {
                alt41=17;
                }
                break;
            case 106:
                {
                alt41=18;
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
                    // InternalGaml.g:2995:3: kw= 'camera:'
                    {
                    kw=(Token)match(input,89,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getCameraKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3001:3: kw= 'data:'
                    {
                    kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getDataKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:3007:3: (kw= 'when' kw= ':' )
                    {
                    // InternalGaml.g:3007:3: (kw= 'when' kw= ':' )
                    // InternalGaml.g:3008:4: kw= 'when' kw= ':'
                    {
                    kw=(Token)match(input,91,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getWhenKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getColonKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:3020:3: kw= 'const:'
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getConstKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:3026:3: kw= 'value:'
                    {
                    kw=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getValueKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:3032:3: kw= 'topology:'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTopologyKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:3038:3: kw= 'item:'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getItemKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:3044:3: kw= 'init:'
                    {
                    kw=(Token)match(input,96,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getInitKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:3050:3: kw= 'message:'
                    {
                    kw=(Token)match(input,97,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getMessageKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:3056:3: kw= 'control:'
                    {
                    kw=(Token)match(input,98,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getControlKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:3062:3: kw= 'layout:'
                    {
                    kw=(Token)match(input,99,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getLayoutKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:3068:3: kw= 'environment:'
                    {
                    kw=(Token)match(input,100,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getEnvironmentKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:3074:3: kw= 'text:'
                    {
                    kw=(Token)match(input,101,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getTextKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:3080:3: kw= 'image:'
                    {
                    kw=(Token)match(input,102,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getImageKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:3086:3: kw= 'using:'
                    {
                    kw=(Token)match(input,103,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getUsingKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:3092:3: kw= 'parameter:'
                    {
                    kw=(Token)match(input,104,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getParameterKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:3098:3: kw= 'aspect:'
                    {
                    kw=(Token)match(input,105,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSpecialFacetKeyAccess().getAspectKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:3104:3: kw= 'light:'
                    {
                    kw=(Token)match(input,106,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3113:1: entryRuleActionFacetKey returns [String current=null] : iv_ruleActionFacetKey= ruleActionFacetKey EOF ;
    public final String entryRuleActionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleActionFacetKey = null;


        try {
            // InternalGaml.g:3113:54: (iv_ruleActionFacetKey= ruleActionFacetKey EOF )
            // InternalGaml.g:3114:2: iv_ruleActionFacetKey= ruleActionFacetKey EOF
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
    // InternalGaml.g:3120:1: ruleActionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'action:' | kw= 'on_change:' ) ;
    public final AntlrDatatypeRuleToken ruleActionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3126:2: ( (kw= 'action:' | kw= 'on_change:' ) )
            // InternalGaml.g:3127:2: (kw= 'action:' | kw= 'on_change:' )
            {
            // InternalGaml.g:3127:2: (kw= 'action:' | kw= 'on_change:' )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==107) ) {
                alt42=1;
            }
            else if ( (LA42_0==108) ) {
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
                    // InternalGaml.g:3128:3: kw= 'action:'
                    {
                    kw=(Token)match(input,107,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getActionFacetKeyAccess().getActionKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3134:3: kw= 'on_change:'
                    {
                    kw=(Token)match(input,108,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3143:1: entryRuleVarFacetKey returns [String current=null] : iv_ruleVarFacetKey= ruleVarFacetKey EOF ;
    public final String entryRuleVarFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleVarFacetKey = null;


        try {
            // InternalGaml.g:3143:51: (iv_ruleVarFacetKey= ruleVarFacetKey EOF )
            // InternalGaml.g:3144:2: iv_ruleVarFacetKey= ruleVarFacetKey EOF
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
    // InternalGaml.g:3150:1: ruleVarFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'var:' ;
    public final AntlrDatatypeRuleToken ruleVarFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3156:2: (kw= 'var:' )
            // InternalGaml.g:3157:2: kw= 'var:'
            {
            kw=(Token)match(input,109,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3165:1: entryRuleClassicFacet returns [EObject current=null] : iv_ruleClassicFacet= ruleClassicFacet EOF ;
    public final EObject entryRuleClassicFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassicFacet = null;


        try {
            // InternalGaml.g:3165:53: (iv_ruleClassicFacet= ruleClassicFacet EOF )
            // InternalGaml.g:3166:2: iv_ruleClassicFacet= ruleClassicFacet EOF
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
    // InternalGaml.g:3172:1: ruleClassicFacet returns [EObject current=null] : ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) ;
    public final EObject ruleClassicFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_key_2_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3178:2: ( ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) ) )
            // InternalGaml.g:3179:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            {
            // InternalGaml.g:3179:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) ) )
            // InternalGaml.g:3180:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) ) ( (lv_expr_3_0= ruleExpression ) )
            {
            // InternalGaml.g:3180:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) | ( (lv_key_2_0= ruleSpecialFacetKey ) ) )
            int alt43=3;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt43=1;
                }
                break;
            case 15:
                {
                alt43=2;
                }
                break;
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
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
                {
                alt43=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }

            switch (alt43) {
                case 1 :
                    // InternalGaml.g:3181:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    {
                    // InternalGaml.g:3181:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    // InternalGaml.g:3182:5: (lv_key_0_0= ruleClassicFacetKey )
                    {
                    // InternalGaml.g:3182:5: (lv_key_0_0= ruleClassicFacetKey )
                    // InternalGaml.g:3183:6: lv_key_0_0= ruleClassicFacetKey
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
                    // InternalGaml.g:3201:4: ( (lv_key_1_0= '<-' ) )
                    {
                    // InternalGaml.g:3201:4: ( (lv_key_1_0= '<-' ) )
                    // InternalGaml.g:3202:5: (lv_key_1_0= '<-' )
                    {
                    // InternalGaml.g:3202:5: (lv_key_1_0= '<-' )
                    // InternalGaml.g:3203:6: lv_key_1_0= '<-'
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
                    // InternalGaml.g:3216:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    {
                    // InternalGaml.g:3216:4: ( (lv_key_2_0= ruleSpecialFacetKey ) )
                    // InternalGaml.g:3217:5: (lv_key_2_0= ruleSpecialFacetKey )
                    {
                    // InternalGaml.g:3217:5: (lv_key_2_0= ruleSpecialFacetKey )
                    // InternalGaml.g:3218:6: lv_key_2_0= ruleSpecialFacetKey
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

            // InternalGaml.g:3236:3: ( (lv_expr_3_0= ruleExpression ) )
            // InternalGaml.g:3237:4: (lv_expr_3_0= ruleExpression )
            {
            // InternalGaml.g:3237:4: (lv_expr_3_0= ruleExpression )
            // InternalGaml.g:3238:5: lv_expr_3_0= ruleExpression
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
    // InternalGaml.g:3259:1: entryRuleDefinitionFacet returns [EObject current=null] : iv_ruleDefinitionFacet= ruleDefinitionFacet EOF ;
    public final EObject entryRuleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDefinitionFacet = null;


        try {
            // InternalGaml.g:3259:56: (iv_ruleDefinitionFacet= ruleDefinitionFacet EOF )
            // InternalGaml.g:3260:2: iv_ruleDefinitionFacet= ruleDefinitionFacet EOF
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
    // InternalGaml.g:3266:1: ruleDefinitionFacet returns [EObject current=null] : ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) ;
    public final EObject ruleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:3272:2: ( ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ) )
            // InternalGaml.g:3273:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            {
            // InternalGaml.g:3273:2: ( ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) )
            // InternalGaml.g:3274:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            {
            // InternalGaml.g:3274:3: ( ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) )
            // InternalGaml.g:3275:4: ( 'name:' | 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey )
            {
            // InternalGaml.g:3276:4: (lv_key_0_0= ruleDefinitionFacetKey )
            // InternalGaml.g:3277:5: lv_key_0_0= ruleDefinitionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getDefinitionFacetAccess().getKeyDefinitionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_6);
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

            // InternalGaml.g:3294:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:3295:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:3295:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:3296:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:3296:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==RULE_ID||(LA44_0>=42 && LA44_0<=72)) ) {
                alt44=1;
            }
            else if ( (LA44_0==RULE_STRING) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // InternalGaml.g:3297:6: lv_name_1_1= ruleValid_ID
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
                    // InternalGaml.g:3313:6: lv_name_1_2= RULE_STRING
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
    // InternalGaml.g:3334:1: entryRuleFunctionFacet returns [EObject current=null] : iv_ruleFunctionFacet= ruleFunctionFacet EOF ;
    public final EObject entryRuleFunctionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunctionFacet = null;


        try {
            // InternalGaml.g:3334:54: (iv_ruleFunctionFacet= ruleFunctionFacet EOF )
            // InternalGaml.g:3335:2: iv_ruleFunctionFacet= ruleFunctionFacet EOF
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
    // InternalGaml.g:3341:1: ruleFunctionFacet returns [EObject current=null] : ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) ;
    public final EObject ruleFunctionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_expr_1_0 = null;

        EObject lv_expr_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3347:2: ( ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) ) )
            // InternalGaml.g:3348:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            {
            // InternalGaml.g:3348:2: ( ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) ) )
            // InternalGaml.g:3349:3: ( (lv_key_0_0= '->' ) ) ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            {
            // InternalGaml.g:3349:3: ( (lv_key_0_0= '->' ) )
            // InternalGaml.g:3350:4: (lv_key_0_0= '->' )
            {
            // InternalGaml.g:3350:4: (lv_key_0_0= '->' )
            // InternalGaml.g:3351:5: lv_key_0_0= '->'
            {
            lv_key_0_0=(Token)match(input,110,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:3363:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )
            int alt45=2;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // InternalGaml.g:3364:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    {
                    // InternalGaml.g:3364:4: ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) )
                    // InternalGaml.g:3365:5: ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) )
                    {
                    // InternalGaml.g:3371:5: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:3372:6: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:3372:6: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:3373:7: lv_expr_1_0= ruleExpression
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
                    // InternalGaml.g:3392:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    {
                    // InternalGaml.g:3392:4: (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' )
                    // InternalGaml.g:3393:5: otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}'
                    {
                    otherlv_2=(Token)match(input,37,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getFunctionFacetAccess().getLeftCurlyBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:3397:5: ( (lv_expr_3_0= ruleExpression ) )
                    // InternalGaml.g:3398:6: (lv_expr_3_0= ruleExpression )
                    {
                    // InternalGaml.g:3398:6: (lv_expr_3_0= ruleExpression )
                    // InternalGaml.g:3399:7: lv_expr_3_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getFunctionFacetAccess().getExprExpressionParserRuleCall_1_1_1_0());
                      						
                    }
                    pushFollow(FOLLOW_38);
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

                    otherlv_4=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3426:1: entryRuleTypeFacet returns [EObject current=null] : iv_ruleTypeFacet= ruleTypeFacet EOF ;
    public final EObject entryRuleTypeFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFacet = null;


        try {
            // InternalGaml.g:3426:50: (iv_ruleTypeFacet= ruleTypeFacet EOF )
            // InternalGaml.g:3427:2: iv_ruleTypeFacet= ruleTypeFacet EOF
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
    // InternalGaml.g:3433:1: ruleTypeFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) ;
    public final EObject ruleTypeFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3439:2: ( ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) ) )
            // InternalGaml.g:3440:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            {
            // InternalGaml.g:3440:2: ( ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:3441:3: ( (lv_key_0_0= ruleTypeFacetKey ) ) ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:3441:3: ( (lv_key_0_0= ruleTypeFacetKey ) )
            // InternalGaml.g:3442:4: (lv_key_0_0= ruleTypeFacetKey )
            {
            // InternalGaml.g:3442:4: (lv_key_0_0= ruleTypeFacetKey )
            // InternalGaml.g:3443:5: lv_key_0_0= ruleTypeFacetKey
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

            // InternalGaml.g:3460:3: ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )
            int alt46=2;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // InternalGaml.g:3461:4: ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    {
                    // InternalGaml.g:3461:4: ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) )
                    // InternalGaml.g:3462:5: ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) )
                    {
                    // InternalGaml.g:3463:5: ( (lv_expr_1_0= ruleTypeRef ) )
                    // InternalGaml.g:3464:6: (lv_expr_1_0= ruleTypeRef )
                    {
                    // InternalGaml.g:3464:6: (lv_expr_1_0= ruleTypeRef )
                    // InternalGaml.g:3465:7: lv_expr_1_0= ruleTypeRef
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
                    // InternalGaml.g:3484:4: ( (lv_expr_2_0= ruleExpression ) )
                    {
                    // InternalGaml.g:3484:4: ( (lv_expr_2_0= ruleExpression ) )
                    // InternalGaml.g:3485:5: (lv_expr_2_0= ruleExpression )
                    {
                    // InternalGaml.g:3485:5: (lv_expr_2_0= ruleExpression )
                    // InternalGaml.g:3486:6: lv_expr_2_0= ruleExpression
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
    // InternalGaml.g:3508:1: entryRuleActionFacet returns [EObject current=null] : iv_ruleActionFacet= ruleActionFacet EOF ;
    public final EObject entryRuleActionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFacet = null;


        try {
            // InternalGaml.g:3508:52: (iv_ruleActionFacet= ruleActionFacet EOF )
            // InternalGaml.g:3509:2: iv_ruleActionFacet= ruleActionFacet EOF
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
    // InternalGaml.g:3515:1: ruleActionFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) ;
    public final EObject ruleActionFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3521:2: ( ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) )
            // InternalGaml.g:3522:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            {
            // InternalGaml.g:3522:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            // InternalGaml.g:3523:3: ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            {
            // InternalGaml.g:3523:3: ( (lv_key_0_0= ruleActionFacetKey ) )
            // InternalGaml.g:3524:4: (lv_key_0_0= ruleActionFacetKey )
            {
            // InternalGaml.g:3524:4: (lv_key_0_0= ruleActionFacetKey )
            // InternalGaml.g:3525:5: lv_key_0_0= ruleActionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionFacetAccess().getKeyActionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_39);
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

            // InternalGaml.g:3542:3: ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==RULE_ID||(LA47_0>=42 && LA47_0<=72)) ) {
                alt47=1;
            }
            else if ( (LA47_0==37) ) {
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
                    // InternalGaml.g:3543:4: ( (lv_expr_1_0= ruleActionRef ) )
                    {
                    // InternalGaml.g:3543:4: ( (lv_expr_1_0= ruleActionRef ) )
                    // InternalGaml.g:3544:5: (lv_expr_1_0= ruleActionRef )
                    {
                    // InternalGaml.g:3544:5: (lv_expr_1_0= ruleActionRef )
                    // InternalGaml.g:3545:6: lv_expr_1_0= ruleActionRef
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
                    // InternalGaml.g:3563:4: ( (lv_block_2_0= ruleBlock ) )
                    {
                    // InternalGaml.g:3563:4: ( (lv_block_2_0= ruleBlock ) )
                    // InternalGaml.g:3564:5: (lv_block_2_0= ruleBlock )
                    {
                    // InternalGaml.g:3564:5: (lv_block_2_0= ruleBlock )
                    // InternalGaml.g:3565:6: lv_block_2_0= ruleBlock
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
    // InternalGaml.g:3587:1: entryRuleVarFacet returns [EObject current=null] : iv_ruleVarFacet= ruleVarFacet EOF ;
    public final EObject entryRuleVarFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFacet = null;


        try {
            // InternalGaml.g:3587:49: (iv_ruleVarFacet= ruleVarFacet EOF )
            // InternalGaml.g:3588:2: iv_ruleVarFacet= ruleVarFacet EOF
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
    // InternalGaml.g:3594:1: ruleVarFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) ;
    public final EObject ruleVarFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3600:2: ( ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) ) )
            // InternalGaml.g:3601:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            {
            // InternalGaml.g:3601:2: ( ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) ) )
            // InternalGaml.g:3602:3: ( (lv_key_0_0= ruleVarFacetKey ) ) ( (lv_expr_1_0= ruleVariableRef ) )
            {
            // InternalGaml.g:3602:3: ( (lv_key_0_0= ruleVarFacetKey ) )
            // InternalGaml.g:3603:4: (lv_key_0_0= ruleVarFacetKey )
            {
            // InternalGaml.g:3603:4: (lv_key_0_0= ruleVarFacetKey )
            // InternalGaml.g:3604:5: lv_key_0_0= ruleVarFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getVarFacetAccess().getKeyVarFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_10);
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

            // InternalGaml.g:3621:3: ( (lv_expr_1_0= ruleVariableRef ) )
            // InternalGaml.g:3622:4: (lv_expr_1_0= ruleVariableRef )
            {
            // InternalGaml.g:3622:4: (lv_expr_1_0= ruleVariableRef )
            // InternalGaml.g:3623:5: lv_expr_1_0= ruleVariableRef
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
    // InternalGaml.g:3644:1: entryRuleBlock returns [EObject current=null] : iv_ruleBlock= ruleBlock EOF ;
    public final EObject entryRuleBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBlock = null;


        try {
            // InternalGaml.g:3644:46: (iv_ruleBlock= ruleBlock EOF )
            // InternalGaml.g:3645:2: iv_ruleBlock= ruleBlock EOF
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
    // InternalGaml.g:3651:1: ruleBlock returns [EObject current=null] : ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) ;
    public final EObject ruleBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3657:2: ( ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) )
            // InternalGaml.g:3658:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            {
            // InternalGaml.g:3658:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            // InternalGaml.g:3659:3: () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:3659:3: ()
            // InternalGaml.g:3660:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,37,FOLLOW_40); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:3670:3: ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            // InternalGaml.g:3671:4: ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}'
            {
            // InternalGaml.g:3671:4: ( (lv_statements_2_0= ruleStatement ) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( ((LA48_0>=RULE_ID && LA48_0<=RULE_KEYWORD)||LA48_0==21||(LA48_0>=25 && LA48_0<=28)||LA48_0==30||(LA48_0>=32 && LA48_0<=33)||(LA48_0>=35 && LA48_0<=37)||(LA48_0>=40 && LA48_0<=73)||LA48_0==120||(LA48_0>=124 && LA48_0<=126)) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // InternalGaml.g:3672:5: (lv_statements_2_0= ruleStatement )
            	    {
            	    // InternalGaml.g:3672:5: (lv_statements_2_0= ruleStatement )
            	    // InternalGaml.g:3673:6: lv_statements_2_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBlockAccess().getStatementsStatementParserRuleCall_2_0_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_40);
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
            	    break loop48;
                }
            } while (true);

            otherlv_3=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleModelBlock"
    // InternalGaml.g:3699:1: entryRuleModelBlock returns [EObject current=null] : iv_ruleModelBlock= ruleModelBlock EOF ;
    public final EObject entryRuleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModelBlock = null;


        try {
            // InternalGaml.g:3699:51: (iv_ruleModelBlock= ruleModelBlock EOF )
            // InternalGaml.g:3700:2: iv_ruleModelBlock= ruleModelBlock EOF
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
    // InternalGaml.g:3706:1: ruleModelBlock returns [EObject current=null] : ( () ( (lv_statements_1_0= ruleS_Section ) )* ) ;
    public final EObject ruleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3712:2: ( ( () ( (lv_statements_1_0= ruleS_Section ) )* ) )
            // InternalGaml.g:3713:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            {
            // InternalGaml.g:3713:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            // InternalGaml.g:3714:3: () ( (lv_statements_1_0= ruleS_Section ) )*
            {
            // InternalGaml.g:3714:3: ()
            // InternalGaml.g:3715:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getModelBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:3721:3: ( (lv_statements_1_0= ruleS_Section ) )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==24||(LA49_0>=42 && LA49_0<=44)) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // InternalGaml.g:3722:4: (lv_statements_1_0= ruleS_Section )
            	    {
            	    // InternalGaml.g:3722:4: (lv_statements_1_0= ruleS_Section )
            	    // InternalGaml.g:3723:5: lv_statements_1_0= ruleS_Section
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelBlockAccess().getStatementsS_SectionParserRuleCall_1_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_41);
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
            	    break loop49;
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


    // $ANTLR start "entryRuleDisplayBlock"
    // InternalGaml.g:3744:1: entryRuleDisplayBlock returns [EObject current=null] : iv_ruleDisplayBlock= ruleDisplayBlock EOF ;
    public final EObject entryRuleDisplayBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDisplayBlock = null;


        try {
            // InternalGaml.g:3744:53: (iv_ruleDisplayBlock= ruleDisplayBlock EOF )
            // InternalGaml.g:3745:2: iv_ruleDisplayBlock= ruleDisplayBlock EOF
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
    // InternalGaml.g:3751:1: ruleDisplayBlock returns [EObject current=null] : ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' ) ;
    public final EObject ruleDisplayBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3757:2: ( ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' ) )
            // InternalGaml.g:3758:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:3758:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' )
            // InternalGaml.g:3759:3: () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}'
            {
            // InternalGaml.g:3759:3: ()
            // InternalGaml.g:3760:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getDisplayBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,37,FOLLOW_40); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getDisplayBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:3770:3: ( (lv_statements_2_0= ruleS_Other ) )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==RULE_ID||(LA50_0>=42 && LA50_0<=72)) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // InternalGaml.g:3771:4: (lv_statements_2_0= ruleS_Other )
            	    {
            	    // InternalGaml.g:3771:4: (lv_statements_2_0= ruleS_Other )
            	    // InternalGaml.g:3772:5: lv_statements_2_0= ruleS_Other
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getDisplayBlockAccess().getStatementsS_OtherParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_40);
            	    lv_statements_2_0=ruleS_Other();

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
            	      						"gaml.compiler.Gaml.S_Other");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);

            otherlv_3=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleExpression"
    // InternalGaml.g:3797:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalGaml.g:3797:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalGaml.g:3798:2: iv_ruleExpression= ruleExpression EOF
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
    // InternalGaml.g:3804:1: ruleExpression returns [EObject current=null] : this_Pair_0= rulePair ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        EObject this_Pair_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3810:2: (this_Pair_0= rulePair )
            // InternalGaml.g:3811:2: this_Pair_0= rulePair
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
    // InternalGaml.g:3822:1: entryRulePair returns [EObject current=null] : iv_rulePair= rulePair EOF ;
    public final EObject entryRulePair() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePair = null;


        try {
            // InternalGaml.g:3822:45: (iv_rulePair= rulePair EOF )
            // InternalGaml.g:3823:2: iv_rulePair= rulePair EOF
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
    // InternalGaml.g:3829:1: rulePair returns [EObject current=null] : (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) ;
    public final EObject rulePair() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_If_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3835:2: ( (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) )
            // InternalGaml.g:3836:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            {
            // InternalGaml.g:3836:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            // InternalGaml.g:3837:3: this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getPairAccess().getIfParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_42);
            this_If_0=ruleIf();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_If_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3845:3: ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==111) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // InternalGaml.g:3846:4: () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) )
                    {
                    // InternalGaml.g:3846:4: ()
                    // InternalGaml.g:3847:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getPairAccess().getBinaryOperatorLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:3853:4: ( (lv_op_2_0= '::' ) )
                    // InternalGaml.g:3854:5: (lv_op_2_0= '::' )
                    {
                    // InternalGaml.g:3854:5: (lv_op_2_0= '::' )
                    // InternalGaml.g:3855:6: lv_op_2_0= '::'
                    {
                    lv_op_2_0=(Token)match(input,111,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:3867:4: ( (lv_right_3_0= ruleIf ) )
                    // InternalGaml.g:3868:5: (lv_right_3_0= ruleIf )
                    {
                    // InternalGaml.g:3868:5: (lv_right_3_0= ruleIf )
                    // InternalGaml.g:3869:6: lv_right_3_0= ruleIf
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
    // InternalGaml.g:3891:1: entryRuleIf returns [EObject current=null] : iv_ruleIf= ruleIf EOF ;
    public final EObject entryRuleIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIf = null;


        try {
            // InternalGaml.g:3891:43: (iv_ruleIf= ruleIf EOF )
            // InternalGaml.g:3892:2: iv_ruleIf= ruleIf EOF
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
    // InternalGaml.g:3898:1: ruleIf returns [EObject current=null] : (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) ;
    public final EObject ruleIf() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        EObject this_Or_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_ifFalse_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3904:2: ( (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) )
            // InternalGaml.g:3905:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            {
            // InternalGaml.g:3905:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            // InternalGaml.g:3906:3: this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getIfAccess().getOrParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_43);
            this_Or_0=ruleOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Or_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3914:3: ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==112) ) {
                alt52=1;
            }
            switch (alt52) {
                case 1 :
                    // InternalGaml.g:3915:4: () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    {
                    // InternalGaml.g:3915:4: ()
                    // InternalGaml.g:3916:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getIfAccess().getIfLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:3922:4: ( (lv_op_2_0= '?' ) )
                    // InternalGaml.g:3923:5: (lv_op_2_0= '?' )
                    {
                    // InternalGaml.g:3923:5: (lv_op_2_0= '?' )
                    // InternalGaml.g:3924:6: lv_op_2_0= '?'
                    {
                    lv_op_2_0=(Token)match(input,112,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:3936:4: ( (lv_right_3_0= ruleOr ) )
                    // InternalGaml.g:3937:5: (lv_right_3_0= ruleOr )
                    {
                    // InternalGaml.g:3937:5: (lv_right_3_0= ruleOr )
                    // InternalGaml.g:3938:6: lv_right_3_0= ruleOr
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getIfAccess().getRightOrParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_37);
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

                    // InternalGaml.g:3955:4: (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    // InternalGaml.g:3956:5: otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) )
                    {
                    otherlv_4=(Token)match(input,81,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getIfAccess().getColonKeyword_1_3_0());
                      				
                    }
                    // InternalGaml.g:3960:5: ( (lv_ifFalse_5_0= ruleOr ) )
                    // InternalGaml.g:3961:6: (lv_ifFalse_5_0= ruleOr )
                    {
                    // InternalGaml.g:3961:6: (lv_ifFalse_5_0= ruleOr )
                    // InternalGaml.g:3962:7: lv_ifFalse_5_0= ruleOr
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
    // InternalGaml.g:3985:1: entryRuleOr returns [EObject current=null] : iv_ruleOr= ruleOr EOF ;
    public final EObject entryRuleOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOr = null;


        try {
            // InternalGaml.g:3985:43: (iv_ruleOr= ruleOr EOF )
            // InternalGaml.g:3986:2: iv_ruleOr= ruleOr EOF
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
    // InternalGaml.g:3992:1: ruleOr returns [EObject current=null] : (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) ;
    public final EObject ruleOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_And_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3998:2: ( (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) )
            // InternalGaml.g:3999:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            {
            // InternalGaml.g:3999:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            // InternalGaml.g:4000:3: this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getOrAccess().getAndParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_44);
            this_And_0=ruleAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_And_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4008:3: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            loop53:
            do {
                int alt53=2;
                int LA53_0 = input.LA(1);

                if ( (LA53_0==113) ) {
                    alt53=1;
                }


                switch (alt53) {
            	case 1 :
            	    // InternalGaml.g:4009:4: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) )
            	    {
            	    // InternalGaml.g:4009:4: ()
            	    // InternalGaml.g:4010:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getOrAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4016:4: ( (lv_op_2_0= 'or' ) )
            	    // InternalGaml.g:4017:5: (lv_op_2_0= 'or' )
            	    {
            	    // InternalGaml.g:4017:5: (lv_op_2_0= 'or' )
            	    // InternalGaml.g:4018:6: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,113,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4030:4: ( (lv_right_3_0= ruleAnd ) )
            	    // InternalGaml.g:4031:5: (lv_right_3_0= ruleAnd )
            	    {
            	    // InternalGaml.g:4031:5: (lv_right_3_0= ruleAnd )
            	    // InternalGaml.g:4032:6: lv_right_3_0= ruleAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOrAccess().getRightAndParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_44);
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
            	    break loop53;
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
    // InternalGaml.g:4054:1: entryRuleAnd returns [EObject current=null] : iv_ruleAnd= ruleAnd EOF ;
    public final EObject entryRuleAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAnd = null;


        try {
            // InternalGaml.g:4054:44: (iv_ruleAnd= ruleAnd EOF )
            // InternalGaml.g:4055:2: iv_ruleAnd= ruleAnd EOF
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
    // InternalGaml.g:4061:1: ruleAnd returns [EObject current=null] : (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) ;
    public final EObject ruleAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Cast_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4067:2: ( (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) )
            // InternalGaml.g:4068:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            {
            // InternalGaml.g:4068:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            // InternalGaml.g:4069:3: this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAndAccess().getCastParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_45);
            this_Cast_0=ruleCast();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Cast_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4077:3: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( (LA54_0==114) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // InternalGaml.g:4078:4: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) )
            	    {
            	    // InternalGaml.g:4078:4: ()
            	    // InternalGaml.g:4079:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAndAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4085:4: ( (lv_op_2_0= 'and' ) )
            	    // InternalGaml.g:4086:5: (lv_op_2_0= 'and' )
            	    {
            	    // InternalGaml.g:4086:5: (lv_op_2_0= 'and' )
            	    // InternalGaml.g:4087:6: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,114,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4099:4: ( (lv_right_3_0= ruleCast ) )
            	    // InternalGaml.g:4100:5: (lv_right_3_0= ruleCast )
            	    {
            	    // InternalGaml.g:4100:5: (lv_right_3_0= ruleCast )
            	    // InternalGaml.g:4101:6: lv_right_3_0= ruleCast
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAndAccess().getRightCastParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_45);
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
            	    break loop54;
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
    // InternalGaml.g:4123:1: entryRuleCast returns [EObject current=null] : iv_ruleCast= ruleCast EOF ;
    public final EObject entryRuleCast() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCast = null;


        try {
            // InternalGaml.g:4123:45: (iv_ruleCast= ruleCast EOF )
            // InternalGaml.g:4124:2: iv_ruleCast= ruleCast EOF
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
    // InternalGaml.g:4130:1: ruleCast returns [EObject current=null] : (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) ;
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
            // InternalGaml.g:4136:2: ( (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) )
            // InternalGaml.g:4137:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            {
            // InternalGaml.g:4137:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            // InternalGaml.g:4138:3: this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getCastAccess().getComparisonParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_12);
            this_Comparison_0=ruleComparison();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Comparison_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4146:3: ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==19) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // InternalGaml.g:4147:4: ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    {
                    // InternalGaml.g:4147:4: ( () ( (lv_op_2_0= 'as' ) ) )
                    // InternalGaml.g:4148:5: () ( (lv_op_2_0= 'as' ) )
                    {
                    // InternalGaml.g:4148:5: ()
                    // InternalGaml.g:4149:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getCastAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4155:5: ( (lv_op_2_0= 'as' ) )
                    // InternalGaml.g:4156:6: (lv_op_2_0= 'as' )
                    {
                    // InternalGaml.g:4156:6: (lv_op_2_0= 'as' )
                    // InternalGaml.g:4157:7: lv_op_2_0= 'as'
                    {
                    lv_op_2_0=(Token)match(input,19,FOLLOW_46); if (state.failed) return current;
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

                    // InternalGaml.g:4170:4: ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    int alt55=2;
                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==RULE_ID||LA55_0==42) ) {
                        alt55=1;
                    }
                    else if ( (LA55_0==33) ) {
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
                            // InternalGaml.g:4171:5: ( (lv_right_3_0= ruleTypeRef ) )
                            {
                            // InternalGaml.g:4171:5: ( (lv_right_3_0= ruleTypeRef ) )
                            // InternalGaml.g:4172:6: (lv_right_3_0= ruleTypeRef )
                            {
                            // InternalGaml.g:4172:6: (lv_right_3_0= ruleTypeRef )
                            // InternalGaml.g:4173:7: lv_right_3_0= ruleTypeRef
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
                            // InternalGaml.g:4191:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            {
                            // InternalGaml.g:4191:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            // InternalGaml.g:4192:6: otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')'
                            {
                            otherlv_4=(Token)match(input,33,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(otherlv_4, grammarAccess.getCastAccess().getLeftParenthesisKeyword_1_1_1_0());
                              					
                            }
                            // InternalGaml.g:4196:6: ( (lv_right_5_0= ruleTypeRef ) )
                            // InternalGaml.g:4197:7: (lv_right_5_0= ruleTypeRef )
                            {
                            // InternalGaml.g:4197:7: (lv_right_5_0= ruleTypeRef )
                            // InternalGaml.g:4198:8: lv_right_5_0= ruleTypeRef
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getCastAccess().getRightTypeRefParserRuleCall_1_1_1_1_0());
                              							
                            }
                            pushFollow(FOLLOW_28);
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

                            otherlv_6=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4226:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
    public final EObject entryRuleComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComparison = null;


        try {
            // InternalGaml.g:4226:51: (iv_ruleComparison= ruleComparison EOF )
            // InternalGaml.g:4227:2: iv_ruleComparison= ruleComparison EOF
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
    // InternalGaml.g:4233:1: ruleComparison returns [EObject current=null] : (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) ;
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
            // InternalGaml.g:4239:2: ( (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) )
            // InternalGaml.g:4240:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            {
            // InternalGaml.g:4240:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            // InternalGaml.g:4241:3: this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getComparisonAccess().getAdditionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_47);
            this_Addition_0=ruleAddition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Addition_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4249:3: ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==39||(LA58_0>=115 && LA58_0<=118)) ) {
                alt58=1;
            }
            else if ( (LA58_0==75) ) {
                int LA58_2 = input.LA(2);

                if ( ((LA58_2>=RULE_ID && LA58_2<=RULE_KEYWORD)||LA58_2==21||LA58_2==33||LA58_2==37||(LA58_2>=42 && LA58_2<=72)||LA58_2==120||(LA58_2>=124 && LA58_2<=126)) ) {
                    alt58=1;
                }
            }
            switch (alt58) {
                case 1 :
                    // InternalGaml.g:4250:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) )
                    {
                    // InternalGaml.g:4250:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) )
                    // InternalGaml.g:4251:5: () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    {
                    // InternalGaml.g:4251:5: ()
                    // InternalGaml.g:4252:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getComparisonAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4258:5: ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    // InternalGaml.g:4259:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    {
                    // InternalGaml.g:4259:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    // InternalGaml.g:4260:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    {
                    // InternalGaml.g:4260:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    int alt57=6;
                    switch ( input.LA(1) ) {
                    case 115:
                        {
                        alt57=1;
                        }
                        break;
                    case 39:
                        {
                        alt57=2;
                        }
                        break;
                    case 116:
                        {
                        alt57=3;
                        }
                        break;
                    case 117:
                        {
                        alt57=4;
                        }
                        break;
                    case 118:
                        {
                        alt57=5;
                        }
                        break;
                    case 75:
                        {
                        alt57=6;
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
                            // InternalGaml.g:4261:8: lv_op_2_1= '!='
                            {
                            lv_op_2_1=(Token)match(input,115,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:4272:8: lv_op_2_2= '='
                            {
                            lv_op_2_2=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:4283:8: lv_op_2_3= '>='
                            {
                            lv_op_2_3=(Token)match(input,116,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:4294:8: lv_op_2_4= '<='
                            {
                            lv_op_2_4=(Token)match(input,117,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:4305:8: lv_op_2_5= '<'
                            {
                            lv_op_2_5=(Token)match(input,118,FOLLOW_5); if (state.failed) return current;
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
                            // InternalGaml.g:4316:8: lv_op_2_6= '>'
                            {
                            lv_op_2_6=(Token)match(input,75,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:4330:4: ( (lv_right_3_0= ruleAddition ) )
                    // InternalGaml.g:4331:5: (lv_right_3_0= ruleAddition )
                    {
                    // InternalGaml.g:4331:5: (lv_right_3_0= ruleAddition )
                    // InternalGaml.g:4332:6: lv_right_3_0= ruleAddition
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
    // InternalGaml.g:4354:1: entryRuleAddition returns [EObject current=null] : iv_ruleAddition= ruleAddition EOF ;
    public final EObject entryRuleAddition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddition = null;


        try {
            // InternalGaml.g:4354:49: (iv_ruleAddition= ruleAddition EOF )
            // InternalGaml.g:4355:2: iv_ruleAddition= ruleAddition EOF
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
    // InternalGaml.g:4361:1: ruleAddition returns [EObject current=null] : (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) ;
    public final EObject ruleAddition() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_Multiplication_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4367:2: ( (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) )
            // InternalGaml.g:4368:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            {
            // InternalGaml.g:4368:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            // InternalGaml.g:4369:3: this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAdditionAccess().getMultiplicationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_48);
            this_Multiplication_0=ruleMultiplication();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Multiplication_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4377:3: ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( ((LA60_0>=119 && LA60_0<=120)) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // InternalGaml.g:4378:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) )
            	    {
            	    // InternalGaml.g:4378:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) )
            	    // InternalGaml.g:4379:5: () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    {
            	    // InternalGaml.g:4379:5: ()
            	    // InternalGaml.g:4380:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getAdditionAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4386:5: ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    // InternalGaml.g:4387:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    {
            	    // InternalGaml.g:4387:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    // InternalGaml.g:4388:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    {
            	    // InternalGaml.g:4388:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    int alt59=2;
            	    int LA59_0 = input.LA(1);

            	    if ( (LA59_0==119) ) {
            	        alt59=1;
            	    }
            	    else if ( (LA59_0==120) ) {
            	        alt59=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 59, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt59) {
            	        case 1 :
            	            // InternalGaml.g:4389:8: lv_op_2_1= '+'
            	            {
            	            lv_op_2_1=(Token)match(input,119,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:4400:8: lv_op_2_2= '-'
            	            {
            	            lv_op_2_2=(Token)match(input,120,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4414:4: ( (lv_right_3_0= ruleMultiplication ) )
            	    // InternalGaml.g:4415:5: (lv_right_3_0= ruleMultiplication )
            	    {
            	    // InternalGaml.g:4415:5: (lv_right_3_0= ruleMultiplication )
            	    // InternalGaml.g:4416:6: lv_right_3_0= ruleMultiplication
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAdditionAccess().getRightMultiplicationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_48);
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
    // $ANTLR end "ruleAddition"


    // $ANTLR start "entryRuleMultiplication"
    // InternalGaml.g:4438:1: entryRuleMultiplication returns [EObject current=null] : iv_ruleMultiplication= ruleMultiplication EOF ;
    public final EObject entryRuleMultiplication() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMultiplication = null;


        try {
            // InternalGaml.g:4438:55: (iv_ruleMultiplication= ruleMultiplication EOF )
            // InternalGaml.g:4439:2: iv_ruleMultiplication= ruleMultiplication EOF
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
    // InternalGaml.g:4445:1: ruleMultiplication returns [EObject current=null] : (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) ;
    public final EObject ruleMultiplication() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        EObject this_Binary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4451:2: ( (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) )
            // InternalGaml.g:4452:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            {
            // InternalGaml.g:4452:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            // InternalGaml.g:4453:3: this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getMultiplicationAccess().getBinaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_49);
            this_Binary_0=ruleBinary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Binary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4461:3: ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            loop62:
            do {
                int alt62=2;
                int LA62_0 = input.LA(1);

                if ( ((LA62_0>=121 && LA62_0<=123)) ) {
                    alt62=1;
                }


                switch (alt62) {
            	case 1 :
            	    // InternalGaml.g:4462:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) )
            	    {
            	    // InternalGaml.g:4462:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) )
            	    // InternalGaml.g:4463:5: () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    {
            	    // InternalGaml.g:4463:5: ()
            	    // InternalGaml.g:4464:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getMultiplicationAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4470:5: ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    // InternalGaml.g:4471:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    {
            	    // InternalGaml.g:4471:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    // InternalGaml.g:4472:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    {
            	    // InternalGaml.g:4472:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    int alt61=3;
            	    switch ( input.LA(1) ) {
            	    case 121:
            	        {
            	        alt61=1;
            	        }
            	        break;
            	    case 122:
            	        {
            	        alt61=2;
            	        }
            	        break;
            	    case 123:
            	        {
            	        alt61=3;
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
            	            // InternalGaml.g:4473:8: lv_op_2_1= '*'
            	            {
            	            lv_op_2_1=(Token)match(input,121,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:4484:8: lv_op_2_2= '/'
            	            {
            	            lv_op_2_2=(Token)match(input,122,FOLLOW_5); if (state.failed) return current;
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
            	            // InternalGaml.g:4495:8: lv_op_2_3= '^'
            	            {
            	            lv_op_2_3=(Token)match(input,123,FOLLOW_5); if (state.failed) return current;
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

            	    // InternalGaml.g:4509:4: ( (lv_right_3_0= ruleBinary ) )
            	    // InternalGaml.g:4510:5: (lv_right_3_0= ruleBinary )
            	    {
            	    // InternalGaml.g:4510:5: (lv_right_3_0= ruleBinary )
            	    // InternalGaml.g:4511:6: lv_right_3_0= ruleBinary
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getMultiplicationAccess().getRightBinaryParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_49);
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
            	    break loop62;
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
    // InternalGaml.g:4533:1: entryRuleBinary returns [EObject current=null] : iv_ruleBinary= ruleBinary EOF ;
    public final EObject entryRuleBinary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBinary = null;


        try {
            // InternalGaml.g:4533:47: (iv_ruleBinary= ruleBinary EOF )
            // InternalGaml.g:4534:2: iv_ruleBinary= ruleBinary EOF
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
    // InternalGaml.g:4540:1: ruleBinary returns [EObject current=null] : (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) ;
    public final EObject ruleBinary() throws RecognitionException {
        EObject current = null;

        EObject this_Unit_0 = null;

        AntlrDatatypeRuleToken lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4546:2: ( (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) )
            // InternalGaml.g:4547:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            {
            // InternalGaml.g:4547:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            // InternalGaml.g:4548:3: this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getBinaryAccess().getUnitParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_Unit_0=ruleUnit();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unit_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4556:3: ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            loop63:
            do {
                int alt63=2;
                int LA63_0 = input.LA(1);

                if ( (LA63_0==RULE_ID) ) {
                    int LA63_2 = input.LA(2);

                    if ( ((LA63_2>=RULE_ID && LA63_2<=RULE_KEYWORD)||LA63_2==21||LA63_2==33||LA63_2==37||(LA63_2>=42 && LA63_2<=72)||LA63_2==120||(LA63_2>=124 && LA63_2<=126)) ) {
                        alt63=1;
                    }


                }
                else if ( ((LA63_0>=42 && LA63_0<=72)) ) {
                    alt63=1;
                }


                switch (alt63) {
            	case 1 :
            	    // InternalGaml.g:4557:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) )
            	    {
            	    // InternalGaml.g:4557:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) )
            	    // InternalGaml.g:4558:5: () ( (lv_op_2_0= ruleValid_ID ) )
            	    {
            	    // InternalGaml.g:4558:5: ()
            	    // InternalGaml.g:4559:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getBinaryAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4565:5: ( (lv_op_2_0= ruleValid_ID ) )
            	    // InternalGaml.g:4566:6: (lv_op_2_0= ruleValid_ID )
            	    {
            	    // InternalGaml.g:4566:6: (lv_op_2_0= ruleValid_ID )
            	    // InternalGaml.g:4567:7: lv_op_2_0= ruleValid_ID
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

            	    // InternalGaml.g:4585:4: ( (lv_right_3_0= ruleUnit ) )
            	    // InternalGaml.g:4586:5: (lv_right_3_0= ruleUnit )
            	    {
            	    // InternalGaml.g:4586:5: (lv_right_3_0= ruleUnit )
            	    // InternalGaml.g:4587:6: lv_right_3_0= ruleUnit
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBinaryAccess().getRightUnitParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_50);
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
            	    break loop63;
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
    // InternalGaml.g:4609:1: entryRuleUnit returns [EObject current=null] : iv_ruleUnit= ruleUnit EOF ;
    public final EObject entryRuleUnit() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnit = null;


        try {
            // InternalGaml.g:4609:45: (iv_ruleUnit= ruleUnit EOF )
            // InternalGaml.g:4610:2: iv_ruleUnit= ruleUnit EOF
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
    // InternalGaml.g:4616:1: ruleUnit returns [EObject current=null] : (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) ;
    public final EObject ruleUnit() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Unary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4622:2: ( (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) )
            // InternalGaml.g:4623:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            {
            // InternalGaml.g:4623:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            // InternalGaml.g:4624:3: this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getUnitAccess().getUnaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_Unary_0=ruleUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4632:3: ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==124) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // InternalGaml.g:4633:4: ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) )
                    {
                    // InternalGaml.g:4633:4: ( () ( (lv_op_2_0= '#' ) ) )
                    // InternalGaml.g:4634:5: () ( (lv_op_2_0= '#' ) )
                    {
                    // InternalGaml.g:4634:5: ()
                    // InternalGaml.g:4635:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getUnitAccess().getUnitLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4641:5: ( (lv_op_2_0= '#' ) )
                    // InternalGaml.g:4642:6: (lv_op_2_0= '#' )
                    {
                    // InternalGaml.g:4642:6: (lv_op_2_0= '#' )
                    // InternalGaml.g:4643:7: lv_op_2_0= '#'
                    {
                    lv_op_2_0=(Token)match(input,124,FOLLOW_13); if (state.failed) return current;
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

                    // InternalGaml.g:4656:4: ( (lv_right_3_0= ruleUnitRef ) )
                    // InternalGaml.g:4657:5: (lv_right_3_0= ruleUnitRef )
                    {
                    // InternalGaml.g:4657:5: (lv_right_3_0= ruleUnitRef )
                    // InternalGaml.g:4658:6: lv_right_3_0= ruleUnitRef
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
    // InternalGaml.g:4680:1: entryRuleUnary returns [EObject current=null] : iv_ruleUnary= ruleUnary EOF ;
    public final EObject entryRuleUnary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnary = null;


        try {
            // InternalGaml.g:4680:46: (iv_ruleUnary= ruleUnary EOF )
            // InternalGaml.g:4681:2: iv_ruleUnary= ruleUnary EOF
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
    // InternalGaml.g:4687:1: ruleUnary returns [EObject current=null] : (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) ;
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
            // InternalGaml.g:4693:2: ( (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) )
            // InternalGaml.g:4694:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            {
            // InternalGaml.g:4694:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            int alt67=2;
            int LA67_0 = input.LA(1);

            if ( ((LA67_0>=RULE_ID && LA67_0<=RULE_KEYWORD)||LA67_0==21||LA67_0==33||LA67_0==37||(LA67_0>=42 && LA67_0<=72)) ) {
                alt67=1;
            }
            else if ( (LA67_0==120||(LA67_0>=124 && LA67_0<=126)) ) {
                alt67=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 67, 0, input);

                throw nvae;
            }
            switch (alt67) {
                case 1 :
                    // InternalGaml.g:4695:3: this_Access_0= ruleAccess
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
                    // InternalGaml.g:4704:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    {
                    // InternalGaml.g:4704:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    // InternalGaml.g:4705:4: () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    {
                    // InternalGaml.g:4705:4: ()
                    // InternalGaml.g:4706:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getUnaryAccess().getUnaryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4712:4: ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    int alt66=2;
                    int LA66_0 = input.LA(1);

                    if ( (LA66_0==124) ) {
                        alt66=1;
                    }
                    else if ( (LA66_0==120||(LA66_0>=125 && LA66_0<=126)) ) {
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
                            // InternalGaml.g:4713:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            {
                            // InternalGaml.g:4713:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            // InternalGaml.g:4714:6: ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) )
                            {
                            // InternalGaml.g:4714:6: ( (lv_op_2_0= '#' ) )
                            // InternalGaml.g:4715:7: (lv_op_2_0= '#' )
                            {
                            // InternalGaml.g:4715:7: (lv_op_2_0= '#' )
                            // InternalGaml.g:4716:8: lv_op_2_0= '#'
                            {
                            lv_op_2_0=(Token)match(input,124,FOLLOW_13); if (state.failed) return current;
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

                            // InternalGaml.g:4728:6: ( (lv_right_3_0= ruleUnitRef ) )
                            // InternalGaml.g:4729:7: (lv_right_3_0= ruleUnitRef )
                            {
                            // InternalGaml.g:4729:7: (lv_right_3_0= ruleUnitRef )
                            // InternalGaml.g:4730:8: lv_right_3_0= ruleUnitRef
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
                            // InternalGaml.g:4749:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            {
                            // InternalGaml.g:4749:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            // InternalGaml.g:4750:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) )
                            {
                            // InternalGaml.g:4750:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) )
                            // InternalGaml.g:4751:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            {
                            // InternalGaml.g:4751:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            // InternalGaml.g:4752:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            {
                            // InternalGaml.g:4752:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            int alt65=3;
                            switch ( input.LA(1) ) {
                            case 120:
                                {
                                alt65=1;
                                }
                                break;
                            case 125:
                                {
                                alt65=2;
                                }
                                break;
                            case 126:
                                {
                                alt65=3;
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
                                    // InternalGaml.g:4753:9: lv_op_4_1= '-'
                                    {
                                    lv_op_4_1=(Token)match(input,120,FOLLOW_5); if (state.failed) return current;
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
                                    // InternalGaml.g:4764:9: lv_op_4_2= '!'
                                    {
                                    lv_op_4_2=(Token)match(input,125,FOLLOW_5); if (state.failed) return current;
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
                                    // InternalGaml.g:4775:9: lv_op_4_3= 'not'
                                    {
                                    lv_op_4_3=(Token)match(input,126,FOLLOW_5); if (state.failed) return current;
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

                            // InternalGaml.g:4788:6: ( (lv_right_5_0= ruleUnary ) )
                            // InternalGaml.g:4789:7: (lv_right_5_0= ruleUnary )
                            {
                            // InternalGaml.g:4789:7: (lv_right_5_0= ruleUnary )
                            // InternalGaml.g:4790:8: lv_right_5_0= ruleUnary
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
    // InternalGaml.g:4814:1: entryRuleAccess returns [EObject current=null] : iv_ruleAccess= ruleAccess EOF ;
    public final EObject entryRuleAccess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAccess = null;


        try {
            // InternalGaml.g:4814:47: (iv_ruleAccess= ruleAccess EOF )
            // InternalGaml.g:4815:2: iv_ruleAccess= ruleAccess EOF
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
    // InternalGaml.g:4821:1: ruleAccess returns [EObject current=null] : (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) ;
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
            // InternalGaml.g:4827:2: ( (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) )
            // InternalGaml.g:4828:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            {
            // InternalGaml.g:4828:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            // InternalGaml.g:4829:3: this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAccessAccess().getPrimaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_Primary_0=rulePrimary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Primary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4837:3: ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            loop70:
            do {
                int alt70=2;
                int LA70_0 = input.LA(1);

                if ( (LA70_0==21||LA70_0==127) ) {
                    alt70=1;
                }


                switch (alt70) {
            	case 1 :
            	    // InternalGaml.g:4838:4: () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    {
            	    // InternalGaml.g:4838:4: ()
            	    // InternalGaml.g:4839:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAccessAccess().getAccessLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4845:4: ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    int alt69=2;
            	    int LA69_0 = input.LA(1);

            	    if ( (LA69_0==21) ) {
            	        alt69=1;
            	    }
            	    else if ( (LA69_0==127) ) {
            	        alt69=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 69, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt69) {
            	        case 1 :
            	            // InternalGaml.g:4846:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            {
            	            // InternalGaml.g:4846:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            // InternalGaml.g:4847:6: ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']'
            	            {
            	            // InternalGaml.g:4847:6: ( (lv_op_2_0= '[' ) )
            	            // InternalGaml.g:4848:7: (lv_op_2_0= '[' )
            	            {
            	            // InternalGaml.g:4848:7: (lv_op_2_0= '[' )
            	            // InternalGaml.g:4849:8: lv_op_2_0= '['
            	            {
            	            lv_op_2_0=(Token)match(input,21,FOLLOW_15); if (state.failed) return current;
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

            	            // InternalGaml.g:4861:6: ( (lv_right_3_0= ruleExpressionList ) )?
            	            int alt68=2;
            	            int LA68_0 = input.LA(1);

            	            if ( ((LA68_0>=RULE_ID && LA68_0<=RULE_KEYWORD)||LA68_0==21||LA68_0==33||LA68_0==37||(LA68_0>=42 && LA68_0<=72)||(LA68_0>=82 && LA68_0<=109)||LA68_0==120||(LA68_0>=124 && LA68_0<=126)) ) {
            	                alt68=1;
            	            }
            	            switch (alt68) {
            	                case 1 :
            	                    // InternalGaml.g:4862:7: (lv_right_3_0= ruleExpressionList )
            	                    {
            	                    // InternalGaml.g:4862:7: (lv_right_3_0= ruleExpressionList )
            	                    // InternalGaml.g:4863:8: lv_right_3_0= ruleExpressionList
            	                    {
            	                    if ( state.backtracking==0 ) {

            	                      								newCompositeNode(grammarAccess.getAccessAccess().getRightExpressionListParserRuleCall_1_1_0_1_0());
            	                      							
            	                    }
            	                    pushFollow(FOLLOW_16);
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

            	            otherlv_4=(Token)match(input,22,FOLLOW_52); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						newLeafNode(otherlv_4, grammarAccess.getAccessAccess().getRightSquareBracketKeyword_1_1_0_2());
            	              					
            	            }

            	            }


            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:4886:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            {
            	            // InternalGaml.g:4886:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            // InternalGaml.g:4887:6: ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) )
            	            {
            	            // InternalGaml.g:4887:6: ( (lv_op_5_0= '.' ) )
            	            // InternalGaml.g:4888:7: (lv_op_5_0= '.' )
            	            {
            	            // InternalGaml.g:4888:7: (lv_op_5_0= '.' )
            	            // InternalGaml.g:4889:8: lv_op_5_0= '.'
            	            {
            	            lv_op_5_0=(Token)match(input,127,FOLLOW_53); if (state.failed) return current;
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

            	            // InternalGaml.g:4901:6: ( (lv_right_6_0= rulePrimary ) )
            	            // InternalGaml.g:4902:7: (lv_right_6_0= rulePrimary )
            	            {
            	            // InternalGaml.g:4902:7: (lv_right_6_0= rulePrimary )
            	            // InternalGaml.g:4903:8: lv_right_6_0= rulePrimary
            	            {
            	            if ( state.backtracking==0 ) {

            	              								newCompositeNode(grammarAccess.getAccessAccess().getRightPrimaryParserRuleCall_1_1_1_1_0());
            	              							
            	            }
            	            pushFollow(FOLLOW_52);
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
            	    break loop70;
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
    // InternalGaml.g:4927:1: entryRulePrimary returns [EObject current=null] : iv_rulePrimary= rulePrimary EOF ;
    public final EObject entryRulePrimary() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePrimary = null;


        try {
            // InternalGaml.g:4927:48: (iv_rulePrimary= rulePrimary EOF )
            // InternalGaml.g:4928:2: iv_rulePrimary= rulePrimary EOF
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
    // InternalGaml.g:4934:1: rulePrimary returns [EObject current=null] : (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) ;
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
            // InternalGaml.g:4940:2: ( (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) )
            // InternalGaml.g:4941:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            {
            // InternalGaml.g:4941:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            int alt73=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
            case RULE_INTEGER:
            case RULE_DOUBLE:
            case RULE_BOOLEAN:
            case RULE_KEYWORD:
                {
                alt73=1;
                }
                break;
            case RULE_ID:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
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
                {
                alt73=2;
                }
                break;
            case 33:
                {
                alt73=3;
                }
                break;
            case 21:
                {
                alt73=4;
                }
                break;
            case 37:
                {
                alt73=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }

            switch (alt73) {
                case 1 :
                    // InternalGaml.g:4942:3: this_TerminalExpression_0= ruleTerminalExpression
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
                    // InternalGaml.g:4951:3: this_AbstractRef_1= ruleAbstractRef
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
                    // InternalGaml.g:4960:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    {
                    // InternalGaml.g:4960:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    // InternalGaml.g:4961:4: otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,33,FOLLOW_54); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getPrimaryAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getPrimaryAccess().getExpressionListParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_28);
                    this_ExpressionList_3=ruleExpressionList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ExpressionList_3;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_4=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getPrimaryAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:4979:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    {
                    // InternalGaml.g:4979:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    // InternalGaml.g:4980:4: otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']'
                    {
                    otherlv_5=(Token)match(input,21,FOLLOW_15); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getPrimaryAccess().getLeftSquareBracketKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:4984:4: ()
                    // InternalGaml.g:4985:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getArrayAction_3_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4991:4: ( (lv_exprs_7_0= ruleExpressionList ) )?
                    int alt71=2;
                    int LA71_0 = input.LA(1);

                    if ( ((LA71_0>=RULE_ID && LA71_0<=RULE_KEYWORD)||LA71_0==21||LA71_0==33||LA71_0==37||(LA71_0>=42 && LA71_0<=72)||(LA71_0>=82 && LA71_0<=109)||LA71_0==120||(LA71_0>=124 && LA71_0<=126)) ) {
                        alt71=1;
                    }
                    switch (alt71) {
                        case 1 :
                            // InternalGaml.g:4992:5: (lv_exprs_7_0= ruleExpressionList )
                            {
                            // InternalGaml.g:4992:5: (lv_exprs_7_0= ruleExpressionList )
                            // InternalGaml.g:4993:6: lv_exprs_7_0= ruleExpressionList
                            {
                            if ( state.backtracking==0 ) {

                              						newCompositeNode(grammarAccess.getPrimaryAccess().getExprsExpressionListParserRuleCall_3_2_0());
                              					
                            }
                            pushFollow(FOLLOW_16);
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

                    otherlv_8=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getPrimaryAccess().getRightSquareBracketKeyword_3_3());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalGaml.g:5016:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    {
                    // InternalGaml.g:5016:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    // InternalGaml.g:5017:4: otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}'
                    {
                    otherlv_9=(Token)match(input,37,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getPrimaryAccess().getLeftCurlyBracketKeyword_4_0());
                      			
                    }
                    // InternalGaml.g:5021:4: ()
                    // InternalGaml.g:5022:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getPointAction_4_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5028:4: ( (lv_left_11_0= ruleExpression ) )
                    // InternalGaml.g:5029:5: (lv_left_11_0= ruleExpression )
                    {
                    // InternalGaml.g:5029:5: (lv_left_11_0= ruleExpression )
                    // InternalGaml.g:5030:6: lv_left_11_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getLeftExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_55);
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

                    // InternalGaml.g:5047:4: ( (lv_op_12_0= ',' ) )
                    // InternalGaml.g:5048:5: (lv_op_12_0= ',' )
                    {
                    // InternalGaml.g:5048:5: (lv_op_12_0= ',' )
                    // InternalGaml.g:5049:6: lv_op_12_0= ','
                    {
                    lv_op_12_0=(Token)match(input,80,FOLLOW_5); if (state.failed) return current;
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

                    // InternalGaml.g:5061:4: ( (lv_right_13_0= ruleExpression ) )
                    // InternalGaml.g:5062:5: (lv_right_13_0= ruleExpression )
                    {
                    // InternalGaml.g:5062:5: (lv_right_13_0= ruleExpression )
                    // InternalGaml.g:5063:6: lv_right_13_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getRightExpressionParserRuleCall_4_4_0());
                      					
                    }
                    pushFollow(FOLLOW_56);
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

                    // InternalGaml.g:5080:4: (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )?
                    int alt72=2;
                    int LA72_0 = input.LA(1);

                    if ( (LA72_0==80) ) {
                        alt72=1;
                    }
                    switch (alt72) {
                        case 1 :
                            // InternalGaml.g:5081:5: otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) )
                            {
                            otherlv_14=(Token)match(input,80,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getPrimaryAccess().getCommaKeyword_4_5_0());
                              				
                            }
                            // InternalGaml.g:5085:5: ( (lv_z_15_0= ruleExpression ) )
                            // InternalGaml.g:5086:6: (lv_z_15_0= ruleExpression )
                            {
                            // InternalGaml.g:5086:6: (lv_z_15_0= ruleExpression )
                            // InternalGaml.g:5087:7: lv_z_15_0= ruleExpression
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPrimaryAccess().getZExpressionParserRuleCall_4_5_1_0());
                              						
                            }
                            pushFollow(FOLLOW_38);
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

                    otherlv_16=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5114:1: entryRuleAbstractRef returns [EObject current=null] : iv_ruleAbstractRef= ruleAbstractRef EOF ;
    public final EObject entryRuleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAbstractRef = null;


        try {
            // InternalGaml.g:5114:52: (iv_ruleAbstractRef= ruleAbstractRef EOF )
            // InternalGaml.g:5115:2: iv_ruleAbstractRef= ruleAbstractRef EOF
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
    // InternalGaml.g:5121:1: ruleAbstractRef returns [EObject current=null] : ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) ;
    public final EObject ruleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject this_Function_0 = null;

        EObject this_VariableRef_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5127:2: ( ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) )
            // InternalGaml.g:5128:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            {
            // InternalGaml.g:5128:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            int alt74=2;
            alt74 = dfa74.predict(input);
            switch (alt74) {
                case 1 :
                    // InternalGaml.g:5129:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    {
                    // InternalGaml.g:5129:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    // InternalGaml.g:5130:4: ( ruleFunction )=>this_Function_0= ruleFunction
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
                    // InternalGaml.g:5141:3: this_VariableRef_1= ruleVariableRef
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
    // InternalGaml.g:5153:1: entryRuleFunction returns [EObject current=null] : iv_ruleFunction= ruleFunction EOF ;
    public final EObject entryRuleFunction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunction = null;


        try {
            // InternalGaml.g:5153:49: (iv_ruleFunction= ruleFunction EOF )
            // InternalGaml.g:5154:2: iv_ruleFunction= ruleFunction EOF
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
    // InternalGaml.g:5160:1: ruleFunction returns [EObject current=null] : ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) ;
    public final EObject ruleFunction() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_left_1_0 = null;

        EObject lv_type_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5166:2: ( ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) )
            // InternalGaml.g:5167:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            {
            // InternalGaml.g:5167:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            // InternalGaml.g:5168:3: () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')'
            {
            // InternalGaml.g:5168:3: ()
            // InternalGaml.g:5169:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getFunctionAccess().getFunctionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5175:3: ( (lv_left_1_0= ruleActionRef ) )
            // InternalGaml.g:5176:4: (lv_left_1_0= ruleActionRef )
            {
            // InternalGaml.g:5176:4: (lv_left_1_0= ruleActionRef )
            // InternalGaml.g:5177:5: lv_left_1_0= ruleActionRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFunctionAccess().getLeftActionRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_57);
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

            // InternalGaml.g:5194:3: ( (lv_type_2_0= ruleTypeInfo ) )?
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0==118) ) {
                alt75=1;
            }
            switch (alt75) {
                case 1 :
                    // InternalGaml.g:5195:4: (lv_type_2_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:5195:4: (lv_type_2_0= ruleTypeInfo )
                    // InternalGaml.g:5196:5: lv_type_2_0= ruleTypeInfo
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getTypeTypeInfoParserRuleCall_2_0());
                      				
                    }
                    pushFollow(FOLLOW_58);
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

            otherlv_3=(Token)match(input,33,FOLLOW_59); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getFunctionAccess().getLeftParenthesisKeyword_3());
              		
            }
            // InternalGaml.g:5217:3: ( (lv_right_4_0= ruleExpressionList ) )?
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( ((LA76_0>=RULE_ID && LA76_0<=RULE_KEYWORD)||LA76_0==21||LA76_0==33||LA76_0==37||(LA76_0>=42 && LA76_0<=72)||(LA76_0>=82 && LA76_0<=109)||LA76_0==120||(LA76_0>=124 && LA76_0<=126)) ) {
                alt76=1;
            }
            switch (alt76) {
                case 1 :
                    // InternalGaml.g:5218:4: (lv_right_4_0= ruleExpressionList )
                    {
                    // InternalGaml.g:5218:4: (lv_right_4_0= ruleExpressionList )
                    // InternalGaml.g:5219:5: lv_right_4_0= ruleExpressionList
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getRightExpressionListParserRuleCall_4_0());
                      				
                    }
                    pushFollow(FOLLOW_28);
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

            otherlv_5=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5244:1: entryRuleExpressionList returns [EObject current=null] : iv_ruleExpressionList= ruleExpressionList EOF ;
    public final EObject entryRuleExpressionList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpressionList = null;


        try {
            // InternalGaml.g:5244:55: (iv_ruleExpressionList= ruleExpressionList EOF )
            // InternalGaml.g:5245:2: iv_ruleExpressionList= ruleExpressionList EOF
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
    // InternalGaml.g:5251:1: ruleExpressionList returns [EObject current=null] : ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) ;
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
            // InternalGaml.g:5257:2: ( ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) )
            // InternalGaml.g:5258:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            {
            // InternalGaml.g:5258:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            int alt79=2;
            alt79 = dfa79.predict(input);
            switch (alt79) {
                case 1 :
                    // InternalGaml.g:5259:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    {
                    // InternalGaml.g:5259:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    // InternalGaml.g:5260:4: ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    {
                    // InternalGaml.g:5260:4: ( (lv_exprs_0_0= ruleExpression ) )
                    // InternalGaml.g:5261:5: (lv_exprs_0_0= ruleExpression )
                    {
                    // InternalGaml.g:5261:5: (lv_exprs_0_0= ruleExpression )
                    // InternalGaml.g:5262:6: lv_exprs_0_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_35);
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

                    // InternalGaml.g:5279:4: (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    loop77:
                    do {
                        int alt77=2;
                        int LA77_0 = input.LA(1);

                        if ( (LA77_0==80) ) {
                            alt77=1;
                        }


                        switch (alt77) {
                    	case 1 :
                    	    // InternalGaml.g:5280:5: otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) )
                    	    {
                    	    otherlv_1=(Token)match(input,80,FOLLOW_5); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_1, grammarAccess.getExpressionListAccess().getCommaKeyword_0_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:5284:5: ( (lv_exprs_2_0= ruleExpression ) )
                    	    // InternalGaml.g:5285:6: (lv_exprs_2_0= ruleExpression )
                    	    {
                    	    // InternalGaml.g:5285:6: (lv_exprs_2_0= ruleExpression )
                    	    // InternalGaml.g:5286:7: lv_exprs_2_0= ruleExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_35);
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
                    	    break loop77;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:5306:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    {
                    // InternalGaml.g:5306:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    // InternalGaml.g:5307:4: ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    {
                    // InternalGaml.g:5307:4: ( (lv_exprs_3_0= ruleParameter ) )
                    // InternalGaml.g:5308:5: (lv_exprs_3_0= ruleParameter )
                    {
                    // InternalGaml.g:5308:5: (lv_exprs_3_0= ruleParameter )
                    // InternalGaml.g:5309:6: lv_exprs_3_0= ruleParameter
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_35);
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

                    // InternalGaml.g:5326:4: (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    loop78:
                    do {
                        int alt78=2;
                        int LA78_0 = input.LA(1);

                        if ( (LA78_0==80) ) {
                            alt78=1;
                        }


                        switch (alt78) {
                    	case 1 :
                    	    // InternalGaml.g:5327:5: otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) )
                    	    {
                    	    otherlv_4=(Token)match(input,80,FOLLOW_54); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_4, grammarAccess.getExpressionListAccess().getCommaKeyword_1_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:5331:5: ( (lv_exprs_5_0= ruleParameter ) )
                    	    // InternalGaml.g:5332:6: (lv_exprs_5_0= ruleParameter )
                    	    {
                    	    // InternalGaml.g:5332:6: (lv_exprs_5_0= ruleParameter )
                    	    // InternalGaml.g:5333:7: lv_exprs_5_0= ruleParameter
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_35);
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
                    	    break loop78;
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
    // InternalGaml.g:5356:1: entryRuleParameter returns [EObject current=null] : iv_ruleParameter= ruleParameter EOF ;
    public final EObject entryRuleParameter() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameter = null;


        try {
            // InternalGaml.g:5356:50: (iv_ruleParameter= ruleParameter EOF )
            // InternalGaml.g:5357:2: iv_ruleParameter= ruleParameter EOF
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
    // InternalGaml.g:5363:1: ruleParameter returns [EObject current=null] : ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) ;
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
            // InternalGaml.g:5369:2: ( ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) )
            // InternalGaml.g:5370:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            {
            // InternalGaml.g:5370:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            // InternalGaml.g:5371:3: () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) )
            {
            // InternalGaml.g:5371:3: ()
            // InternalGaml.g:5372:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getParameterAccess().getParameterAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5378:3: ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) )
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( ((LA81_0>=82 && LA81_0<=109)) ) {
                alt81=1;
            }
            else if ( (LA81_0==RULE_ID||(LA81_0>=42 && LA81_0<=72)) ) {
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
                    // InternalGaml.g:5379:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    {
                    // InternalGaml.g:5379:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) ) )
                    // InternalGaml.g:5380:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    {
                    // InternalGaml.g:5380:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey ) )
                    // InternalGaml.g:5381:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    {
                    // InternalGaml.g:5381:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleTypeFacetKey | lv_builtInFacetKey_1_3= ruleSpecialFacetKey | lv_builtInFacetKey_1_4= ruleActionFacetKey | lv_builtInFacetKey_1_5= ruleVarFacetKey )
                    int alt80=5;
                    switch ( input.LA(1) ) {
                    case 82:
                    case 83:
                        {
                        alt80=1;
                        }
                        break;
                    case 84:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                        {
                        alt80=2;
                        }
                        break;
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
                    case 102:
                    case 103:
                    case 104:
                    case 105:
                    case 106:
                        {
                        alt80=3;
                        }
                        break;
                    case 107:
                    case 108:
                        {
                        alt80=4;
                        }
                        break;
                    case 109:
                        {
                        alt80=5;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 80, 0, input);

                        throw nvae;
                    }

                    switch (alt80) {
                        case 1 :
                            // InternalGaml.g:5382:7: lv_builtInFacetKey_1_1= ruleDefinitionFacetKey
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
                            // InternalGaml.g:5398:7: lv_builtInFacetKey_1_2= ruleTypeFacetKey
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
                            // InternalGaml.g:5414:7: lv_builtInFacetKey_1_3= ruleSpecialFacetKey
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
                            // InternalGaml.g:5430:7: lv_builtInFacetKey_1_4= ruleActionFacetKey
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
                            // InternalGaml.g:5446:7: lv_builtInFacetKey_1_5= ruleVarFacetKey
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
                    // InternalGaml.g:5465:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    {
                    // InternalGaml.g:5465:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    // InternalGaml.g:5466:5: ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':'
                    {
                    // InternalGaml.g:5466:5: ( (lv_left_2_0= ruleVariableRef ) )
                    // InternalGaml.g:5467:6: (lv_left_2_0= ruleVariableRef )
                    {
                    // InternalGaml.g:5467:6: (lv_left_2_0= ruleVariableRef )
                    // InternalGaml.g:5468:7: lv_left_2_0= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getParameterAccess().getLeftVariableRefParserRuleCall_1_1_0_0());
                      						
                    }
                    pushFollow(FOLLOW_37);
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

                    otherlv_3=(Token)match(input,81,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getParameterAccess().getColonKeyword_1_1_1());
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:5491:3: ( (lv_right_4_0= ruleExpression ) )
            // InternalGaml.g:5492:4: (lv_right_4_0= ruleExpression )
            {
            // InternalGaml.g:5492:4: (lv_right_4_0= ruleExpression )
            // InternalGaml.g:5493:5: lv_right_4_0= ruleExpression
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
    // InternalGaml.g:5514:1: entryRuleUnitRef returns [EObject current=null] : iv_ruleUnitRef= ruleUnitRef EOF ;
    public final EObject entryRuleUnitRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitRef = null;


        try {
            // InternalGaml.g:5514:48: (iv_ruleUnitRef= ruleUnitRef EOF )
            // InternalGaml.g:5515:2: iv_ruleUnitRef= ruleUnitRef EOF
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
    // InternalGaml.g:5521:1: ruleUnitRef returns [EObject current=null] : ( () ( (otherlv_1= RULE_ID ) ) ) ;
    public final EObject ruleUnitRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;


        	enterRule();

        try {
            // InternalGaml.g:5527:2: ( ( () ( (otherlv_1= RULE_ID ) ) ) )
            // InternalGaml.g:5528:2: ( () ( (otherlv_1= RULE_ID ) ) )
            {
            // InternalGaml.g:5528:2: ( () ( (otherlv_1= RULE_ID ) ) )
            // InternalGaml.g:5529:3: () ( (otherlv_1= RULE_ID ) )
            {
            // InternalGaml.g:5529:3: ()
            // InternalGaml.g:5530:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getUnitRefAccess().getUnitNameAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5536:3: ( (otherlv_1= RULE_ID ) )
            // InternalGaml.g:5537:4: (otherlv_1= RULE_ID )
            {
            // InternalGaml.g:5537:4: (otherlv_1= RULE_ID )
            // InternalGaml.g:5538:5: otherlv_1= RULE_ID
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
    // InternalGaml.g:5553:1: entryRuleVariableRef returns [EObject current=null] : iv_ruleVariableRef= ruleVariableRef EOF ;
    public final EObject entryRuleVariableRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVariableRef = null;


        try {
            // InternalGaml.g:5553:52: (iv_ruleVariableRef= ruleVariableRef EOF )
            // InternalGaml.g:5554:2: iv_ruleVariableRef= ruleVariableRef EOF
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
    // InternalGaml.g:5560:1: ruleVariableRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleVariableRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5566:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5567:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5567:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5568:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5568:3: ()
            // InternalGaml.g:5569:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getVariableRefAccess().getVariableRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5575:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5576:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5576:4: ( ruleValid_ID )
            // InternalGaml.g:5577:5: ruleValid_ID
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
    // InternalGaml.g:5595:1: entryRuleTypeRef returns [EObject current=null] : iv_ruleTypeRef= ruleTypeRef EOF ;
    public final EObject entryRuleTypeRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeRef = null;


        try {
            // InternalGaml.g:5595:48: (iv_ruleTypeRef= ruleTypeRef EOF )
            // InternalGaml.g:5596:2: iv_ruleTypeRef= ruleTypeRef EOF
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
    // InternalGaml.g:5602:1: ruleTypeRef returns [EObject current=null] : ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) ;
    public final EObject ruleTypeRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_parameter_2_0 = null;

        EObject lv_parameter_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5608:2: ( ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) )
            // InternalGaml.g:5609:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            {
            // InternalGaml.g:5609:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0==RULE_ID) ) {
                alt83=1;
            }
            else if ( (LA83_0==42) ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // InternalGaml.g:5610:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    {
                    // InternalGaml.g:5610:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    // InternalGaml.g:5611:4: () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    {
                    // InternalGaml.g:5611:4: ()
                    // InternalGaml.g:5612:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5618:4: ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    // InternalGaml.g:5619:5: ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    {
                    // InternalGaml.g:5619:5: ( (otherlv_1= RULE_ID ) )
                    // InternalGaml.g:5620:6: (otherlv_1= RULE_ID )
                    {
                    // InternalGaml.g:5620:6: (otherlv_1= RULE_ID )
                    // InternalGaml.g:5621:7: otherlv_1= RULE_ID
                    {
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElement(grammarAccess.getTypeRefRule());
                      							}
                      						
                    }
                    otherlv_1=(Token)match(input,RULE_ID,FOLLOW_60); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							newLeafNode(otherlv_1, grammarAccess.getTypeRefAccess().getRefTypeDefinitionCrossReference_0_1_0_0());
                      						
                    }

                    }


                    }

                    // InternalGaml.g:5632:5: ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    int alt82=2;
                    int LA82_0 = input.LA(1);

                    if ( (LA82_0==118) ) {
                        alt82=1;
                    }
                    switch (alt82) {
                        case 1 :
                            // InternalGaml.g:5633:6: (lv_parameter_2_0= ruleTypeInfo )
                            {
                            // InternalGaml.g:5633:6: (lv_parameter_2_0= ruleTypeInfo )
                            // InternalGaml.g:5634:7: lv_parameter_2_0= ruleTypeInfo
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
                    // InternalGaml.g:5654:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    {
                    // InternalGaml.g:5654:3: ( () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    // InternalGaml.g:5655:4: () (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    {
                    // InternalGaml.g:5655:4: ()
                    // InternalGaml.g:5656:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5662:4: (otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    // InternalGaml.g:5663:5: otherlv_4= 'species' ( (lv_parameter_5_0= ruleTypeInfo ) )
                    {
                    otherlv_4=(Token)match(input,42,FOLLOW_61); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getTypeRefAccess().getSpeciesKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:5667:5: ( (lv_parameter_5_0= ruleTypeInfo ) )
                    // InternalGaml.g:5668:6: (lv_parameter_5_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:5668:6: (lv_parameter_5_0= ruleTypeInfo )
                    // InternalGaml.g:5669:7: lv_parameter_5_0= ruleTypeInfo
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
    // InternalGaml.g:5692:1: entryRuleTypeInfo returns [EObject current=null] : iv_ruleTypeInfo= ruleTypeInfo EOF ;
    public final EObject entryRuleTypeInfo() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeInfo = null;


        try {
            // InternalGaml.g:5692:49: (iv_ruleTypeInfo= ruleTypeInfo EOF )
            // InternalGaml.g:5693:2: iv_ruleTypeInfo= ruleTypeInfo EOF
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
    // InternalGaml.g:5699:1: ruleTypeInfo returns [EObject current=null] : (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) ;
    public final EObject ruleTypeInfo() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_first_1_0 = null;

        EObject lv_second_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5705:2: ( (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) )
            // InternalGaml.g:5706:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            {
            // InternalGaml.g:5706:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            // InternalGaml.g:5707:3: otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' )
            {
            otherlv_0=(Token)match(input,118,FOLLOW_27); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeInfoAccess().getLessThanSignKeyword_0());
              		
            }
            // InternalGaml.g:5711:3: ( (lv_first_1_0= ruleTypeRef ) )
            // InternalGaml.g:5712:4: (lv_first_1_0= ruleTypeRef )
            {
            // InternalGaml.g:5712:4: (lv_first_1_0= ruleTypeRef )
            // InternalGaml.g:5713:5: lv_first_1_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getTypeInfoAccess().getFirstTypeRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_62);
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

            // InternalGaml.g:5730:3: (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )?
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==80) ) {
                alt84=1;
            }
            switch (alt84) {
                case 1 :
                    // InternalGaml.g:5731:4: otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) )
                    {
                    otherlv_2=(Token)match(input,80,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getTypeInfoAccess().getCommaKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:5735:4: ( (lv_second_3_0= ruleTypeRef ) )
                    // InternalGaml.g:5736:5: (lv_second_3_0= ruleTypeRef )
                    {
                    // InternalGaml.g:5736:5: (lv_second_3_0= ruleTypeRef )
                    // InternalGaml.g:5737:6: lv_second_3_0= ruleTypeRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getTypeInfoAccess().getSecondTypeRefParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_33);
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

            // InternalGaml.g:5755:3: ( ( '>' )=>otherlv_4= '>' )
            // InternalGaml.g:5756:4: ( '>' )=>otherlv_4= '>'
            {
            otherlv_4=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5766:1: entryRuleActionRef returns [EObject current=null] : iv_ruleActionRef= ruleActionRef EOF ;
    public final EObject entryRuleActionRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionRef = null;


        try {
            // InternalGaml.g:5766:50: (iv_ruleActionRef= ruleActionRef EOF )
            // InternalGaml.g:5767:2: iv_ruleActionRef= ruleActionRef EOF
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
    // InternalGaml.g:5773:1: ruleActionRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleActionRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5779:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5780:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5780:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5781:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5781:3: ()
            // InternalGaml.g:5782:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getActionRefAccess().getActionRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5788:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5789:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5789:4: ( ruleValid_ID )
            // InternalGaml.g:5790:5: ruleValid_ID
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
    // InternalGaml.g:5808:1: entryRuleEquationRef returns [EObject current=null] : iv_ruleEquationRef= ruleEquationRef EOF ;
    public final EObject entryRuleEquationRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationRef = null;


        try {
            // InternalGaml.g:5808:52: (iv_ruleEquationRef= ruleEquationRef EOF )
            // InternalGaml.g:5809:2: iv_ruleEquationRef= ruleEquationRef EOF
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
    // InternalGaml.g:5815:1: ruleEquationRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleEquationRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5821:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5822:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5822:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5823:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5823:3: ()
            // InternalGaml.g:5824:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getEquationRefAccess().getEquationRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5830:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5831:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5831:4: ( ruleValid_ID )
            // InternalGaml.g:5832:5: ruleValid_ID
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
    // InternalGaml.g:5850:1: entryRuleEquationDefinition returns [EObject current=null] : iv_ruleEquationDefinition= ruleEquationDefinition EOF ;
    public final EObject entryRuleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationDefinition = null;


        try {
            // InternalGaml.g:5850:59: (iv_ruleEquationDefinition= ruleEquationDefinition EOF )
            // InternalGaml.g:5851:2: iv_ruleEquationDefinition= ruleEquationDefinition EOF
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
    // InternalGaml.g:5857:1: ruleEquationDefinition returns [EObject current=null] : (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) ;
    public final EObject ruleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Equations_0 = null;

        EObject this_EquationFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5863:2: ( (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) )
            // InternalGaml.g:5864:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            {
            // InternalGaml.g:5864:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            int alt85=2;
            int LA85_0 = input.LA(1);

            if ( (LA85_0==36) ) {
                alt85=1;
            }
            else if ( (LA85_0==133) ) {
                alt85=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }
            switch (alt85) {
                case 1 :
                    // InternalGaml.g:5865:3: this_S_Equations_0= ruleS_Equations
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
                    // InternalGaml.g:5874:3: this_EquationFakeDefinition_1= ruleEquationFakeDefinition
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
    // InternalGaml.g:5886:1: entryRuleTypeDefinition returns [EObject current=null] : iv_ruleTypeDefinition= ruleTypeDefinition EOF ;
    public final EObject entryRuleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDefinition = null;


        try {
            // InternalGaml.g:5886:55: (iv_ruleTypeDefinition= ruleTypeDefinition EOF )
            // InternalGaml.g:5887:2: iv_ruleTypeDefinition= ruleTypeDefinition EOF
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
    // InternalGaml.g:5893:1: ruleTypeDefinition returns [EObject current=null] : (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) ;
    public final EObject ruleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Species_0 = null;

        EObject this_TypeFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5899:2: ( (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) )
            // InternalGaml.g:5900:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            {
            // InternalGaml.g:5900:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            int alt86=2;
            int LA86_0 = input.LA(1);

            if ( ((LA86_0>=42 && LA86_0<=43)) ) {
                alt86=1;
            }
            else if ( (LA86_0==129) ) {
                alt86=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }
            switch (alt86) {
                case 1 :
                    // InternalGaml.g:5901:3: this_S_Species_0= ruleS_Species
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
                    // InternalGaml.g:5910:3: this_TypeFakeDefinition_1= ruleTypeFakeDefinition
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
    // InternalGaml.g:5922:1: entryRuleVarDefinition returns [EObject current=null] : iv_ruleVarDefinition= ruleVarDefinition EOF ;
    public final EObject entryRuleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDefinition = null;


        try {
            // InternalGaml.g:5922:54: (iv_ruleVarDefinition= ruleVarDefinition EOF )
            // InternalGaml.g:5923:2: iv_ruleVarDefinition= ruleVarDefinition EOF
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
    // InternalGaml.g:5929:1: ruleVarDefinition returns [EObject current=null] : ( ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment ) ;
    public final EObject ruleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Definition_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Reflex_2 = null;

        EObject this_S_Action_3 = null;

        EObject this_S_Loop_4 = null;

        EObject this_Model_5 = null;

        EObject this_ArgumentDefinition_6 = null;

        EObject this_DefinitionFacet_7 = null;

        EObject this_VarFakeDefinition_8 = null;

        EObject this_Import_9 = null;

        EObject this_S_Experiment_10 = null;



        	enterRule();

        try {
            // InternalGaml.g:5935:2: ( ( ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment ) )
            // InternalGaml.g:5936:2: ( ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )
            {
            // InternalGaml.g:5936:2: ( ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )
            int alt88=7;
            alt88 = dfa88.predict(input);
            switch (alt88) {
                case 1 :
                    // InternalGaml.g:5937:3: ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) )
                    {
                    // InternalGaml.g:5937:3: ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) )
                    // InternalGaml.g:5938:4: ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop )
                    {
                    // InternalGaml.g:5954:4: ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop )
                    int alt87=5;
                    int LA87_0 = input.LA(1);

                    if ( (LA87_0==RULE_ID) && (synpred17_InternalGaml())) {
                        alt87=1;
                    }
                    else if ( (LA87_0==42) ) {
                        int LA87_2 = input.LA(2);

                        if ( (LA87_2==118) && (synpred17_InternalGaml())) {
                            alt87=1;
                        }
                        else if ( (LA87_2==RULE_ID) ) {
                            alt87=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return current;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 87, 2, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA87_0==43) ) {
                        alt87=2;
                    }
                    else if ( ((LA87_0>=72 && LA87_0<=73)) ) {
                        alt87=3;
                    }
                    else if ( (LA87_0==35) ) {
                        alt87=4;
                    }
                    else if ( (LA87_0==27) ) {
                        alt87=5;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 87, 0, input);

                        throw nvae;
                    }
                    switch (alt87) {
                        case 1 :
                            // InternalGaml.g:5955:5: ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                            {
                            // InternalGaml.g:5955:5: ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition )
                            // InternalGaml.g:5956:6: ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition
                            {
                            if ( state.backtracking==0 ) {

                              						newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_DefinitionParserRuleCall_0_0_0());
                              					
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
                            // InternalGaml.g:5967:5: this_S_Species_1= ruleS_Species
                            {
                            if ( state.backtracking==0 ) {

                              					newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_SpeciesParserRuleCall_0_0_1());
                              				
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
                            // InternalGaml.g:5976:5: this_S_Reflex_2= ruleS_Reflex
                            {
                            if ( state.backtracking==0 ) {

                              					newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ReflexParserRuleCall_0_0_2());
                              				
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
                            // InternalGaml.g:5985:5: this_S_Action_3= ruleS_Action
                            {
                            if ( state.backtracking==0 ) {

                              					newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ActionParserRuleCall_0_0_3());
                              				
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
                            // InternalGaml.g:5994:5: this_S_Loop_4= ruleS_Loop
                            {
                            if ( state.backtracking==0 ) {

                              					newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_LoopParserRuleCall_0_0_4());
                              				
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


                    }
                    break;
                case 2 :
                    // InternalGaml.g:6005:3: this_Model_5= ruleModel
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getModelParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_Model_5=ruleModel();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_Model_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:6014:3: this_ArgumentDefinition_6= ruleArgumentDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getArgumentDefinitionParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ArgumentDefinition_6=ruleArgumentDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_ArgumentDefinition_6;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:6023:3: this_DefinitionFacet_7= ruleDefinitionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getDefinitionFacetParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_DefinitionFacet_7=ruleDefinitionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_DefinitionFacet_7;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:6032:3: this_VarFakeDefinition_8= ruleVarFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getVarFakeDefinitionParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_VarFakeDefinition_8=ruleVarFakeDefinition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_VarFakeDefinition_8;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:6041:3: this_Import_9= ruleImport
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getImportParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_Import_9=ruleImport();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_Import_9;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:6050:3: this_S_Experiment_10= ruleS_Experiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ExperimentParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Experiment_10=ruleS_Experiment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Experiment_10;
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
    // InternalGaml.g:6062:1: entryRuleActionDefinition returns [EObject current=null] : iv_ruleActionDefinition= ruleActionDefinition EOF ;
    public final EObject entryRuleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionDefinition = null;


        try {
            // InternalGaml.g:6062:57: (iv_ruleActionDefinition= ruleActionDefinition EOF )
            // InternalGaml.g:6063:2: iv_ruleActionDefinition= ruleActionDefinition EOF
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
    // InternalGaml.g:6069:1: ruleActionDefinition returns [EObject current=null] : (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) ;
    public final EObject ruleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Action_0 = null;

        EObject this_ActionFakeDefinition_1 = null;

        EObject this_S_Definition_2 = null;

        EObject this_TypeDefinition_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:6075:2: ( (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) )
            // InternalGaml.g:6076:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            {
            // InternalGaml.g:6076:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            int alt89=4;
            switch ( input.LA(1) ) {
            case 35:
                {
                alt89=1;
                }
                break;
            case 130:
                {
                alt89=2;
                }
                break;
            case RULE_ID:
                {
                alt89=3;
                }
                break;
            case 42:
                {
                int LA89_4 = input.LA(2);

                if ( (LA89_4==RULE_ID) ) {
                    alt89=4;
                }
                else if ( (LA89_4==118) ) {
                    alt89=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 89, 4, input);

                    throw nvae;
                }
                }
                break;
            case 43:
            case 129:
                {
                alt89=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }

            switch (alt89) {
                case 1 :
                    // InternalGaml.g:6077:3: this_S_Action_0= ruleS_Action
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
                    // InternalGaml.g:6086:3: this_ActionFakeDefinition_1= ruleActionFakeDefinition
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
                    // InternalGaml.g:6095:3: this_S_Definition_2= ruleS_Definition
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
                    // InternalGaml.g:6104:3: this_TypeDefinition_3= ruleTypeDefinition
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
    // InternalGaml.g:6116:1: entryRuleValid_ID returns [String current=null] : iv_ruleValid_ID= ruleValid_ID EOF ;
    public final String entryRuleValid_ID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValid_ID = null;


        try {
            // InternalGaml.g:6116:48: (iv_ruleValid_ID= ruleValid_ID EOF )
            // InternalGaml.g:6117:2: iv_ruleValid_ID= ruleValid_ID EOF
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
    // InternalGaml.g:6123:1: ruleValid_ID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this__SpeciesKey_0= rule_SpeciesKey | this_K_Init_1= ruleK_Init | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID ) ;
    public final AntlrDatatypeRuleToken ruleValid_ID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_4=null;
        AntlrDatatypeRuleToken this__SpeciesKey_0 = null;

        AntlrDatatypeRuleToken this_K_Init_1 = null;

        AntlrDatatypeRuleToken this__GeneralKey_2 = null;

        AntlrDatatypeRuleToken this__ExperimentKey_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:6129:2: ( (this__SpeciesKey_0= rule_SpeciesKey | this_K_Init_1= ruleK_Init | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID ) )
            // InternalGaml.g:6130:2: (this__SpeciesKey_0= rule_SpeciesKey | this_K_Init_1= ruleK_Init | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID )
            {
            // InternalGaml.g:6130:2: (this__SpeciesKey_0= rule_SpeciesKey | this_K_Init_1= ruleK_Init | this__GeneralKey_2= rule_GeneralKey | this__ExperimentKey_3= rule_ExperimentKey | this_ID_4= RULE_ID )
            int alt90=5;
            switch ( input.LA(1) ) {
            case 42:
            case 43:
                {
                alt90=1;
                }
                break;
            case 72:
                {
                alt90=2;
                }
                break;
            case 45:
            case 46:
            case 47:
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
                {
                alt90=3;
                }
                break;
            case 44:
                {
                alt90=4;
                }
                break;
            case RULE_ID:
                {
                alt90=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                throw nvae;
            }

            switch (alt90) {
                case 1 :
                    // InternalGaml.g:6131:3: this__SpeciesKey_0= rule_SpeciesKey
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
                    // InternalGaml.g:6142:3: this_K_Init_1= ruleK_Init
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_InitParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Init_1=ruleK_Init();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Init_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:6153:3: this__GeneralKey_2= rule_GeneralKey
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
                    // InternalGaml.g:6164:3: this__ExperimentKey_3= rule_ExperimentKey
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
                    // InternalGaml.g:6175:3: this_ID_4= RULE_ID
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
    // InternalGaml.g:6186:1: entryRuleUnitFakeDefinition returns [EObject current=null] : iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF ;
    public final EObject entryRuleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitFakeDefinition = null;


        try {
            // InternalGaml.g:6186:59: (iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF )
            // InternalGaml.g:6187:2: iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF
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
    // InternalGaml.g:6193:1: ruleUnitFakeDefinition returns [EObject current=null] : (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6199:2: ( (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6200:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6200:2: (otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6201:3: otherlv_0= '**unit*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,128,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getUnitFakeDefinitionAccess().getUnitKeyword_0());
              		
            }
            // InternalGaml.g:6205:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6206:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6206:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6207:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6227:1: entryRuleTypeFakeDefinition returns [EObject current=null] : iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF ;
    public final EObject entryRuleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFakeDefinition = null;


        try {
            // InternalGaml.g:6227:59: (iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF )
            // InternalGaml.g:6228:2: iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF
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
    // InternalGaml.g:6234:1: ruleTypeFakeDefinition returns [EObject current=null] : (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6240:2: ( (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6241:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6241:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6242:3: otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,129,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeFakeDefinitionAccess().getTypeKeyword_0());
              		
            }
            // InternalGaml.g:6246:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6247:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6247:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6248:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6268:1: entryRuleActionFakeDefinition returns [EObject current=null] : iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF ;
    public final EObject entryRuleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFakeDefinition = null;


        try {
            // InternalGaml.g:6268:61: (iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF )
            // InternalGaml.g:6269:2: iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF
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
    // InternalGaml.g:6275:1: ruleActionFakeDefinition returns [EObject current=null] : (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6281:2: ( (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6282:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6282:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6283:3: otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,130,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getActionFakeDefinitionAccess().getActionKeyword_0());
              		
            }
            // InternalGaml.g:6287:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6288:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6288:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6289:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6310:1: entryRuleSkillFakeDefinition returns [EObject current=null] : iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF ;
    public final EObject entryRuleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSkillFakeDefinition = null;


        try {
            // InternalGaml.g:6310:60: (iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF )
            // InternalGaml.g:6311:2: iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF
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
    // InternalGaml.g:6317:1: ruleSkillFakeDefinition returns [EObject current=null] : (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6323:2: ( (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6324:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6324:2: (otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6325:3: otherlv_0= '**skill*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,131,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSkillFakeDefinitionAccess().getSkillKeyword_0());
              		
            }
            // InternalGaml.g:6329:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6330:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6330:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6331:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6351:1: entryRuleVarFakeDefinition returns [EObject current=null] : iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF ;
    public final EObject entryRuleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFakeDefinition = null;


        try {
            // InternalGaml.g:6351:58: (iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF )
            // InternalGaml.g:6352:2: iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF
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
    // InternalGaml.g:6358:1: ruleVarFakeDefinition returns [EObject current=null] : (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6364:2: ( (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6365:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6365:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6366:3: otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,132,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getVarFakeDefinitionAccess().getVarKeyword_0());
              		
            }
            // InternalGaml.g:6370:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6371:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6371:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6372:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6393:1: entryRuleEquationFakeDefinition returns [EObject current=null] : iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF ;
    public final EObject entryRuleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationFakeDefinition = null;


        try {
            // InternalGaml.g:6393:63: (iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF )
            // InternalGaml.g:6394:2: iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF
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
    // InternalGaml.g:6400:1: ruleEquationFakeDefinition returns [EObject current=null] : (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6406:2: ( (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6407:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6407:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6408:3: otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,133,FOLLOW_10); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getEquationFakeDefinitionAccess().getEquationKeyword_0());
              		
            }
            // InternalGaml.g:6412:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6413:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6413:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6414:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6435:1: entryRuleTerminalExpression returns [EObject current=null] : iv_ruleTerminalExpression= ruleTerminalExpression EOF ;
    public final EObject entryRuleTerminalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTerminalExpression = null;


        try {
            // InternalGaml.g:6435:59: (iv_ruleTerminalExpression= ruleTerminalExpression EOF )
            // InternalGaml.g:6436:2: iv_ruleTerminalExpression= ruleTerminalExpression EOF
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
    // InternalGaml.g:6442:1: ruleTerminalExpression returns [EObject current=null] : (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) ;
    public final EObject ruleTerminalExpression() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token lv_op_4_0=null;
        Token lv_op_6_0=null;
        Token lv_op_8_0=null;
        EObject this_StringLiteral_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6448:2: ( (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) )
            // InternalGaml.g:6449:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            {
            // InternalGaml.g:6449:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            int alt91=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt91=1;
                }
                break;
            case RULE_INTEGER:
                {
                alt91=2;
                }
                break;
            case RULE_DOUBLE:
                {
                alt91=3;
                }
                break;
            case RULE_BOOLEAN:
                {
                alt91=4;
                }
                break;
            case RULE_KEYWORD:
                {
                alt91=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 91, 0, input);

                throw nvae;
            }

            switch (alt91) {
                case 1 :
                    // InternalGaml.g:6450:3: this_StringLiteral_0= ruleStringLiteral
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
                    // InternalGaml.g:6459:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    {
                    // InternalGaml.g:6459:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    // InternalGaml.g:6460:4: () ( (lv_op_2_0= RULE_INTEGER ) )
                    {
                    // InternalGaml.g:6460:4: ()
                    // InternalGaml.g:6461:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getIntLiteralAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6467:4: ( (lv_op_2_0= RULE_INTEGER ) )
                    // InternalGaml.g:6468:5: (lv_op_2_0= RULE_INTEGER )
                    {
                    // InternalGaml.g:6468:5: (lv_op_2_0= RULE_INTEGER )
                    // InternalGaml.g:6469:6: lv_op_2_0= RULE_INTEGER
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
                    // InternalGaml.g:6487:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    {
                    // InternalGaml.g:6487:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    // InternalGaml.g:6488:4: () ( (lv_op_4_0= RULE_DOUBLE ) )
                    {
                    // InternalGaml.g:6488:4: ()
                    // InternalGaml.g:6489:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getDoubleLiteralAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6495:4: ( (lv_op_4_0= RULE_DOUBLE ) )
                    // InternalGaml.g:6496:5: (lv_op_4_0= RULE_DOUBLE )
                    {
                    // InternalGaml.g:6496:5: (lv_op_4_0= RULE_DOUBLE )
                    // InternalGaml.g:6497:6: lv_op_4_0= RULE_DOUBLE
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
                    // InternalGaml.g:6515:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    {
                    // InternalGaml.g:6515:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    // InternalGaml.g:6516:4: () ( (lv_op_6_0= RULE_BOOLEAN ) )
                    {
                    // InternalGaml.g:6516:4: ()
                    // InternalGaml.g:6517:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getBooleanLiteralAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6523:4: ( (lv_op_6_0= RULE_BOOLEAN ) )
                    // InternalGaml.g:6524:5: (lv_op_6_0= RULE_BOOLEAN )
                    {
                    // InternalGaml.g:6524:5: (lv_op_6_0= RULE_BOOLEAN )
                    // InternalGaml.g:6525:6: lv_op_6_0= RULE_BOOLEAN
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
                    // InternalGaml.g:6543:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    {
                    // InternalGaml.g:6543:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    // InternalGaml.g:6544:4: () ( (lv_op_8_0= RULE_KEYWORD ) )
                    {
                    // InternalGaml.g:6544:4: ()
                    // InternalGaml.g:6545:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getReservedLiteralAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6551:4: ( (lv_op_8_0= RULE_KEYWORD ) )
                    // InternalGaml.g:6552:5: (lv_op_8_0= RULE_KEYWORD )
                    {
                    // InternalGaml.g:6552:5: (lv_op_8_0= RULE_KEYWORD )
                    // InternalGaml.g:6553:6: lv_op_8_0= RULE_KEYWORD
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
    // InternalGaml.g:6574:1: entryRuleStringLiteral returns [EObject current=null] : iv_ruleStringLiteral= ruleStringLiteral EOF ;
    public final EObject entryRuleStringLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStringLiteral = null;


        try {
            // InternalGaml.g:6574:54: (iv_ruleStringLiteral= ruleStringLiteral EOF )
            // InternalGaml.g:6575:2: iv_ruleStringLiteral= ruleStringLiteral EOF
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
    // InternalGaml.g:6581:1: ruleStringLiteral returns [EObject current=null] : ( (lv_op_0_0= RULE_STRING ) ) ;
    public final EObject ruleStringLiteral() throws RecognitionException {
        EObject current = null;

        Token lv_op_0_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6587:2: ( ( (lv_op_0_0= RULE_STRING ) ) )
            // InternalGaml.g:6588:2: ( (lv_op_0_0= RULE_STRING ) )
            {
            // InternalGaml.g:6588:2: ( (lv_op_0_0= RULE_STRING ) )
            // InternalGaml.g:6589:3: (lv_op_0_0= RULE_STRING )
            {
            // InternalGaml.g:6589:3: (lv_op_0_0= RULE_STRING )
            // InternalGaml.g:6590:4: lv_op_0_0= RULE_STRING
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
        if ( input.LA(1)==17||input.LA(1)==20 ) {
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
        // InternalGaml.g:947:4: ( ruleS_Equations )
        // InternalGaml.g:947:5: ruleS_Equations
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
        // InternalGaml.g:959:4: ( ruleS_Action )
        // InternalGaml.g:959:5: ruleS_Action
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
        // InternalGaml.g:971:4: ( ruleS_Species )
        // InternalGaml.g:971:5: ruleS_Species
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
        // InternalGaml.g:983:4: ( ruleS_Reflex )
        // InternalGaml.g:983:5: ruleS_Reflex
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
        // InternalGaml.g:995:4: ( ruleS_Assignment )
        // InternalGaml.g:995:5: ruleS_Assignment
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
        // InternalGaml.g:1007:4: ( ruleS_Definition )
        // InternalGaml.g:1007:5: ruleS_Definition
        {
        pushFollow(FOLLOW_2);
        ruleS_Definition();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_InternalGaml

    // $ANTLR start synpred8_InternalGaml
    public final void synpred8_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1264:5: ( 'else' )
        // InternalGaml.g:1264:6: 'else'
        {
        match(input,29,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_InternalGaml

    // $ANTLR start synpred9_InternalGaml
    public final void synpred9_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1362:5: ( 'catch' )
        // InternalGaml.g:1362:6: 'catch'
        {
        match(input,31,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_InternalGaml

    // $ANTLR start synpred10_InternalGaml
    public final void synpred10_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1484:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )
        // InternalGaml.g:1484:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        {
        // InternalGaml.g:1484:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        // InternalGaml.g:1485:6: ( ( ruleExpression ) ) ruleFacetsAndBlock[null]
        {
        // InternalGaml.g:1485:6: ( ( ruleExpression ) )
        // InternalGaml.g:1486:7: ( ruleExpression )
        {
        // InternalGaml.g:1486:7: ( ruleExpression )
        // InternalGaml.g:1487:8: ruleExpression
        {
        pushFollow(FOLLOW_7);
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
    // $ANTLR end synpred10_InternalGaml

    // $ANTLR start synpred12_InternalGaml
    public final void synpred12_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:3365:5: ( ( ( ruleExpression ) ) )
        // InternalGaml.g:3365:6: ( ( ruleExpression ) )
        {
        // InternalGaml.g:3365:6: ( ( ruleExpression ) )
        // InternalGaml.g:3366:6: ( ruleExpression )
        {
        // InternalGaml.g:3366:6: ( ruleExpression )
        // InternalGaml.g:3367:7: ruleExpression
        {
        pushFollow(FOLLOW_2);
        ruleExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }
    }
    // $ANTLR end synpred12_InternalGaml

    // $ANTLR start synpred13_InternalGaml
    public final void synpred13_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:3462:5: ( 'species' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==42 ) {
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
    // $ANTLR end synpred13_InternalGaml

    // $ANTLR start synpred14_InternalGaml
    public final void synpred14_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5130:4: ( ruleFunction )
        // InternalGaml.g:5130:5: ruleFunction
        {
        pushFollow(FOLLOW_2);
        ruleFunction();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_InternalGaml

    // $ANTLR start synpred15_InternalGaml
    public final void synpred15_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5756:4: ( '>' )
        // InternalGaml.g:5756:5: '>'
        {
        match(input,75,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_InternalGaml

    // $ANTLR start synpred16_InternalGaml
    public final void synpred16_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5938:4: ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )
        // InternalGaml.g:5938:5: ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop )
        {
        // InternalGaml.g:5938:5: ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop )
        int alt92=5;
        switch ( input.LA(1) ) {
        case 42:
            {
            int LA92_1 = input.LA(2);

            if ( (LA92_1==RULE_ID) ) {
                alt92=2;
            }
            else if ( (LA92_1==EOF) ) {
                alt92=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 1, input);

                throw nvae;
            }
            }
            break;
        case RULE_ID:
            {
            alt92=1;
            }
            break;
        case 43:
            {
            alt92=2;
            }
            break;
        case 72:
        case 73:
            {
            alt92=3;
            }
            break;
        case 35:
            {
            alt92=4;
            }
            break;
        case 27:
            {
            alt92=5;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 92, 0, input);

            throw nvae;
        }

        switch (alt92) {
            case 1 :
                // InternalGaml.g:5939:5: ( 'species' | RULE_ID )
                {
                if ( input.LA(1)==RULE_ID||input.LA(1)==42 ) {
                    input.consume();
                    state.errorRecovery=false;state.failed=false;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    MismatchedSetException mse = new MismatchedSetException(null,input);
                    throw mse;
                }


                }
                break;
            case 2 :
                // InternalGaml.g:5945:5: ruleS_Species
                {
                pushFollow(FOLLOW_2);
                ruleS_Species();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 3 :
                // InternalGaml.g:5947:5: ruleS_Reflex
                {
                pushFollow(FOLLOW_2);
                ruleS_Reflex();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 4 :
                // InternalGaml.g:5949:5: ruleS_Action
                {
                pushFollow(FOLLOW_2);
                ruleS_Action();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 5 :
                // InternalGaml.g:5951:5: ruleS_Loop
                {
                pushFollow(FOLLOW_2);
                ruleS_Loop();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred16_InternalGaml

    // $ANTLR start synpred17_InternalGaml
    public final void synpred17_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5956:6: ( 'species' | RULE_ID )
        // InternalGaml.g:
        {
        if ( input.LA(1)==RULE_ID||input.LA(1)==42 ) {
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
    // $ANTLR end synpred17_InternalGaml

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


    protected DFA13 dfa13 = new DFA13(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA30 dfa30 = new DFA30(this);
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA74 dfa74 = new DFA74(this);
    protected DFA79 dfa79 = new DFA79(this);
    protected DFA88 dfa88 = new DFA88(this);
    static final String dfa_1s = "\73\uffff";
    static final String dfa_2s = "\1\4\12\uffff\2\0\1\uffff\1\0\5\uffff\35\0\12\uffff";
    static final String dfa_3s = "\1\176\12\uffff\2\0\1\uffff\1\0\5\uffff\35\0\12\uffff";
    static final String dfa_4s = "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\1\11\2\uffff\1\13\1\uffff\5\14\35\uffff\7\14\1\12\1\15\1\16";
    static final String dfa_5s = "\1\0\12\uffff\1\1\1\2\1\uffff\1\3\5\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\12\uffff}>";
    static final String[] dfa_6s = {
            "\1\60\1\17\1\20\1\21\1\22\1\23\13\uffff\1\62\3\uffff\2\6\1\10\1\4\1\uffff\1\5\1\uffff\1\2\1\61\1\uffff\1\12\1\11\1\63\2\uffff\1\3\1\1\1\13\1\14\1\57\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\16\1\15\56\uffff\1\65\3\uffff\1\64\1\66\1\67",
            "",
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
            "\1\uffff",
            "",
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

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = dfa_1;
            this.eof = dfa_1;
            this.min = dfa_2;
            this.max = dfa_3;
            this.accept = dfa_4;
            this.special = dfa_5;
            this.transition = dfa_6;
        }
        public String getDescription() {
            return "882:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | ( ( ruleS_Equations )=>this_S_Equations_7= ruleS_Equations ) | ( ( ruleS_Action )=>this_S_Action_8= ruleS_Action ) | ( ( ruleS_Species )=>this_S_Species_9= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_10= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_11= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_12= ruleS_Definition ) | this_S_Other_13= ruleS_Other )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA13_0 = input.LA(1);

                         
                        int index13_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA13_0==41) ) {s = 1;}

                        else if ( (LA13_0==32) ) {s = 2;}

                        else if ( (LA13_0==40) ) {s = 3;}

                        else if ( (LA13_0==28) ) {s = 4;}

                        else if ( (LA13_0==30) ) {s = 5;}

                        else if ( ((LA13_0>=25 && LA13_0<=26)) ) {s = 6;}

                        else if ( (LA13_0==27) ) {s = 8;}

                        else if ( (LA13_0==36) && (synpred2_InternalGaml())) {s = 9;}

                        else if ( (LA13_0==35) && (synpred3_InternalGaml())) {s = 10;}

                        else if ( (LA13_0==42) ) {s = 11;}

                        else if ( (LA13_0==43) ) {s = 12;}

                        else if ( (LA13_0==73) && (synpred5_InternalGaml())) {s = 13;}

                        else if ( (LA13_0==72) ) {s = 14;}

                        else if ( (LA13_0==RULE_STRING) && (synpred6_InternalGaml())) {s = 15;}

                        else if ( (LA13_0==RULE_INTEGER) && (synpred6_InternalGaml())) {s = 16;}

                        else if ( (LA13_0==RULE_DOUBLE) && (synpred6_InternalGaml())) {s = 17;}

                        else if ( (LA13_0==RULE_BOOLEAN) && (synpred6_InternalGaml())) {s = 18;}

                        else if ( (LA13_0==RULE_KEYWORD) && (synpred6_InternalGaml())) {s = 19;}

                        else if ( (LA13_0==45) ) {s = 20;}

                        else if ( (LA13_0==46) ) {s = 21;}

                        else if ( (LA13_0==47) ) {s = 22;}

                        else if ( (LA13_0==48) ) {s = 23;}

                        else if ( (LA13_0==49) ) {s = 24;}

                        else if ( (LA13_0==50) ) {s = 25;}

                        else if ( (LA13_0==51) ) {s = 26;}

                        else if ( (LA13_0==52) ) {s = 27;}

                        else if ( (LA13_0==53) ) {s = 28;}

                        else if ( (LA13_0==54) ) {s = 29;}

                        else if ( (LA13_0==55) ) {s = 30;}

                        else if ( (LA13_0==56) ) {s = 31;}

                        else if ( (LA13_0==57) ) {s = 32;}

                        else if ( (LA13_0==58) ) {s = 33;}

                        else if ( (LA13_0==59) ) {s = 34;}

                        else if ( (LA13_0==60) ) {s = 35;}

                        else if ( (LA13_0==61) ) {s = 36;}

                        else if ( (LA13_0==62) ) {s = 37;}

                        else if ( (LA13_0==63) ) {s = 38;}

                        else if ( (LA13_0==64) ) {s = 39;}

                        else if ( (LA13_0==65) ) {s = 40;}

                        else if ( (LA13_0==66) ) {s = 41;}

                        else if ( (LA13_0==67) ) {s = 42;}

                        else if ( (LA13_0==68) ) {s = 43;}

                        else if ( (LA13_0==69) ) {s = 44;}

                        else if ( (LA13_0==70) ) {s = 45;}

                        else if ( (LA13_0==71) ) {s = 46;}

                        else if ( (LA13_0==44) ) {s = 47;}

                        else if ( (LA13_0==RULE_ID) ) {s = 48;}

                        else if ( (LA13_0==33) && (synpred6_InternalGaml())) {s = 49;}

                        else if ( (LA13_0==21) && (synpred6_InternalGaml())) {s = 50;}

                        else if ( (LA13_0==37) && (synpred6_InternalGaml())) {s = 51;}

                        else if ( (LA13_0==124) && (synpred6_InternalGaml())) {s = 52;}

                        else if ( (LA13_0==120) && (synpred6_InternalGaml())) {s = 53;}

                        else if ( (LA13_0==125) && (synpred6_InternalGaml())) {s = 54;}

                        else if ( (LA13_0==126) && (synpred6_InternalGaml())) {s = 55;}

                         
                        input.seek(index13_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA13_11 = input.LA(1);

                         
                        int index13_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 56;}

                        else if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (synpred7_InternalGaml()) ) {s = 57;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA13_12 = input.LA(1);

                         
                        int index13_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 56;}

                        else if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_12);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA13_14 = input.LA(1);

                         
                        int index13_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_InternalGaml()) ) {s = 13;}

                        else if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_14);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA13_20 = input.LA(1);

                         
                        int index13_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_20);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA13_21 = input.LA(1);

                         
                        int index13_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_21);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA13_22 = input.LA(1);

                         
                        int index13_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_22);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA13_23 = input.LA(1);

                         
                        int index13_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_23);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA13_24 = input.LA(1);

                         
                        int index13_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_24);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA13_25 = input.LA(1);

                         
                        int index13_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_25);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA13_26 = input.LA(1);

                         
                        int index13_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_26);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA13_27 = input.LA(1);

                         
                        int index13_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_27);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA13_28 = input.LA(1);

                         
                        int index13_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_28);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA13_29 = input.LA(1);

                         
                        int index13_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_29);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA13_30 = input.LA(1);

                         
                        int index13_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_30);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA13_31 = input.LA(1);

                         
                        int index13_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_31);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA13_32 = input.LA(1);

                         
                        int index13_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_32);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA13_33 = input.LA(1);

                         
                        int index13_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_33);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA13_34 = input.LA(1);

                         
                        int index13_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_34);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA13_35 = input.LA(1);

                         
                        int index13_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_35);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA13_36 = input.LA(1);

                         
                        int index13_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_36);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA13_37 = input.LA(1);

                         
                        int index13_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_37);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA13_38 = input.LA(1);

                         
                        int index13_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_38);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA13_39 = input.LA(1);

                         
                        int index13_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_39);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA13_40 = input.LA(1);

                         
                        int index13_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_40);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA13_41 = input.LA(1);

                         
                        int index13_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_41);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA13_42 = input.LA(1);

                         
                        int index13_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_42);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA13_43 = input.LA(1);

                         
                        int index13_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_43);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA13_44 = input.LA(1);

                         
                        int index13_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_44);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA13_45 = input.LA(1);

                         
                        int index13_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_45);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA13_46 = input.LA(1);

                         
                        int index13_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_46);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA13_47 = input.LA(1);

                         
                        int index13_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_47);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA13_48 = input.LA(1);

                         
                        int index13_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_InternalGaml()) ) {s = 55;}

                        else if ( (synpred7_InternalGaml()) ) {s = 57;}

                        else if ( (true) ) {s = 58;}

                         
                        input.seek(index13_48);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 13, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_7s = "\114\uffff";
    static final String dfa_8s = "\1\4\44\uffff\1\0\2\uffff\1\0\43\uffff";
    static final String dfa_9s = "\1\176\44\uffff\1\0\2\uffff\1\0\43\uffff";
    static final String dfa_10s = "\1\uffff\44\1\1\uffff\2\1\1\uffff\4\1\1\2\36\uffff";
    static final String dfa_11s = "\1\0\44\uffff\1\1\2\uffff\1\2\43\uffff}>";
    static final String[] dfa_12s = {
            "\1\45\1\1\1\2\1\3\1\4\1\5\5\uffff\1\55\5\uffff\1\47\1\uffff\1\55\11\uffff\1\46\3\uffff\1\50\4\uffff\1\6\1\7\1\44\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\10\11\uffff\35\55\11\uffff\1\52\3\uffff\1\51\1\53\1\54",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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

    static final short[] dfa_7 = DFA.unpackEncodedString(dfa_7s);
    static final char[] dfa_8 = DFA.unpackEncodedStringToUnsignedChars(dfa_8s);
    static final char[] dfa_9 = DFA.unpackEncodedStringToUnsignedChars(dfa_9s);
    static final short[] dfa_10 = DFA.unpackEncodedString(dfa_10s);
    static final short[] dfa_11 = DFA.unpackEncodedString(dfa_11s);
    static final short[][] dfa_12 = unpackEncodedStringArray(dfa_12s);

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_8;
            this.max = dfa_9;
            this.accept = dfa_10;
            this.special = dfa_11;
            this.transition = dfa_12;
        }
        public String getDescription() {
            return "1482:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA21_0 = input.LA(1);

                         
                        int index21_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_0==RULE_STRING) && (synpred10_InternalGaml())) {s = 1;}

                        else if ( (LA21_0==RULE_INTEGER) && (synpred10_InternalGaml())) {s = 2;}

                        else if ( (LA21_0==RULE_DOUBLE) && (synpred10_InternalGaml())) {s = 3;}

                        else if ( (LA21_0==RULE_BOOLEAN) && (synpred10_InternalGaml())) {s = 4;}

                        else if ( (LA21_0==RULE_KEYWORD) && (synpred10_InternalGaml())) {s = 5;}

                        else if ( (LA21_0==42) && (synpred10_InternalGaml())) {s = 6;}

                        else if ( (LA21_0==43) && (synpred10_InternalGaml())) {s = 7;}

                        else if ( (LA21_0==72) && (synpred10_InternalGaml())) {s = 8;}

                        else if ( (LA21_0==45) && (synpred10_InternalGaml())) {s = 9;}

                        else if ( (LA21_0==46) && (synpred10_InternalGaml())) {s = 10;}

                        else if ( (LA21_0==47) && (synpred10_InternalGaml())) {s = 11;}

                        else if ( (LA21_0==48) && (synpred10_InternalGaml())) {s = 12;}

                        else if ( (LA21_0==49) && (synpred10_InternalGaml())) {s = 13;}

                        else if ( (LA21_0==50) && (synpred10_InternalGaml())) {s = 14;}

                        else if ( (LA21_0==51) && (synpred10_InternalGaml())) {s = 15;}

                        else if ( (LA21_0==52) && (synpred10_InternalGaml())) {s = 16;}

                        else if ( (LA21_0==53) && (synpred10_InternalGaml())) {s = 17;}

                        else if ( (LA21_0==54) && (synpred10_InternalGaml())) {s = 18;}

                        else if ( (LA21_0==55) && (synpred10_InternalGaml())) {s = 19;}

                        else if ( (LA21_0==56) && (synpred10_InternalGaml())) {s = 20;}

                        else if ( (LA21_0==57) && (synpred10_InternalGaml())) {s = 21;}

                        else if ( (LA21_0==58) && (synpred10_InternalGaml())) {s = 22;}

                        else if ( (LA21_0==59) && (synpred10_InternalGaml())) {s = 23;}

                        else if ( (LA21_0==60) && (synpred10_InternalGaml())) {s = 24;}

                        else if ( (LA21_0==61) && (synpred10_InternalGaml())) {s = 25;}

                        else if ( (LA21_0==62) && (synpred10_InternalGaml())) {s = 26;}

                        else if ( (LA21_0==63) && (synpred10_InternalGaml())) {s = 27;}

                        else if ( (LA21_0==64) && (synpred10_InternalGaml())) {s = 28;}

                        else if ( (LA21_0==65) && (synpred10_InternalGaml())) {s = 29;}

                        else if ( (LA21_0==66) && (synpred10_InternalGaml())) {s = 30;}

                        else if ( (LA21_0==67) && (synpred10_InternalGaml())) {s = 31;}

                        else if ( (LA21_0==68) && (synpred10_InternalGaml())) {s = 32;}

                        else if ( (LA21_0==69) && (synpred10_InternalGaml())) {s = 33;}

                        else if ( (LA21_0==70) && (synpred10_InternalGaml())) {s = 34;}

                        else if ( (LA21_0==71) && (synpred10_InternalGaml())) {s = 35;}

                        else if ( (LA21_0==44) && (synpred10_InternalGaml())) {s = 36;}

                        else if ( (LA21_0==RULE_ID) ) {s = 37;}

                        else if ( (LA21_0==33) && (synpred10_InternalGaml())) {s = 38;}

                        else if ( (LA21_0==21) && (synpred10_InternalGaml())) {s = 39;}

                        else if ( (LA21_0==37) ) {s = 40;}

                        else if ( (LA21_0==124) && (synpred10_InternalGaml())) {s = 41;}

                        else if ( (LA21_0==120) && (synpred10_InternalGaml())) {s = 42;}

                        else if ( (LA21_0==125) && (synpred10_InternalGaml())) {s = 43;}

                        else if ( (LA21_0==126) && (synpred10_InternalGaml())) {s = 44;}

                        else if ( (LA21_0==15||LA21_0==23||(LA21_0>=82 && LA21_0<=110)) ) {s = 45;}

                         
                        input.seek(index21_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA21_37 = input.LA(1);

                         
                        int index21_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 44;}

                        else if ( (true) ) {s = 45;}

                         
                        input.seek(index21_37);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA21_40 = input.LA(1);

                         
                        int index21_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 44;}

                        else if ( (true) ) {s = 45;}

                         
                        input.seek(index21_40);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 21, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_13s = "\43\uffff";
    static final String dfa_14s = "\1\4\40\41\2\uffff";
    static final String dfa_15s = "\1\110\40\166\2\uffff";
    static final String dfa_16s = "\41\uffff\1\2\1\1";
    static final String dfa_17s = "\43\uffff}>";
    static final String[] dfa_18s = {
            "\1\40\45\uffff\1\1\1\2\1\37\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\3",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "\1\42\5\uffff\1\41\116\uffff\1\42",
            "",
            ""
    };

    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final char[] dfa_14 = DFA.unpackEncodedStringToUnsignedChars(dfa_14s);
    static final char[] dfa_15 = DFA.unpackEncodedStringToUnsignedChars(dfa_15s);
    static final short[] dfa_16 = DFA.unpackEncodedString(dfa_16s);
    static final short[] dfa_17 = DFA.unpackEncodedString(dfa_17s);
    static final short[][] dfa_18 = unpackEncodedStringArray(dfa_18s);

    class DFA30 extends DFA {

        public DFA30(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 30;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_14;
            this.max = dfa_15;
            this.accept = dfa_16;
            this.special = dfa_17;
            this.transition = dfa_18;
        }
        public String getDescription() {
            return "2056:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )";
        }
    }
    static final String dfa_19s = "\12\uffff";
    static final String dfa_20s = "\1\17\2\uffff\1\113\6\uffff";
    static final String dfa_21s = "\1\117\2\uffff\1\115\6\uffff";
    static final String dfa_22s = "\1\uffff\1\1\1\2\1\uffff\1\4\1\6\1\7\1\10\1\5\1\3";
    static final String dfa_23s = "\12\uffff}>";
    static final String[] dfa_24s = {
            "\1\1\72\uffff\1\2\1\3\1\4\1\7\1\5\1\6",
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

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = dfa_19;
            this.eof = dfa_19;
            this.min = dfa_20;
            this.max = dfa_21;
            this.accept = dfa_22;
            this.special = dfa_23;
            this.transition = dfa_24;
        }
        public String getDescription() {
            return "2588:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )";
        }
    }
    static final String dfa_25s = "\56\uffff";
    static final String dfa_26s = "\1\4\47\uffff\1\0\5\uffff";
    static final String dfa_27s = "\1\176\47\uffff\1\0\5\uffff";
    static final String dfa_28s = "\1\uffff\47\1\1\uffff\4\1\1\2";
    static final String dfa_29s = "\1\0\47\uffff\1\1\5\uffff}>";
    static final String[] dfa_30s = {
            "\1\45\1\1\1\2\1\3\1\4\1\5\13\uffff\1\47\13\uffff\1\46\3\uffff\1\50\4\uffff\1\6\1\7\1\44\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\10\57\uffff\1\52\3\uffff\1\51\1\53\1\54",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = dfa_25;
            this.eof = dfa_25;
            this.min = dfa_26;
            this.max = dfa_27;
            this.accept = dfa_28;
            this.special = dfa_29;
            this.transition = dfa_30;
        }
        public String getDescription() {
            return "3363:3: ( ( ( ( ( ruleExpression ) ) )=> ( (lv_expr_1_0= ruleExpression ) ) ) | (otherlv_2= '{' ( (lv_expr_3_0= ruleExpression ) ) otherlv_4= '}' ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_0 = input.LA(1);

                         
                        int index45_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA45_0==RULE_STRING) && (synpred12_InternalGaml())) {s = 1;}

                        else if ( (LA45_0==RULE_INTEGER) && (synpred12_InternalGaml())) {s = 2;}

                        else if ( (LA45_0==RULE_DOUBLE) && (synpred12_InternalGaml())) {s = 3;}

                        else if ( (LA45_0==RULE_BOOLEAN) && (synpred12_InternalGaml())) {s = 4;}

                        else if ( (LA45_0==RULE_KEYWORD) && (synpred12_InternalGaml())) {s = 5;}

                        else if ( (LA45_0==42) && (synpred12_InternalGaml())) {s = 6;}

                        else if ( (LA45_0==43) && (synpred12_InternalGaml())) {s = 7;}

                        else if ( (LA45_0==72) && (synpred12_InternalGaml())) {s = 8;}

                        else if ( (LA45_0==45) && (synpred12_InternalGaml())) {s = 9;}

                        else if ( (LA45_0==46) && (synpred12_InternalGaml())) {s = 10;}

                        else if ( (LA45_0==47) && (synpred12_InternalGaml())) {s = 11;}

                        else if ( (LA45_0==48) && (synpred12_InternalGaml())) {s = 12;}

                        else if ( (LA45_0==49) && (synpred12_InternalGaml())) {s = 13;}

                        else if ( (LA45_0==50) && (synpred12_InternalGaml())) {s = 14;}

                        else if ( (LA45_0==51) && (synpred12_InternalGaml())) {s = 15;}

                        else if ( (LA45_0==52) && (synpred12_InternalGaml())) {s = 16;}

                        else if ( (LA45_0==53) && (synpred12_InternalGaml())) {s = 17;}

                        else if ( (LA45_0==54) && (synpred12_InternalGaml())) {s = 18;}

                        else if ( (LA45_0==55) && (synpred12_InternalGaml())) {s = 19;}

                        else if ( (LA45_0==56) && (synpred12_InternalGaml())) {s = 20;}

                        else if ( (LA45_0==57) && (synpred12_InternalGaml())) {s = 21;}

                        else if ( (LA45_0==58) && (synpred12_InternalGaml())) {s = 22;}

                        else if ( (LA45_0==59) && (synpred12_InternalGaml())) {s = 23;}

                        else if ( (LA45_0==60) && (synpred12_InternalGaml())) {s = 24;}

                        else if ( (LA45_0==61) && (synpred12_InternalGaml())) {s = 25;}

                        else if ( (LA45_0==62) && (synpred12_InternalGaml())) {s = 26;}

                        else if ( (LA45_0==63) && (synpred12_InternalGaml())) {s = 27;}

                        else if ( (LA45_0==64) && (synpred12_InternalGaml())) {s = 28;}

                        else if ( (LA45_0==65) && (synpred12_InternalGaml())) {s = 29;}

                        else if ( (LA45_0==66) && (synpred12_InternalGaml())) {s = 30;}

                        else if ( (LA45_0==67) && (synpred12_InternalGaml())) {s = 31;}

                        else if ( (LA45_0==68) && (synpred12_InternalGaml())) {s = 32;}

                        else if ( (LA45_0==69) && (synpred12_InternalGaml())) {s = 33;}

                        else if ( (LA45_0==70) && (synpred12_InternalGaml())) {s = 34;}

                        else if ( (LA45_0==71) && (synpred12_InternalGaml())) {s = 35;}

                        else if ( (LA45_0==44) && (synpred12_InternalGaml())) {s = 36;}

                        else if ( (LA45_0==RULE_ID) && (synpred12_InternalGaml())) {s = 37;}

                        else if ( (LA45_0==33) && (synpred12_InternalGaml())) {s = 38;}

                        else if ( (LA45_0==21) && (synpred12_InternalGaml())) {s = 39;}

                        else if ( (LA45_0==37) ) {s = 40;}

                        else if ( (LA45_0==124) && (synpred12_InternalGaml())) {s = 41;}

                        else if ( (LA45_0==120) && (synpred12_InternalGaml())) {s = 42;}

                        else if ( (LA45_0==125) && (synpred12_InternalGaml())) {s = 43;}

                        else if ( (LA45_0==126) && (synpred12_InternalGaml())) {s = 44;}

                         
                        input.seek(index45_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA45_40 = input.LA(1);

                         
                        int index45_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_InternalGaml()) ) {s = 44;}

                        else if ( (true) ) {s = 45;}

                         
                        input.seek(index45_40);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_31s = "\1\4\2\0\53\uffff";
    static final String dfa_32s = "\1\176\2\0\53\uffff";
    static final String dfa_33s = "\3\uffff\1\2\51\uffff\1\1";
    static final String dfa_34s = "\1\uffff\1\0\1\1\53\uffff}>";
    static final String[] dfa_35s = {
            "\1\1\5\3\13\uffff\1\3\13\uffff\1\3\3\uffff\1\3\4\uffff\1\2\36\3\57\uffff\1\3\3\uffff\3\3",
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
            ""
    };
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final char[] dfa_32 = DFA.unpackEncodedStringToUnsignedChars(dfa_32s);
    static final short[] dfa_33 = DFA.unpackEncodedString(dfa_33s);
    static final short[] dfa_34 = DFA.unpackEncodedString(dfa_34s);
    static final short[][] dfa_35 = unpackEncodedStringArray(dfa_35s);

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = dfa_25;
            this.eof = dfa_25;
            this.min = dfa_31;
            this.max = dfa_32;
            this.accept = dfa_33;
            this.special = dfa_34;
            this.transition = dfa_35;
        }
        public String getDescription() {
            return "3460:3: ( ( ( 'species' | RULE_ID )=> ( (lv_expr_1_0= ruleTypeRef ) ) ) | ( (lv_expr_2_0= ruleExpression ) ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_1 = input.LA(1);

                         
                        int index46_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_InternalGaml()) ) {s = 45;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index46_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA46_2 = input.LA(1);

                         
                        int index46_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_InternalGaml()) ) {s = 45;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index46_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_36s = "\1\4\40\0\2\uffff";
    static final String dfa_37s = "\1\110\40\0\2\uffff";
    static final String dfa_38s = "\41\uffff\1\1\1\2";
    static final String dfa_39s = "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\2\uffff}>";
    static final String[] dfa_40s = {
            "\1\40\45\uffff\1\1\1\2\1\37\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\3",
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

    class DFA74 extends DFA {

        public DFA74(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 74;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_36;
            this.max = dfa_37;
            this.accept = dfa_38;
            this.special = dfa_39;
            this.transition = dfa_40;
        }
        public String getDescription() {
            return "5128:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA74_1 = input.LA(1);

                         
                        int index74_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA74_2 = input.LA(1);

                         
                        int index74_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA74_3 = input.LA(1);

                         
                        int index74_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA74_4 = input.LA(1);

                         
                        int index74_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA74_5 = input.LA(1);

                         
                        int index74_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA74_6 = input.LA(1);

                         
                        int index74_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA74_7 = input.LA(1);

                         
                        int index74_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA74_8 = input.LA(1);

                         
                        int index74_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA74_9 = input.LA(1);

                         
                        int index74_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA74_10 = input.LA(1);

                         
                        int index74_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA74_11 = input.LA(1);

                         
                        int index74_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA74_12 = input.LA(1);

                         
                        int index74_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA74_13 = input.LA(1);

                         
                        int index74_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA74_14 = input.LA(1);

                         
                        int index74_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA74_15 = input.LA(1);

                         
                        int index74_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA74_16 = input.LA(1);

                         
                        int index74_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA74_17 = input.LA(1);

                         
                        int index74_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA74_18 = input.LA(1);

                         
                        int index74_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA74_19 = input.LA(1);

                         
                        int index74_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA74_20 = input.LA(1);

                         
                        int index74_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA74_21 = input.LA(1);

                         
                        int index74_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA74_22 = input.LA(1);

                         
                        int index74_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA74_23 = input.LA(1);

                         
                        int index74_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA74_24 = input.LA(1);

                         
                        int index74_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA74_25 = input.LA(1);

                         
                        int index74_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA74_26 = input.LA(1);

                         
                        int index74_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA74_27 = input.LA(1);

                         
                        int index74_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA74_28 = input.LA(1);

                         
                        int index74_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA74_29 = input.LA(1);

                         
                        int index74_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA74_30 = input.LA(1);

                         
                        int index74_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_30);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA74_31 = input.LA(1);

                         
                        int index74_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_31);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA74_32 = input.LA(1);

                         
                        int index74_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred14_InternalGaml()) ) {s = 33;}

                        else if ( (true) ) {s = 34;}

                         
                        input.seek(index74_32);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 74, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_41s = "\2\uffff\40\1\1\uffff";
    static final String dfa_42s = "\1\4\1\uffff\40\4\1\uffff";
    static final String dfa_43s = "\1\176\1\uffff\40\177\1\uffff";
    static final String dfa_44s = "\1\uffff\1\1\40\uffff\1\2";
    static final String[] dfa_45s = {
            "\1\41\5\1\13\uffff\1\1\13\uffff\1\1\3\uffff\1\1\4\uffff\1\2\1\3\1\40\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\4\11\uffff\34\42\12\uffff\1\1\3\uffff\3\1",
            "",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            "\1\1\16\uffff\1\1\1\uffff\2\1\12\uffff\2\1\4\uffff\1\1\2\uffff\37\1\2\uffff\1\1\4\uffff\1\1\1\42\35\uffff\16\1\2\uffff\1\1",
            ""
    };
    static final short[] dfa_41 = DFA.unpackEncodedString(dfa_41s);
    static final char[] dfa_42 = DFA.unpackEncodedStringToUnsignedChars(dfa_42s);
    static final char[] dfa_43 = DFA.unpackEncodedStringToUnsignedChars(dfa_43s);
    static final short[] dfa_44 = DFA.unpackEncodedString(dfa_44s);
    static final short[][] dfa_45 = unpackEncodedStringArray(dfa_45s);

    class DFA79 extends DFA {

        public DFA79(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 79;
            this.eot = dfa_13;
            this.eof = dfa_41;
            this.min = dfa_42;
            this.max = dfa_43;
            this.accept = dfa_44;
            this.special = dfa_17;
            this.transition = dfa_45;
        }
        public String getDescription() {
            return "5258:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )";
        }
    }
    static final String dfa_46s = "\20\uffff";
    static final String dfa_47s = "\1\4\2\0\15\uffff";
    static final String dfa_48s = "\1\u0084\2\0\15\uffff";
    static final String dfa_49s = "\3\uffff\5\1\1\2\1\uffff\1\4\1\uffff\1\5\1\6\1\7\1\3";
    static final String dfa_50s = "\1\0\1\1\1\2\15\uffff}>";
    static final String[] dfa_51s = {
            "\1\1\14\uffff\1\10\1\15\1\uffff\1\10\6\uffff\1\7\7\uffff\1\6\6\uffff\1\2\1\3\1\16\33\uffff\1\5\1\4\10\uffff\2\12\60\uffff\1\14",
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
            ""
    };

    static final short[] dfa_46 = DFA.unpackEncodedString(dfa_46s);
    static final char[] dfa_47 = DFA.unpackEncodedStringToUnsignedChars(dfa_47s);
    static final char[] dfa_48 = DFA.unpackEncodedStringToUnsignedChars(dfa_48s);
    static final short[] dfa_49 = DFA.unpackEncodedString(dfa_49s);
    static final short[] dfa_50 = DFA.unpackEncodedString(dfa_50s);
    static final short[][] dfa_51 = unpackEncodedStringArray(dfa_51s);

    class DFA88 extends DFA {

        public DFA88(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 88;
            this.eot = dfa_46;
            this.eof = dfa_46;
            this.min = dfa_47;
            this.max = dfa_48;
            this.accept = dfa_49;
            this.special = dfa_50;
            this.transition = dfa_51;
        }
        public String getDescription() {
            return "5936:2: ( ( ( ( ( 'species' | RULE_ID ) | ruleS_Species | ruleS_Reflex | ruleS_Action | ruleS_Loop ) )=> ( ( ( 'species' | RULE_ID )=>this_S_Definition_0= ruleS_Definition ) | this_S_Species_1= ruleS_Species | this_S_Reflex_2= ruleS_Reflex | this_S_Action_3= ruleS_Action | this_S_Loop_4= ruleS_Loop ) ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA88_0 = input.LA(1);

                         
                        int index88_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA88_0==RULE_ID) ) {s = 1;}

                        else if ( (LA88_0==42) ) {s = 2;}

                        else if ( (LA88_0==43) && (synpred16_InternalGaml())) {s = 3;}

                        else if ( (LA88_0==73) && (synpred16_InternalGaml())) {s = 4;}

                        else if ( (LA88_0==72) && (synpred16_InternalGaml())) {s = 5;}

                        else if ( (LA88_0==35) && (synpred16_InternalGaml())) {s = 6;}

                        else if ( (LA88_0==27) && (synpred16_InternalGaml())) {s = 7;}

                        else if ( (LA88_0==17||LA88_0==20) ) {s = 8;}

                        else if ( ((LA88_0>=82 && LA88_0<=83)) ) {s = 10;}

                        else if ( (LA88_0==132) ) {s = 12;}

                        else if ( (LA88_0==18) ) {s = 13;}

                        else if ( (LA88_0==44) ) {s = 14;}

                         
                        input.seek(index88_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA88_1 = input.LA(1);

                         
                        int index88_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_InternalGaml()) ) {s = 7;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index88_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA88_2 = input.LA(1);

                         
                        int index88_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred16_InternalGaml()) ) {s = 7;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index88_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 88, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0xFFFFFC22002003F0L,0x71000000000001FFL});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0xFFFFFC0000000030L,0x00000000000001FFL});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000002000818010L,0x00007FFFFFFC0000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000120000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0xFFFFFC0000000010L,0x00000000000001FFL});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x00001C0001040000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0xFFFFFC22006083F0L,0x71003FFFFFFC01FFL});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000002000808010L,0x00007FFFFFFC0000L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000002000008010L,0x00007FFFFFFC0000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000002010000000L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0xFFFFFC2200A003F0L,0x71000000000001FFL});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0xFFFFFC2200A183F0L,0x71007FFFFFFC01FFL});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0xFFFFFC2000818010L,0x00007FFFFFFC01FFL});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000002200818010L,0x00007FFFFFFC0000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000040000000010L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0000000000008000L,0x000000000000FC00L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000000000808010L,0x00007FFFFFFC0000L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0xFFFFFC4000000010L,0x00000000000001FFL});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0000000000000002L,0x0000000000010000L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0xFFFFFC2000000010L,0x00000000000001FFL});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0xFFFFFF7B5E2003F0L,0x71000000000003FFL});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x00001C0001000002L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000002L,0x0000800000000000L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0000000000000002L,0x0001000000000000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000002L,0x0002000000000000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000040200000010L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000008000000002L,0x0078000000000800L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000000002L,0x0180000000000000L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000000002L,0x0E00000000000000L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0xFFFFFC0000000012L,0x00000000000001FFL});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000002L,0x1000000000000000L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000200002L,0x8000000000000000L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0xFFFFFC22002003F0L,0x00000000000001FFL});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0xFFFFFC22002083F0L,0x71003FFFFFFC01FFL});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000004000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000200000000L,0x0040000000000000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0xFFFFFC26002083F0L,0x71003FFFFFFC01FFL});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000002L,0x0040000000000000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0x0000000000000000L,0x0040000000000000L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010800L});

}