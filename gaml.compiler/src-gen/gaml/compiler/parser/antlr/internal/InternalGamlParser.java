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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_INTEGER", "RULE_DOUBLE", "RULE_BOOLEAN", "RULE_KEYWORD", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'__standalone_block__'", "'__standalone_expression__'", "'model:'", "'model'", "'import'", "'as'", "'@'", "'['", "']'", "';'", "','", "'global'", "'do'", "'invoke'", "'loop'", "'if'", "'else'", "'try'", "'catch'", "'switch'", "'match'", "'match_between'", "'match_one'", "'match_regex'", "'default'", "'return'", "'reflex'", "'abort'", "'('", "')'", "'action'", "'equation'", "'{'", "'}'", "'='", "'solve'", "'display'", "'ask'", "'text'", "'assert'", "'setup'", "'add'", "'remove'", "'put'", "'capture'", "'release'", "'migrate'", "'create'", "'error'", "'warn'", "'write'", "'status'", "'focus_on'", "'highlight'", "'layout'", "'save'", "'restore'", "'diffuse'", "'species'", "'grid'", "'init'", "'experiment'", "'<-'", "'<<'", "'>'", "'<<+'", "'>-'", "'+<-'", "'<+'", "':'", "'as:'", "'returns:'", "'action:'", "'on_change:'", "'->'", "'::'", "'?'", "'or'", "'and'", "'!='", "'>='", "'<='", "'<'", "'+'", "'-'", "'*'", "'/'", "'^'", "'#'", "'!'", "'not'", "'.'", "'**unit*'", "'**type*'", "'**action*'", "'**skill*'", "'**var*'", "'**equation*'"
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
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=5;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=10;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
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
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int RULE_STRING=4;
    public static final int RULE_SL_COMMENT=11;
    public static final int RULE_DOUBLE=7;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__80=80;
    public static final int T__111=111;
    public static final int T__81=81;
    public static final int T__110=110;
    public static final int T__82=82;
    public static final int T__83=83;
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
            else if ( (LA1_0==15) ) {
                alt1=2;
            }
            else if ( (LA1_0==14) ) {
                alt1=3;
            }
            else if ( (LA1_0==75) ) {
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
    // InternalGaml.g:128:1: ruleStandaloneBlock returns [EObject current=null] : (otherlv_0= '__standalone_block__' ( (lv_block_1_0= ruleBlock ) ) ) ;
    public final EObject ruleStandaloneBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:134:2: ( (otherlv_0= '__standalone_block__' ( (lv_block_1_0= ruleBlock ) ) ) )
            // InternalGaml.g:135:2: (otherlv_0= '__standalone_block__' ( (lv_block_1_0= ruleBlock ) ) )
            {
            // InternalGaml.g:135:2: (otherlv_0= '__standalone_block__' ( (lv_block_1_0= ruleBlock ) ) )
            // InternalGaml.g:136:3: otherlv_0= '__standalone_block__' ( (lv_block_1_0= ruleBlock ) )
            {
            otherlv_0=(Token)match(input,14,FOLLOW_3); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getStandaloneBlockAccess().get__standalone_block__Keyword_0());
              		
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
    // InternalGaml.g:170:1: ruleStandaloneExpression returns [EObject current=null] : (otherlv_0= '__standalone_expression__' ( (lv_expr_1_0= ruleExpression ) ) ) ;
    public final EObject ruleStandaloneExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:176:2: ( (otherlv_0= '__standalone_expression__' ( (lv_expr_1_0= ruleExpression ) ) ) )
            // InternalGaml.g:177:2: (otherlv_0= '__standalone_expression__' ( (lv_expr_1_0= ruleExpression ) ) )
            {
            // InternalGaml.g:177:2: (otherlv_0= '__standalone_expression__' ( (lv_expr_1_0= ruleExpression ) ) )
            // InternalGaml.g:178:3: otherlv_0= '__standalone_expression__' ( (lv_expr_1_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,15,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getStandaloneExpressionAccess().get__standalone_expression__Keyword_0());
              		
            }
            // InternalGaml.g:182:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:183:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:183:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:184:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStandaloneExpressionAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getStandaloneExpressionRule());
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

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
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
    // InternalGaml.g:205:1: entryRuleStandaloneExperiment returns [EObject current=null] : iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF ;
    public final EObject entryRuleStandaloneExperiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStandaloneExperiment = null;


        try {
            // InternalGaml.g:205:61: (iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF )
            // InternalGaml.g:206:2: iv_ruleStandaloneExperiment= ruleStandaloneExperiment EOF
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
    // InternalGaml.g:212:1: ruleStandaloneExperiment returns [EObject current=null] : ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) ;
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
            // InternalGaml.g:218:2: ( ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:219:2: ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:219:2: ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:220:3: ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )? this_FacetsAndBlock_4= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:220:3: ( (lv_key_0_0= ruleK_Experiment ) )
            // InternalGaml.g:221:4: (lv_key_0_0= ruleK_Experiment )
            {
            // InternalGaml.g:221:4: (lv_key_0_0= ruleK_Experiment )
            // InternalGaml.g:222:5: lv_key_0_0= ruleK_Experiment
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getStandaloneExperimentAccess().getKeyK_ExperimentParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=ruleK_Experiment();

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
              						"gaml.compiler.Gaml.K_Experiment");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:239:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:240:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:240:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:241:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:241:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==RULE_ID||LA2_0==38||(LA2_0>=51 && LA2_0<=75)) ) {
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
                    // InternalGaml.g:242:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStandaloneExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_6);
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
                    // InternalGaml.g:258:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_6); if (state.failed) return current;
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

            // InternalGaml.g:275:3: (otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==16) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // InternalGaml.g:276:4: otherlv_2= 'model:' ( (lv_importURI_3_0= RULE_STRING ) )
                    {
                    otherlv_2=(Token)match(input,16,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getStandaloneExperimentAccess().getModelKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:280:4: ( (lv_importURI_3_0= RULE_STRING ) )
                    // InternalGaml.g:281:5: (lv_importURI_3_0= RULE_STRING )
                    {
                    // InternalGaml.g:281:5: (lv_importURI_3_0= RULE_STRING )
                    // InternalGaml.g:282:6: lv_importURI_3_0= RULE_STRING
                    {
                    lv_importURI_3_0=(Token)match(input,RULE_STRING,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:314:1: entryRuleModel returns [EObject current=null] : iv_ruleModel= ruleModel EOF ;
    public final EObject entryRuleModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModel = null;


        try {
            // InternalGaml.g:314:46: (iv_ruleModel= ruleModel EOF )
            // InternalGaml.g:315:2: iv_ruleModel= ruleModel EOF
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
    // InternalGaml.g:321:1: ruleModel returns [EObject current=null] : ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) ;
    public final EObject ruleModel() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_pragmas_0_0 = null;

        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject lv_imports_3_0 = null;

        EObject lv_block_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:327:2: ( ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) ) )
            // InternalGaml.g:328:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            {
            // InternalGaml.g:328:2: ( ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) ) )
            // InternalGaml.g:329:3: ( (lv_pragmas_0_0= rulePragma ) )* otherlv_1= 'model' ( (lv_name_2_0= ruleValid_ID ) ) ( (lv_imports_3_0= ruleImport ) )* ( (lv_block_4_0= ruleModelBlock ) )
            {
            // InternalGaml.g:329:3: ( (lv_pragmas_0_0= rulePragma ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==20) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalGaml.g:330:4: (lv_pragmas_0_0= rulePragma )
            	    {
            	    // InternalGaml.g:330:4: (lv_pragmas_0_0= rulePragma )
            	    // InternalGaml.g:331:5: lv_pragmas_0_0= rulePragma
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getPragmasPragmaParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_8);
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

            otherlv_1=(Token)match(input,17,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getModelAccess().getModelKeyword_1());
              		
            }
            // InternalGaml.g:352:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:353:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:353:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:354:5: lv_name_2_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getModelAccess().getNameValid_IDParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_10);
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

            // InternalGaml.g:371:3: ( (lv_imports_3_0= ruleImport ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==18) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // InternalGaml.g:372:4: (lv_imports_3_0= ruleImport )
            	    {
            	    // InternalGaml.g:372:4: (lv_imports_3_0= ruleImport )
            	    // InternalGaml.g:373:5: lv_imports_3_0= ruleImport
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelAccess().getImportsImportParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_10);
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

            // InternalGaml.g:390:3: ( (lv_block_4_0= ruleModelBlock ) )
            // InternalGaml.g:391:4: (lv_block_4_0= ruleModelBlock )
            {
            // InternalGaml.g:391:4: (lv_block_4_0= ruleModelBlock )
            // InternalGaml.g:392:5: lv_block_4_0= ruleModelBlock
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
    // InternalGaml.g:413:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // InternalGaml.g:413:47: (iv_ruleImport= ruleImport EOF )
            // InternalGaml.g:414:2: iv_ruleImport= ruleImport EOF
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
    // InternalGaml.g:420:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_importURI_1_0=null;
        Token otherlv_2=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:426:2: ( (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? ) )
            // InternalGaml.g:427:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            {
            // InternalGaml.g:427:2: (otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )? )
            // InternalGaml.g:428:3: otherlv_0= 'import' ( (lv_importURI_1_0= RULE_STRING ) ) (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            {
            otherlv_0=(Token)match(input,18,FOLLOW_7); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
              		
            }
            // InternalGaml.g:432:3: ( (lv_importURI_1_0= RULE_STRING ) )
            // InternalGaml.g:433:4: (lv_importURI_1_0= RULE_STRING )
            {
            // InternalGaml.g:433:4: (lv_importURI_1_0= RULE_STRING )
            // InternalGaml.g:434:5: lv_importURI_1_0= RULE_STRING
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

            // InternalGaml.g:450:3: (otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) ) )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==19) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // InternalGaml.g:451:4: otherlv_2= 'as' ( (lv_name_3_0= ruleValid_ID ) )
                    {
                    otherlv_2=(Token)match(input,19,FOLLOW_9); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getImportAccess().getAsKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:455:4: ( (lv_name_3_0= ruleValid_ID ) )
                    // InternalGaml.g:456:5: (lv_name_3_0= ruleValid_ID )
                    {
                    // InternalGaml.g:456:5: (lv_name_3_0= ruleValid_ID )
                    // InternalGaml.g:457:6: lv_name_3_0= ruleValid_ID
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
    // InternalGaml.g:479:1: entryRulePragma returns [EObject current=null] : iv_rulePragma= rulePragma EOF ;
    public final EObject entryRulePragma() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePragma = null;


        try {
            // InternalGaml.g:479:47: (iv_rulePragma= rulePragma EOF )
            // InternalGaml.g:480:2: iv_rulePragma= rulePragma EOF
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
    // InternalGaml.g:486:1: rulePragma returns [EObject current=null] : (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) ;
    public final EObject rulePragma() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_plugins_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:492:2: ( (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) ) )
            // InternalGaml.g:493:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            {
            // InternalGaml.g:493:2: (otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? ) )
            // InternalGaml.g:494:3: otherlv_0= '@' ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            {
            otherlv_0=(Token)match(input,20,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getPragmaAccess().getCommercialAtKeyword_0());
              		
            }
            // InternalGaml.g:498:3: ( ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )? )
            // InternalGaml.g:499:4: ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            {
            // InternalGaml.g:499:4: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:500:5: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:500:5: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:501:6: lv_name_1_0= RULE_ID
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

            // InternalGaml.g:517:4: (otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==21) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // InternalGaml.g:518:5: otherlv_2= '[' ( (lv_plugins_3_0= ruleExpressionList ) )? otherlv_4= ']'
                    {
                    otherlv_2=(Token)match(input,21,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getPragmaAccess().getLeftSquareBracketKeyword_1_1_0());
                      				
                    }
                    // InternalGaml.g:522:5: ( (lv_plugins_3_0= ruleExpressionList ) )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( ((LA7_0>=RULE_STRING && LA7_0<=RULE_KEYWORD)||LA7_0==21||LA7_0==38||LA7_0==42||LA7_0==46||(LA7_0>=51 && LA7_0<=75)||(LA7_0>=85 && LA7_0<=87)||LA7_0==98||(LA7_0>=102 && LA7_0<=104)) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // InternalGaml.g:523:6: (lv_plugins_3_0= ruleExpressionList )
                            {
                            // InternalGaml.g:523:6: (lv_plugins_3_0= ruleExpressionList )
                            // InternalGaml.g:524:7: lv_plugins_3_0= ruleExpressionList
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
    // InternalGaml.g:552:1: ruleFacetsAndBlock[EObject in_current] returns [EObject current=in_current] : ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) ;
    public final EObject ruleFacetsAndBlock(EObject in_current) throws RecognitionException {
        EObject current = in_current;

        Token otherlv_2=null;
        EObject lv_facets_0_0 = null;

        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:558:2: ( ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) ) )
            // InternalGaml.g:559:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            {
            // InternalGaml.g:559:2: ( ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' ) )
            // InternalGaml.g:560:3: ( (lv_facets_0_0= ruleFacet ) )* ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            {
            // InternalGaml.g:560:3: ( (lv_facets_0_0= ruleFacet ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==RULE_ID||LA9_0==38||(LA9_0>=51 && LA9_0<=76)||(LA9_0>=84 && LA9_0<=88)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // InternalGaml.g:561:4: (lv_facets_0_0= ruleFacet )
            	    {
            	    // InternalGaml.g:561:4: (lv_facets_0_0= ruleFacet )
            	    // InternalGaml.g:562:5: lv_facets_0_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getFacetsAndBlockAccess().getFacetsFacetParserRuleCall_0_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_16);
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

            // InternalGaml.g:579:3: ( ( (lv_block_1_0= ruleBlock ) ) | otherlv_2= ';' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==46) ) {
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
                    // InternalGaml.g:580:4: ( (lv_block_1_0= ruleBlock ) )
                    {
                    // InternalGaml.g:580:4: ( (lv_block_1_0= ruleBlock ) )
                    // InternalGaml.g:581:5: (lv_block_1_0= ruleBlock )
                    {
                    // InternalGaml.g:581:5: (lv_block_1_0= ruleBlock )
                    // InternalGaml.g:582:6: lv_block_1_0= ruleBlock
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
                    // InternalGaml.g:600:4: otherlv_2= ';'
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


    // $ANTLR start "ruleActionArguments"
    // InternalGaml.g:610:1: ruleActionArguments[EObject in_current] returns [EObject current=in_current] : ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) ;
    public final EObject ruleActionArguments(EObject in_current) throws RecognitionException {
        EObject current = in_current;

        Token otherlv_1=null;
        EObject lv_args_0_0 = null;

        EObject lv_args_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:616:2: ( ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* ) )
            // InternalGaml.g:617:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            {
            // InternalGaml.g:617:2: ( ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )* )
            // InternalGaml.g:618:3: ( (lv_args_0_0= ruleArgumentDefinition ) ) (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            {
            // InternalGaml.g:618:3: ( (lv_args_0_0= ruleArgumentDefinition ) )
            // InternalGaml.g:619:4: (lv_args_0_0= ruleArgumentDefinition )
            {
            // InternalGaml.g:619:4: (lv_args_0_0= ruleArgumentDefinition )
            // InternalGaml.g:620:5: lv_args_0_0= ruleArgumentDefinition
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_17);
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

            // InternalGaml.g:637:3: (otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) ) )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==24) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // InternalGaml.g:638:4: otherlv_1= ',' ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    {
            	    otherlv_1=(Token)match(input,24,FOLLOW_18); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_1, grammarAccess.getActionArgumentsAccess().getCommaKeyword_1_0());
            	      			
            	    }
            	    // InternalGaml.g:642:4: ( (lv_args_2_0= ruleArgumentDefinition ) )
            	    // InternalGaml.g:643:5: (lv_args_2_0= ruleArgumentDefinition )
            	    {
            	    // InternalGaml.g:643:5: (lv_args_2_0= ruleArgumentDefinition )
            	    // InternalGaml.g:644:6: lv_args_2_0= ruleArgumentDefinition
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getActionArgumentsAccess().getArgsArgumentDefinitionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_17);
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
            	    break loop11;
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


    // $ANTLR start "entryRuleS_Section"
    // InternalGaml.g:666:1: entryRuleS_Section returns [EObject current=null] : iv_ruleS_Section= ruleS_Section EOF ;
    public final EObject entryRuleS_Section() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Section = null;


        try {
            // InternalGaml.g:666:50: (iv_ruleS_Section= ruleS_Section EOF )
            // InternalGaml.g:667:2: iv_ruleS_Section= ruleS_Section EOF
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
    // InternalGaml.g:673:1: ruleS_Section returns [EObject current=null] : (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) ;
    public final EObject ruleS_Section() throws RecognitionException {
        EObject current = null;

        EObject this_S_Global_0 = null;

        EObject this_S_Species_1 = null;

        EObject this_S_Experiment_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:679:2: ( (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment ) )
            // InternalGaml.g:680:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            {
            // InternalGaml.g:680:2: (this_S_Global_0= ruleS_Global | this_S_Species_1= ruleS_Species | this_S_Experiment_2= ruleS_Experiment )
            int alt12=3;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt12=1;
                }
                break;
            case 72:
            case 73:
                {
                alt12=2;
                }
                break;
            case 75:
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
                    // InternalGaml.g:681:3: this_S_Global_0= ruleS_Global
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
                    // InternalGaml.g:690:3: this_S_Species_1= ruleS_Species
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
                    // InternalGaml.g:699:3: this_S_Experiment_2= ruleS_Experiment
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
    // InternalGaml.g:711:1: entryRuleS_Global returns [EObject current=null] : iv_ruleS_Global= ruleS_Global EOF ;
    public final EObject entryRuleS_Global() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Global = null;


        try {
            // InternalGaml.g:711:49: (iv_ruleS_Global= ruleS_Global EOF )
            // InternalGaml.g:712:2: iv_ruleS_Global= ruleS_Global EOF
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
    // InternalGaml.g:718:1: ruleS_Global returns [EObject current=null] : ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Global() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject this_FacetsAndBlock_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:724:2: ( ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:725:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:725:2: ( ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:726:3: ( (lv_key_0_0= 'global' ) ) this_FacetsAndBlock_1= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:726:3: ( (lv_key_0_0= 'global' ) )
            // InternalGaml.g:727:4: (lv_key_0_0= 'global' )
            {
            // InternalGaml.g:727:4: (lv_key_0_0= 'global' )
            // InternalGaml.g:728:5: lv_key_0_0= 'global'
            {
            lv_key_0_0=(Token)match(input,25,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:755:1: entryRuleS_Species returns [EObject current=null] : iv_ruleS_Species= ruleS_Species EOF ;
    public final EObject entryRuleS_Species() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Species = null;


        try {
            // InternalGaml.g:755:50: (iv_ruleS_Species= ruleS_Species EOF )
            // InternalGaml.g:756:2: iv_ruleS_Species= ruleS_Species EOF
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
    // InternalGaml.g:762:1: ruleS_Species returns [EObject current=null] : ( ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Species() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_1 = null;

        AntlrDatatypeRuleToken lv_key_0_2 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:768:2: ( ( ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:769:2: ( ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:769:2: ( ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:770:3: ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) ) ( (lv_name_1_0= RULE_ID ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:770:3: ( ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) ) )
            // InternalGaml.g:771:4: ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) )
            {
            // InternalGaml.g:771:4: ( (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid ) )
            // InternalGaml.g:772:5: (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid )
            {
            // InternalGaml.g:772:5: (lv_key_0_1= ruleK_Species | lv_key_0_2= ruleK_Grid )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==72) ) {
                alt13=1;
            }
            else if ( (LA13_0==73) ) {
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
                    // InternalGaml.g:773:6: lv_key_0_1= ruleK_Species
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_SpeciesAccess().getKeyK_SpeciesParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_12);
                    lv_key_0_1=ruleK_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_SpeciesRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_1,
                      							"gaml.compiler.Gaml.K_Species");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:789:6: lv_key_0_2= ruleK_Grid
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_SpeciesAccess().getKeyK_GridParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_12);
                    lv_key_0_2=ruleK_Grid();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_SpeciesRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_2,
                      							"gaml.compiler.Gaml.K_Grid");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:807:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:808:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:808:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:809:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:840:1: entryRuleS_Experiment returns [EObject current=null] : iv_ruleS_Experiment= ruleS_Experiment EOF ;
    public final EObject entryRuleS_Experiment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Experiment = null;


        try {
            // InternalGaml.g:840:53: (iv_ruleS_Experiment= ruleS_Experiment EOF )
            // InternalGaml.g:841:2: iv_ruleS_Experiment= ruleS_Experiment EOF
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
    // InternalGaml.g:847:1: ruleS_Experiment returns [EObject current=null] : ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Experiment() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:853:2: ( ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:854:2: ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:854:2: ( ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:855:3: ( (lv_key_0_0= ruleK_Experiment ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:855:3: ( (lv_key_0_0= ruleK_Experiment ) )
            // InternalGaml.g:856:4: (lv_key_0_0= ruleK_Experiment )
            {
            // InternalGaml.g:856:4: (lv_key_0_0= ruleK_Experiment )
            // InternalGaml.g:857:5: lv_key_0_0= ruleK_Experiment
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_ExperimentAccess().getKeyK_ExperimentParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_5);
            lv_key_0_0=ruleK_Experiment();

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
              						"gaml.compiler.Gaml.K_Experiment");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalGaml.g:874:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:875:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:875:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:876:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:876:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==RULE_ID||LA14_0==38||(LA14_0>=51 && LA14_0<=75)) ) {
                alt14=1;
            }
            else if ( (LA14_0==RULE_STRING) ) {
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
                    // InternalGaml.g:877:6: lv_name_1_1= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ExperimentAccess().getNameValid_IDParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_6);
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
                    // InternalGaml.g:893:6: lv_name_1_2= RULE_STRING
                    {
                    lv_name_1_2=(Token)match(input,RULE_STRING,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:925:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalGaml.g:925:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalGaml.g:926:2: iv_ruleStatement= ruleStatement EOF
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
    // InternalGaml.g:932:1: ruleStatement returns [EObject current=null] : (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | this_S_Switch_7= ruleS_Switch | this_S_Equations_8= ruleS_Equations | this_S_Action_9= ruleS_Action | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition ) | this_S_Other_14= ruleS_Other ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_S_Display_0 = null;

        EObject this_S_Return_1 = null;

        EObject this_S_Solve_2 = null;

        EObject this_S_If_3 = null;

        EObject this_S_Try_4 = null;

        EObject this_S_Do_5 = null;

        EObject this_S_Loop_6 = null;

        EObject this_S_Switch_7 = null;

        EObject this_S_Equations_8 = null;

        EObject this_S_Action_9 = null;

        EObject this_S_Species_10 = null;

        EObject this_S_Reflex_11 = null;

        EObject this_S_Assignment_12 = null;

        EObject this_S_Definition_13 = null;

        EObject this_S_Other_14 = null;



        	enterRule();

        try {
            // InternalGaml.g:938:2: ( (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | this_S_Switch_7= ruleS_Switch | this_S_Equations_8= ruleS_Equations | this_S_Action_9= ruleS_Action | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition ) | this_S_Other_14= ruleS_Other ) )
            // InternalGaml.g:939:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | this_S_Switch_7= ruleS_Switch | this_S_Equations_8= ruleS_Equations | this_S_Action_9= ruleS_Action | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition ) | this_S_Other_14= ruleS_Other )
            {
            // InternalGaml.g:939:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | this_S_Switch_7= ruleS_Switch | this_S_Equations_8= ruleS_Equations | this_S_Action_9= ruleS_Action | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition ) | this_S_Other_14= ruleS_Other )
            int alt15=15;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // InternalGaml.g:940:3: this_S_Display_0= ruleS_Display
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
                    // InternalGaml.g:949:3: this_S_Return_1= ruleS_Return
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
                    // InternalGaml.g:958:3: this_S_Solve_2= ruleS_Solve
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
                    // InternalGaml.g:967:3: this_S_If_3= ruleS_If
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
                    // InternalGaml.g:976:3: this_S_Try_4= ruleS_Try
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
                    // InternalGaml.g:985:3: this_S_Do_5= ruleS_Do
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
                    // InternalGaml.g:994:3: this_S_Loop_6= ruleS_Loop
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
                    // InternalGaml.g:1003:3: this_S_Switch_7= ruleS_Switch
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_SwitchParserRuleCall_7());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Switch_7=ruleS_Switch();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Switch_7;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:1012:3: this_S_Equations_8= ruleS_Equations
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_EquationsParserRuleCall_8());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Equations_8=ruleS_Equations();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Equations_8;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:1021:3: this_S_Action_9= ruleS_Action
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_ActionParserRuleCall_9());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Action_9=ruleS_Action();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Action_9;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:1030:3: ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species )
                    {
                    // InternalGaml.g:1030:3: ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species )
                    // InternalGaml.g:1031:4: ( ruleS_Species )=>this_S_Species_10= ruleS_Species
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
                    // InternalGaml.g:1042:3: ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex )
                    {
                    // InternalGaml.g:1042:3: ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex )
                    // InternalGaml.g:1043:4: ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex
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
                    // InternalGaml.g:1054:3: ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment )
                    {
                    // InternalGaml.g:1054:3: ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment )
                    // InternalGaml.g:1055:4: ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment
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
                    // InternalGaml.g:1066:3: ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition )
                    {
                    // InternalGaml.g:1066:3: ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition )
                    // InternalGaml.g:1067:4: ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getStatementAccess().getS_DefinitionParserRuleCall_13());
                      			
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Definition_13=ruleS_Definition();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_S_Definition_13;
                      				afterParserOrEnumRuleCall();
                      			
                    }

                    }


                    }
                    break;
                case 15 :
                    // InternalGaml.g:1078:3: this_S_Other_14= ruleS_Other
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getStatementAccess().getS_OtherParserRuleCall_14());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_S_Other_14=ruleS_Other();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_S_Other_14;
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
    // InternalGaml.g:1090:1: entryRuleS_Do returns [EObject current=null] : iv_ruleS_Do= ruleS_Do EOF ;
    public final EObject entryRuleS_Do() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Do = null;


        try {
            // InternalGaml.g:1090:45: (iv_ruleS_Do= ruleS_Do EOF )
            // InternalGaml.g:1091:2: iv_ruleS_Do= ruleS_Do EOF
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
    // InternalGaml.g:1097:1: ruleS_Do returns [EObject current=null] : ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Do() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_1=null;
        Token lv_key_0_2=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1103:2: ( ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1104:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1104:2: ( ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1105:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) ) ( (lv_expr_1_0= ruleAbstractRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1105:3: ( ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) ) )
            // InternalGaml.g:1106:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            {
            // InternalGaml.g:1106:4: ( (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' ) )
            // InternalGaml.g:1107:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            {
            // InternalGaml.g:1107:5: (lv_key_0_1= 'do' | lv_key_0_2= 'invoke' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==26) ) {
                alt16=1;
            }
            else if ( (LA16_0==27) ) {
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
                    // InternalGaml.g:1108:6: lv_key_0_1= 'do'
                    {
                    lv_key_0_1=(Token)match(input,26,FOLLOW_9); if (state.failed) return current;
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
                    // InternalGaml.g:1119:6: lv_key_0_2= 'invoke'
                    {
                    lv_key_0_2=(Token)match(input,27,FOLLOW_9); if (state.failed) return current;
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

            // InternalGaml.g:1132:3: ( (lv_expr_1_0= ruleAbstractRef ) )
            // InternalGaml.g:1133:4: (lv_expr_1_0= ruleAbstractRef )
            {
            // InternalGaml.g:1133:4: (lv_expr_1_0= ruleAbstractRef )
            // InternalGaml.g:1134:5: lv_expr_1_0= ruleAbstractRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DoAccess().getExprAbstractRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_6);
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
    // InternalGaml.g:1166:1: entryRuleS_Loop returns [EObject current=null] : iv_ruleS_Loop= ruleS_Loop EOF ;
    public final EObject entryRuleS_Loop() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Loop = null;


        try {
            // InternalGaml.g:1166:47: (iv_ruleS_Loop= ruleS_Loop EOF )
            // InternalGaml.g:1167:2: iv_ruleS_Loop= ruleS_Loop EOF
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
    // InternalGaml.g:1173:1: ruleS_Loop returns [EObject current=null] : ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Loop() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_0=null;
        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1179:2: ( ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) ) )
            // InternalGaml.g:1180:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1180:2: ( ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) ) )
            // InternalGaml.g:1181:3: ( (lv_key_0_0= 'loop' ) ) ( (lv_name_1_0= RULE_ID ) )? ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleBlock ) )
            {
            // InternalGaml.g:1181:3: ( (lv_key_0_0= 'loop' ) )
            // InternalGaml.g:1182:4: (lv_key_0_0= 'loop' )
            {
            // InternalGaml.g:1182:4: (lv_key_0_0= 'loop' )
            // InternalGaml.g:1183:5: lv_key_0_0= 'loop'
            {
            lv_key_0_0=(Token)match(input,28,FOLLOW_19); if (state.failed) return current;
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

            // InternalGaml.g:1195:3: ( (lv_name_1_0= RULE_ID ) )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==RULE_ID) ) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==RULE_ID||LA17_1==38||LA17_1==46||(LA17_1>=51 && LA17_1<=76)||(LA17_1>=84 && LA17_1<=88)) ) {
                    alt17=1;
                }
            }
            switch (alt17) {
                case 1 :
                    // InternalGaml.g:1196:4: (lv_name_1_0= RULE_ID )
                    {
                    // InternalGaml.g:1196:4: (lv_name_1_0= RULE_ID )
                    // InternalGaml.g:1197:5: lv_name_1_0= RULE_ID
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

            // InternalGaml.g:1213:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==RULE_ID||LA18_0==38||(LA18_0>=51 && LA18_0<=76)||(LA18_0>=84 && LA18_0<=88)) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // InternalGaml.g:1214:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:1214:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:1215:5: lv_facets_2_0= ruleFacet
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
            	    break loop18;
                }
            } while (true);

            // InternalGaml.g:1232:3: ( (lv_block_3_0= ruleBlock ) )
            // InternalGaml.g:1233:4: (lv_block_3_0= ruleBlock )
            {
            // InternalGaml.g:1233:4: (lv_block_3_0= ruleBlock )
            // InternalGaml.g:1234:5: lv_block_3_0= ruleBlock
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
    // InternalGaml.g:1255:1: entryRuleS_If returns [EObject current=null] : iv_ruleS_If= ruleS_If EOF ;
    public final EObject entryRuleS_If() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_If = null;


        try {
            // InternalGaml.g:1255:45: (iv_ruleS_If= ruleS_If EOF )
            // InternalGaml.g:1256:2: iv_ruleS_If= ruleS_If EOF
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
    // InternalGaml.g:1262:1: ruleS_If returns [EObject current=null] : ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) ;
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
            // InternalGaml.g:1268:2: ( ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? ) )
            // InternalGaml.g:1269:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            {
            // InternalGaml.g:1269:2: ( ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )? )
            // InternalGaml.g:1270:3: ( (lv_key_0_0= 'if' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            {
            // InternalGaml.g:1270:3: ( (lv_key_0_0= 'if' ) )
            // InternalGaml.g:1271:4: (lv_key_0_0= 'if' )
            {
            // InternalGaml.g:1271:4: (lv_key_0_0= 'if' )
            // InternalGaml.g:1272:5: lv_key_0_0= 'if'
            {
            lv_key_0_0=(Token)match(input,29,FOLLOW_4); if (state.failed) return current;
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

            // InternalGaml.g:1284:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1285:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1285:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1286:5: lv_expr_1_0= ruleExpression
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

            // InternalGaml.g:1303:3: ( (lv_block_2_0= ruleBlock ) )
            // InternalGaml.g:1304:4: (lv_block_2_0= ruleBlock )
            {
            // InternalGaml.g:1304:4: (lv_block_2_0= ruleBlock )
            // InternalGaml.g:1305:5: lv_block_2_0= ruleBlock
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

            // InternalGaml.g:1322:3: ( ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==30) && (synpred6_InternalGaml())) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalGaml.g:1323:4: ( ( 'else' )=>otherlv_3= 'else' ) ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    {
                    // InternalGaml.g:1323:4: ( ( 'else' )=>otherlv_3= 'else' )
                    // InternalGaml.g:1324:5: ( 'else' )=>otherlv_3= 'else'
                    {
                    otherlv_3=(Token)match(input,30,FOLLOW_21); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_IfAccess().getElseKeyword_3_0());
                      				
                    }

                    }

                    // InternalGaml.g:1330:4: ( ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) ) )
                    // InternalGaml.g:1331:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    {
                    // InternalGaml.g:1331:5: ( (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock ) )
                    // InternalGaml.g:1332:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    {
                    // InternalGaml.g:1332:6: (lv_else_4_1= ruleS_If | lv_else_4_2= ruleBlock )
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==29) ) {
                        alt19=1;
                    }
                    else if ( (LA19_0==46) ) {
                        alt19=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 19, 0, input);

                        throw nvae;
                    }
                    switch (alt19) {
                        case 1 :
                            // InternalGaml.g:1333:7: lv_else_4_1= ruleS_If
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
                            // InternalGaml.g:1349:7: lv_else_4_2= ruleBlock
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
    // InternalGaml.g:1372:1: entryRuleS_Try returns [EObject current=null] : iv_ruleS_Try= ruleS_Try EOF ;
    public final EObject entryRuleS_Try() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Try = null;


        try {
            // InternalGaml.g:1372:46: (iv_ruleS_Try= ruleS_Try EOF )
            // InternalGaml.g:1373:2: iv_ruleS_Try= ruleS_Try EOF
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
    // InternalGaml.g:1379:1: ruleS_Try returns [EObject current=null] : ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) ;
    public final EObject ruleS_Try() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_block_1_0 = null;

        EObject lv_catch_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1385:2: ( ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? ) )
            // InternalGaml.g:1386:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            {
            // InternalGaml.g:1386:2: ( ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )? )
            // InternalGaml.g:1387:3: ( (lv_key_0_0= 'try' ) ) ( (lv_block_1_0= ruleBlock ) ) ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            {
            // InternalGaml.g:1387:3: ( (lv_key_0_0= 'try' ) )
            // InternalGaml.g:1388:4: (lv_key_0_0= 'try' )
            {
            // InternalGaml.g:1388:4: (lv_key_0_0= 'try' )
            // InternalGaml.g:1389:5: lv_key_0_0= 'try'
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

            // InternalGaml.g:1401:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:1402:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:1402:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:1403:5: lv_block_1_0= ruleBlock
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

            // InternalGaml.g:1420:3: ( ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) ) )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==32) && (synpred7_InternalGaml())) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // InternalGaml.g:1421:4: ( ( 'catch' )=>otherlv_2= 'catch' ) ( (lv_catch_3_0= ruleBlock ) )
                    {
                    // InternalGaml.g:1421:4: ( ( 'catch' )=>otherlv_2= 'catch' )
                    // InternalGaml.g:1422:5: ( 'catch' )=>otherlv_2= 'catch'
                    {
                    otherlv_2=(Token)match(input,32,FOLLOW_3); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_2, grammarAccess.getS_TryAccess().getCatchKeyword_2_0());
                      				
                    }

                    }

                    // InternalGaml.g:1428:4: ( (lv_catch_3_0= ruleBlock ) )
                    // InternalGaml.g:1429:5: (lv_catch_3_0= ruleBlock )
                    {
                    // InternalGaml.g:1429:5: (lv_catch_3_0= ruleBlock )
                    // InternalGaml.g:1430:6: lv_catch_3_0= ruleBlock
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


    // $ANTLR start "entryRuleS_Switch"
    // InternalGaml.g:1452:1: entryRuleS_Switch returns [EObject current=null] : iv_ruleS_Switch= ruleS_Switch EOF ;
    public final EObject entryRuleS_Switch() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Switch = null;


        try {
            // InternalGaml.g:1452:49: (iv_ruleS_Switch= ruleS_Switch EOF )
            // InternalGaml.g:1453:2: iv_ruleS_Switch= ruleS_Switch EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_SwitchRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Switch=ruleS_Switch();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Switch; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Switch"


    // $ANTLR start "ruleS_Switch"
    // InternalGaml.g:1459:1: ruleS_Switch returns [EObject current=null] : ( ( (lv_key_0_0= 'switch' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleMatchBlock ) ) ) ;
    public final EObject ruleS_Switch() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1465:2: ( ( ( (lv_key_0_0= 'switch' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleMatchBlock ) ) ) )
            // InternalGaml.g:1466:2: ( ( (lv_key_0_0= 'switch' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleMatchBlock ) ) )
            {
            // InternalGaml.g:1466:2: ( ( (lv_key_0_0= 'switch' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleMatchBlock ) ) )
            // InternalGaml.g:1467:3: ( (lv_key_0_0= 'switch' ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleMatchBlock ) )
            {
            // InternalGaml.g:1467:3: ( (lv_key_0_0= 'switch' ) )
            // InternalGaml.g:1468:4: (lv_key_0_0= 'switch' )
            {
            // InternalGaml.g:1468:4: (lv_key_0_0= 'switch' )
            // InternalGaml.g:1469:5: lv_key_0_0= 'switch'
            {
            lv_key_0_0=(Token)match(input,33,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_SwitchAccess().getKeySwitchKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_SwitchRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "switch");
              				
            }

            }


            }

            // InternalGaml.g:1481:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1482:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1482:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1483:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SwitchAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_3);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SwitchRule());
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

            // InternalGaml.g:1500:3: ( (lv_block_2_0= ruleMatchBlock ) )
            // InternalGaml.g:1501:4: (lv_block_2_0= ruleMatchBlock )
            {
            // InternalGaml.g:1501:4: (lv_block_2_0= ruleMatchBlock )
            // InternalGaml.g:1502:5: lv_block_2_0= ruleMatchBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SwitchAccess().getBlockMatchBlockParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_2_0=ruleMatchBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_SwitchRule());
              					}
              					set(
              						current,
              						"block",
              						lv_block_2_0,
              						"gaml.compiler.Gaml.MatchBlock");
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
    // $ANTLR end "ruleS_Switch"


    // $ANTLR start "entryRuleS_Match"
    // InternalGaml.g:1523:1: entryRuleS_Match returns [EObject current=null] : iv_ruleS_Match= ruleS_Match EOF ;
    public final EObject entryRuleS_Match() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Match = null;


        try {
            // InternalGaml.g:1523:48: (iv_ruleS_Match= ruleS_Match EOF )
            // InternalGaml.g:1524:2: iv_ruleS_Match= ruleS_Match EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_MatchRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Match=ruleS_Match();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Match; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Match"


    // $ANTLR start "ruleS_Match"
    // InternalGaml.g:1530:1: ruleS_Match returns [EObject current=null] : ( ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Match() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_1=null;
        Token lv_key_0_2=null;
        Token lv_key_0_3=null;
        Token lv_key_0_4=null;
        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1536:2: ( ( ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) ) )
            // InternalGaml.g:1537:2: ( ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1537:2: ( ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) ) )
            // InternalGaml.g:1538:3: ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) ) ( (lv_expr_1_0= ruleExpression ) ) ( (lv_block_2_0= ruleBlock ) )
            {
            // InternalGaml.g:1538:3: ( ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) ) )
            // InternalGaml.g:1539:4: ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) )
            {
            // InternalGaml.g:1539:4: ( (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' ) )
            // InternalGaml.g:1540:5: (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' )
            {
            // InternalGaml.g:1540:5: (lv_key_0_1= 'match' | lv_key_0_2= 'match_between' | lv_key_0_3= 'match_one' | lv_key_0_4= 'match_regex' )
            int alt22=4;
            switch ( input.LA(1) ) {
            case 34:
                {
                alt22=1;
                }
                break;
            case 35:
                {
                alt22=2;
                }
                break;
            case 36:
                {
                alt22=3;
                }
                break;
            case 37:
                {
                alt22=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }

            switch (alt22) {
                case 1 :
                    // InternalGaml.g:1541:6: lv_key_0_1= 'match'
                    {
                    lv_key_0_1=(Token)match(input,34,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_1, grammarAccess.getS_MatchAccess().getKeyMatchKeyword_0_0_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_MatchRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_1, null);
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:1552:6: lv_key_0_2= 'match_between'
                    {
                    lv_key_0_2=(Token)match(input,35,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_2, grammarAccess.getS_MatchAccess().getKeyMatch_betweenKeyword_0_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_MatchRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_2, null);
                      					
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:1563:6: lv_key_0_3= 'match_one'
                    {
                    lv_key_0_3=(Token)match(input,36,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_3, grammarAccess.getS_MatchAccess().getKeyMatch_oneKeyword_0_0_2());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_MatchRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_3, null);
                      					
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:1574:6: lv_key_0_4= 'match_regex'
                    {
                    lv_key_0_4=(Token)match(input,37,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_4, grammarAccess.getS_MatchAccess().getKeyMatch_regexKeyword_0_0_3());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_MatchRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_4, null);
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:1587:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:1588:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:1588:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:1589:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_MatchAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_3);
            lv_expr_1_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_MatchRule());
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

            // InternalGaml.g:1606:3: ( (lv_block_2_0= ruleBlock ) )
            // InternalGaml.g:1607:4: (lv_block_2_0= ruleBlock )
            {
            // InternalGaml.g:1607:4: (lv_block_2_0= ruleBlock )
            // InternalGaml.g:1608:5: lv_block_2_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_MatchAccess().getBlockBlockParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_2_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_MatchRule());
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


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleS_Match"


    // $ANTLR start "entryRuleS_Default"
    // InternalGaml.g:1629:1: entryRuleS_Default returns [EObject current=null] : iv_ruleS_Default= ruleS_Default EOF ;
    public final EObject entryRuleS_Default() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Default = null;


        try {
            // InternalGaml.g:1629:50: (iv_ruleS_Default= ruleS_Default EOF )
            // InternalGaml.g:1630:2: iv_ruleS_Default= ruleS_Default EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getS_DefaultRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleS_Default=ruleS_Default();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleS_Default; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleS_Default"


    // $ANTLR start "ruleS_Default"
    // InternalGaml.g:1636:1: ruleS_Default returns [EObject current=null] : ( ( (lv_key_0_0= 'default' ) ) ( (lv_block_1_0= ruleBlock ) ) ) ;
    public final EObject ruleS_Default() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_block_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1642:2: ( ( ( (lv_key_0_0= 'default' ) ) ( (lv_block_1_0= ruleBlock ) ) ) )
            // InternalGaml.g:1643:2: ( ( (lv_key_0_0= 'default' ) ) ( (lv_block_1_0= ruleBlock ) ) )
            {
            // InternalGaml.g:1643:2: ( ( (lv_key_0_0= 'default' ) ) ( (lv_block_1_0= ruleBlock ) ) )
            // InternalGaml.g:1644:3: ( (lv_key_0_0= 'default' ) ) ( (lv_block_1_0= ruleBlock ) )
            {
            // InternalGaml.g:1644:3: ( (lv_key_0_0= 'default' ) )
            // InternalGaml.g:1645:4: (lv_key_0_0= 'default' )
            {
            // InternalGaml.g:1645:4: (lv_key_0_0= 'default' )
            // InternalGaml.g:1646:5: lv_key_0_0= 'default'
            {
            lv_key_0_0=(Token)match(input,38,FOLLOW_3); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					newLeafNode(lv_key_0_0, grammarAccess.getS_DefaultAccess().getKeyDefaultKeyword_0_0());
              				
            }
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getS_DefaultRule());
              					}
              					setWithLastConsumed(current, "key", lv_key_0_0, "default");
              				
            }

            }


            }

            // InternalGaml.g:1658:3: ( (lv_block_1_0= ruleBlock ) )
            // InternalGaml.g:1659:4: (lv_block_1_0= ruleBlock )
            {
            // InternalGaml.g:1659:4: (lv_block_1_0= ruleBlock )
            // InternalGaml.g:1660:5: lv_block_1_0= ruleBlock
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DefaultAccess().getBlockBlockParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_block_1_0=ruleBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getS_DefaultRule());
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
    // $ANTLR end "ruleS_Default"


    // $ANTLR start "entryRuleS_Return"
    // InternalGaml.g:1681:1: entryRuleS_Return returns [EObject current=null] : iv_ruleS_Return= ruleS_Return EOF ;
    public final EObject entryRuleS_Return() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Return = null;


        try {
            // InternalGaml.g:1681:49: (iv_ruleS_Return= ruleS_Return EOF )
            // InternalGaml.g:1682:2: iv_ruleS_Return= ruleS_Return EOF
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
    // InternalGaml.g:1688:1: ruleS_Return returns [EObject current=null] : ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) ;
    public final EObject ruleS_Return() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token otherlv_2=null;
        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:1694:2: ( ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' ) )
            // InternalGaml.g:1695:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            {
            // InternalGaml.g:1695:2: ( ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';' )
            // InternalGaml.g:1696:3: ( (lv_key_0_0= 'return' ) ) ( (lv_expr_1_0= ruleExpression ) )? otherlv_2= ';'
            {
            // InternalGaml.g:1696:3: ( (lv_key_0_0= 'return' ) )
            // InternalGaml.g:1697:4: (lv_key_0_0= 'return' )
            {
            // InternalGaml.g:1697:4: (lv_key_0_0= 'return' )
            // InternalGaml.g:1698:5: lv_key_0_0= 'return'
            {
            lv_key_0_0=(Token)match(input,39,FOLLOW_23); if (state.failed) return current;
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

            // InternalGaml.g:1710:3: ( (lv_expr_1_0= ruleExpression ) )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( ((LA23_0>=RULE_STRING && LA23_0<=RULE_KEYWORD)||LA23_0==21||LA23_0==38||LA23_0==42||LA23_0==46||(LA23_0>=51 && LA23_0<=75)||LA23_0==98||(LA23_0>=102 && LA23_0<=104)) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // InternalGaml.g:1711:4: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:1711:4: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:1712:5: lv_expr_1_0= ruleExpression
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
    // InternalGaml.g:1737:1: entryRuleS_Other returns [EObject current=null] : iv_ruleS_Other= ruleS_Other EOF ;
    public final EObject entryRuleS_Other() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Other = null;


        try {
            // InternalGaml.g:1737:48: (iv_ruleS_Other= ruleS_Other EOF )
            // InternalGaml.g:1738:2: iv_ruleS_Other= ruleS_Other EOF
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
    // InternalGaml.g:1744:1: ruleS_Other returns [EObject current=null] : ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) ;
    public final EObject ruleS_Other() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;

        EObject this_FacetsAndBlock_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:1750:2: ( ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) ) )
            // InternalGaml.g:1751:2: ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            {
            // InternalGaml.g:1751:2: ( ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1752:3: ( (lv_key_0_0= ruleValid_ID ) ) ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1752:3: ( (lv_key_0_0= ruleValid_ID ) )
            // InternalGaml.g:1753:4: (lv_key_0_0= ruleValid_ID )
            {
            // InternalGaml.g:1753:4: (lv_key_0_0= ruleValid_ID )
            // InternalGaml.g:1754:5: lv_key_0_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_OtherAccess().getKeyValid_IDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_25);
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

            // InternalGaml.g:1771:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )
            int alt24=2;
            alt24 = dfa24.predict(input);
            switch (alt24) {
                case 1 :
                    // InternalGaml.g:1772:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    {
                    // InternalGaml.g:1772:4: ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
                    // InternalGaml.g:1773:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    {
                    // InternalGaml.g:1782:5: ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
                    // InternalGaml.g:1783:6: ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
                    {
                    // InternalGaml.g:1783:6: ( (lv_expr_1_0= ruleExpression ) )
                    // InternalGaml.g:1784:7: (lv_expr_1_0= ruleExpression )
                    {
                    // InternalGaml.g:1784:7: (lv_expr_1_0= ruleExpression )
                    // InternalGaml.g:1785:8: lv_expr_1_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getS_OtherAccess().getExprExpressionParserRuleCall_1_0_0_0_0());
                      							
                    }
                    pushFollow(FOLLOW_6);
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
                    // InternalGaml.g:1816:4: this_FacetsAndBlock_3= ruleFacetsAndBlock[$current]
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
    // InternalGaml.g:1832:1: entryRuleS_Reflex returns [EObject current=null] : iv_ruleS_Reflex= ruleS_Reflex EOF ;
    public final EObject entryRuleS_Reflex() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Reflex = null;


        try {
            // InternalGaml.g:1832:49: (iv_ruleS_Reflex= ruleS_Reflex EOF )
            // InternalGaml.g:1833:2: iv_ruleS_Reflex= ruleS_Reflex EOF
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
    // InternalGaml.g:1839:1: ruleS_Reflex returns [EObject current=null] : ( ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Reflex() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_1=null;
        Token lv_key_0_2=null;
        AntlrDatatypeRuleToken lv_key_0_3 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:1845:2: ( ( ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1846:2: ( ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1846:2: ( ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1847:3: ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) ) ( (lv_name_1_0= ruleValid_ID ) )? this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1847:3: ( ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) ) )
            // InternalGaml.g:1848:4: ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) )
            {
            // InternalGaml.g:1848:4: ( (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init ) )
            // InternalGaml.g:1849:5: (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init )
            {
            // InternalGaml.g:1849:5: (lv_key_0_1= 'reflex' | lv_key_0_2= 'abort' | lv_key_0_3= ruleK_Init )
            int alt25=3;
            switch ( input.LA(1) ) {
            case 40:
                {
                alt25=1;
                }
                break;
            case 41:
                {
                alt25=2;
                }
                break;
            case 74:
                {
                alt25=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // InternalGaml.g:1850:6: lv_key_0_1= 'reflex'
                    {
                    lv_key_0_1=(Token)match(input,40,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_1, grammarAccess.getS_ReflexAccess().getKeyReflexKeyword_0_0_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_ReflexRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_1, null);
                      					
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:1861:6: lv_key_0_2= 'abort'
                    {
                    lv_key_0_2=(Token)match(input,41,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_key_0_2, grammarAccess.getS_ReflexAccess().getKeyAbortKeyword_0_0_1());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getS_ReflexRule());
                      						}
                      						setWithLastConsumed(current, "key", lv_key_0_2, null);
                      					
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:1872:6: lv_key_0_3= ruleK_Init
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_ReflexAccess().getKeyK_InitParserRuleCall_0_0_2());
                      					
                    }
                    pushFollow(FOLLOW_6);
                    lv_key_0_3=ruleK_Init();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getS_ReflexRule());
                      						}
                      						set(
                      							current,
                      							"key",
                      							lv_key_0_3,
                      							"gaml.compiler.Gaml.K_Init");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }
                    break;

            }


            }


            }

            // InternalGaml.g:1890:3: ( (lv_name_1_0= ruleValid_ID ) )?
            int alt26=2;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // InternalGaml.g:1891:4: (lv_name_1_0= ruleValid_ID )
                    {
                    // InternalGaml.g:1891:4: (lv_name_1_0= ruleValid_ID )
                    // InternalGaml.g:1892:5: lv_name_1_0= ruleValid_ID
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getS_ReflexAccess().getNameValid_IDParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_6);
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
    // InternalGaml.g:1924:1: entryRuleS_Definition returns [EObject current=null] : iv_ruleS_Definition= ruleS_Definition EOF ;
    public final EObject entryRuleS_Definition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Definition = null;


        try {
            // InternalGaml.g:1924:53: (iv_ruleS_Definition= ruleS_Definition EOF )
            // InternalGaml.g:1925:2: iv_ruleS_Definition= ruleS_Definition EOF
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
    // InternalGaml.g:1931:1: ruleS_Definition returns [EObject current=null] : ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Definition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_tkey_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject this_ActionArguments_3 = null;

        EObject this_FacetsAndBlock_5 = null;



        	enterRule();

        try {
            // InternalGaml.g:1937:2: ( ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:1938:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:1938:2: ( ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:1939:3: ( (lv_tkey_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )? this_FacetsAndBlock_5= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:1939:3: ( (lv_tkey_0_0= ruleTypeRef ) )
            // InternalGaml.g:1940:4: (lv_tkey_0_0= ruleTypeRef )
            {
            // InternalGaml.g:1940:4: (lv_tkey_0_0= ruleTypeRef )
            // InternalGaml.g:1941:5: lv_tkey_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_DefinitionAccess().getTkeyTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_9);
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

            // InternalGaml.g:1958:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:1959:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:1959:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:1960:5: lv_name_1_0= ruleValid_ID
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

            // InternalGaml.g:1977:3: (otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')' )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==42) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalGaml.g:1978:4: otherlv_2= '(' this_ActionArguments_3= ruleActionArguments[$current] otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,42,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getS_DefinitionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				if (current==null) {
                      					current = createModelElement(grammarAccess.getS_DefinitionRule());
                      				}
                      				newCompositeNode(grammarAccess.getS_DefinitionAccess().getActionArgumentsParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_27);
                    this_ActionArguments_3=ruleActionArguments(current);

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ActionArguments_3;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_4=(Token)match(input,43,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:2013:1: entryRuleS_Action returns [EObject current=null] : iv_ruleS_Action= ruleS_Action EOF ;
    public final EObject entryRuleS_Action() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Action = null;


        try {
            // InternalGaml.g:2013:49: (iv_ruleS_Action= ruleS_Action EOF )
            // InternalGaml.g:2014:2: iv_ruleS_Action= ruleS_Action EOF
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
    // InternalGaml.g:2020:1: ruleS_Action returns [EObject current=null] : ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Action() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        AntlrDatatypeRuleToken lv_name_2_0 = null;

        EObject this_ActionArguments_4 = null;

        EObject this_FacetsAndBlock_6 = null;



        	enterRule();

        try {
            // InternalGaml.g:2026:2: ( ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2027:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2027:2: ( () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2028:3: () ( (lv_key_1_0= 'action' ) ) ( (lv_name_2_0= ruleValid_ID ) ) (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )? this_FacetsAndBlock_6= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2028:3: ()
            // InternalGaml.g:2029:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getS_ActionAccess().getS_ActionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:2035:3: ( (lv_key_1_0= 'action' ) )
            // InternalGaml.g:2036:4: (lv_key_1_0= 'action' )
            {
            // InternalGaml.g:2036:4: (lv_key_1_0= 'action' )
            // InternalGaml.g:2037:5: lv_key_1_0= 'action'
            {
            lv_key_1_0=(Token)match(input,44,FOLLOW_9); if (state.failed) return current;
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

            // InternalGaml.g:2049:3: ( (lv_name_2_0= ruleValid_ID ) )
            // InternalGaml.g:2050:4: (lv_name_2_0= ruleValid_ID )
            {
            // InternalGaml.g:2050:4: (lv_name_2_0= ruleValid_ID )
            // InternalGaml.g:2051:5: lv_name_2_0= ruleValid_ID
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

            // InternalGaml.g:2068:3: (otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')' )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==42) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // InternalGaml.g:2069:4: otherlv_3= '(' this_ActionArguments_4= ruleActionArguments[$current] otherlv_5= ')'
                    {
                    otherlv_3=(Token)match(input,42,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getS_ActionAccess().getLeftParenthesisKeyword_3_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				if (current==null) {
                      					current = createModelElement(grammarAccess.getS_ActionRule());
                      				}
                      				newCompositeNode(grammarAccess.getS_ActionAccess().getActionArgumentsParserRuleCall_3_1());
                      			
                    }
                    pushFollow(FOLLOW_27);
                    this_ActionArguments_4=ruleActionArguments(current);

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ActionArguments_4;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_5=(Token)match(input,43,FOLLOW_6); if (state.failed) return current;
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
    // InternalGaml.g:2104:1: entryRuleS_Assignment returns [EObject current=null] : iv_ruleS_Assignment= ruleS_Assignment EOF ;
    public final EObject entryRuleS_Assignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Assignment = null;


        try {
            // InternalGaml.g:2104:53: (iv_ruleS_Assignment= ruleS_Assignment EOF )
            // InternalGaml.g:2105:2: iv_ruleS_Assignment= ruleS_Assignment EOF
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
    // InternalGaml.g:2111:1: ruleS_Assignment returns [EObject current=null] : ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* otherlv_4= ';' ) ;
    public final EObject ruleS_Assignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_4=null;
        EObject lv_expr_0_0 = null;

        AntlrDatatypeRuleToken lv_key_1_0 = null;

        EObject lv_value_2_0 = null;

        EObject lv_facets_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2117:2: ( ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* otherlv_4= ';' ) )
            // InternalGaml.g:2118:2: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* otherlv_4= ';' )
            {
            // InternalGaml.g:2118:2: ( ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* otherlv_4= ';' )
            // InternalGaml.g:2119:3: ( (lv_expr_0_0= ruleExpression ) ) ( (lv_key_1_0= ruleK_Assignment ) ) ( (lv_value_2_0= ruleExpression ) ) ( (lv_facets_3_0= ruleFacet ) )* otherlv_4= ';'
            {
            // InternalGaml.g:2119:3: ( (lv_expr_0_0= ruleExpression ) )
            // InternalGaml.g:2120:4: (lv_expr_0_0= ruleExpression )
            {
            // InternalGaml.g:2120:4: (lv_expr_0_0= ruleExpression )
            // InternalGaml.g:2121:5: lv_expr_0_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_AssignmentAccess().getExprExpressionParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_28);
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

            // InternalGaml.g:2138:3: ( (lv_key_1_0= ruleK_Assignment ) )
            // InternalGaml.g:2139:4: (lv_key_1_0= ruleK_Assignment )
            {
            // InternalGaml.g:2139:4: (lv_key_1_0= ruleK_Assignment )
            // InternalGaml.g:2140:5: lv_key_1_0= ruleK_Assignment
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_AssignmentAccess().getKeyK_AssignmentParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_4);
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

            // InternalGaml.g:2157:3: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2158:4: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2158:4: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2159:5: lv_value_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_AssignmentAccess().getValueExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_29);
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

            // InternalGaml.g:2176:3: ( (lv_facets_3_0= ruleFacet ) )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==RULE_ID||LA29_0==38||(LA29_0>=51 && LA29_0<=76)||(LA29_0>=84 && LA29_0<=88)) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // InternalGaml.g:2177:4: (lv_facets_3_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2177:4: (lv_facets_3_0= ruleFacet )
            	    // InternalGaml.g:2178:5: lv_facets_3_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_AssignmentAccess().getFacetsFacetParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_29);
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
            	    break loop29;
                }
            } while (true);

            otherlv_4=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getS_AssignmentAccess().getSemicolonKeyword_4());
              		
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
    // InternalGaml.g:2203:1: entryRuleS_Equations returns [EObject current=null] : iv_ruleS_Equations= ruleS_Equations EOF ;
    public final EObject entryRuleS_Equations() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equations = null;


        try {
            // InternalGaml.g:2203:52: (iv_ruleS_Equations= ruleS_Equations EOF )
            // InternalGaml.g:2204:2: iv_ruleS_Equations= ruleS_Equations EOF
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
    // InternalGaml.g:2210:1: ruleS_Equations returns [EObject current=null] : ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) ;
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
            // InternalGaml.g:2216:2: ( ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) ) )
            // InternalGaml.g:2217:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            {
            // InternalGaml.g:2217:2: ( ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' ) )
            // InternalGaml.g:2218:3: ( (lv_key_0_0= 'equation' ) ) ( (lv_name_1_0= ruleValid_ID ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            {
            // InternalGaml.g:2218:3: ( (lv_key_0_0= 'equation' ) )
            // InternalGaml.g:2219:4: (lv_key_0_0= 'equation' )
            {
            // InternalGaml.g:2219:4: (lv_key_0_0= 'equation' )
            // InternalGaml.g:2220:5: lv_key_0_0= 'equation'
            {
            lv_key_0_0=(Token)match(input,45,FOLLOW_9); if (state.failed) return current;
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

            // InternalGaml.g:2232:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:2233:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:2233:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:2234:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_EquationsAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_16);
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

            // InternalGaml.g:2251:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==RULE_ID||LA30_0==38||(LA30_0>=51 && LA30_0<=76)||(LA30_0>=84 && LA30_0<=88)) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // InternalGaml.g:2252:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2252:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2253:5: lv_facets_2_0= ruleFacet
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getS_EquationsAccess().getFacetsFacetParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_16);
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
            	    break loop30;
                }
            } while (true);

            // InternalGaml.g:2270:3: ( (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' ) | otherlv_7= ';' )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==46) ) {
                alt32=1;
            }
            else if ( (LA32_0==23) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // InternalGaml.g:2271:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    {
                    // InternalGaml.g:2271:4: (otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}' )
                    // InternalGaml.g:2272:5: otherlv_3= '{' ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )* otherlv_6= '}'
                    {
                    otherlv_3=(Token)match(input,46,FOLLOW_30); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0());
                      				
                    }
                    // InternalGaml.g:2276:5: ( ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';' )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==RULE_ID||LA31_0==38||(LA31_0>=51 && LA31_0<=75)) ) {
                            alt31=1;
                        }


                        switch (alt31) {
                    	case 1 :
                    	    // InternalGaml.g:2277:6: ( (lv_equations_4_0= ruleS_Equation ) ) otherlv_5= ';'
                    	    {
                    	    // InternalGaml.g:2277:6: ( (lv_equations_4_0= ruleS_Equation ) )
                    	    // InternalGaml.g:2278:7: (lv_equations_4_0= ruleS_Equation )
                    	    {
                    	    // InternalGaml.g:2278:7: (lv_equations_4_0= ruleS_Equation )
                    	    // InternalGaml.g:2279:8: lv_equations_4_0= ruleS_Equation
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

                    	    otherlv_5=(Token)match(input,23,FOLLOW_30); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						newLeafNode(otherlv_5, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_0_1_1());
                    	      					
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);

                    otherlv_6=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_6, grammarAccess.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2());
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:2307:4: otherlv_7= ';'
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
    // InternalGaml.g:2316:1: entryRuleS_Equation returns [EObject current=null] : iv_ruleS_Equation= ruleS_Equation EOF ;
    public final EObject entryRuleS_Equation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Equation = null;


        try {
            // InternalGaml.g:2316:51: (iv_ruleS_Equation= ruleS_Equation EOF )
            // InternalGaml.g:2317:2: iv_ruleS_Equation= ruleS_Equation EOF
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
    // InternalGaml.g:2323:1: ruleS_Equation returns [EObject current=null] : ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) ;
    public final EObject ruleS_Equation() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        EObject lv_expr_0_1 = null;

        EObject lv_expr_0_2 = null;

        EObject lv_value_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2329:2: ( ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:2330:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:2330:2: ( ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) ) )
            // InternalGaml.g:2331:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) ) ( (lv_key_1_0= '=' ) ) ( (lv_value_2_0= ruleExpression ) )
            {
            // InternalGaml.g:2331:3: ( ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) ) )
            // InternalGaml.g:2332:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            {
            // InternalGaml.g:2332:4: ( (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef ) )
            // InternalGaml.g:2333:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            {
            // InternalGaml.g:2333:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )
            int alt33=2;
            alt33 = dfa33.predict(input);
            switch (alt33) {
                case 1 :
                    // InternalGaml.g:2334:6: lv_expr_0_1= ruleFunction
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprFunctionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_31);
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
                    // InternalGaml.g:2350:6: lv_expr_0_2= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getS_EquationAccess().getExprVariableRefParserRuleCall_0_0_1());
                      					
                    }
                    pushFollow(FOLLOW_31);
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

            // InternalGaml.g:2368:3: ( (lv_key_1_0= '=' ) )
            // InternalGaml.g:2369:4: (lv_key_1_0= '=' )
            {
            // InternalGaml.g:2369:4: (lv_key_1_0= '=' )
            // InternalGaml.g:2370:5: lv_key_1_0= '='
            {
            lv_key_1_0=(Token)match(input,48,FOLLOW_4); if (state.failed) return current;
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

            // InternalGaml.g:2382:3: ( (lv_value_2_0= ruleExpression ) )
            // InternalGaml.g:2383:4: (lv_value_2_0= ruleExpression )
            {
            // InternalGaml.g:2383:4: (lv_value_2_0= ruleExpression )
            // InternalGaml.g:2384:5: lv_value_2_0= ruleExpression
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
    // InternalGaml.g:2405:1: entryRuleS_Solve returns [EObject current=null] : iv_ruleS_Solve= ruleS_Solve EOF ;
    public final EObject entryRuleS_Solve() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Solve = null;


        try {
            // InternalGaml.g:2405:48: (iv_ruleS_Solve= ruleS_Solve EOF )
            // InternalGaml.g:2406:2: iv_ruleS_Solve= ruleS_Solve EOF
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
    // InternalGaml.g:2412:1: ruleS_Solve returns [EObject current=null] : ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ;
    public final EObject ruleS_Solve() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;

        EObject this_FacetsAndBlock_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:2418:2: ( ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) )
            // InternalGaml.g:2419:2: ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            {
            // InternalGaml.g:2419:2: ( ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] )
            // InternalGaml.g:2420:3: ( (lv_key_0_0= 'solve' ) ) ( (lv_expr_1_0= ruleEquationRef ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current]
            {
            // InternalGaml.g:2420:3: ( (lv_key_0_0= 'solve' ) )
            // InternalGaml.g:2421:4: (lv_key_0_0= 'solve' )
            {
            // InternalGaml.g:2421:4: (lv_key_0_0= 'solve' )
            // InternalGaml.g:2422:5: lv_key_0_0= 'solve'
            {
            lv_key_0_0=(Token)match(input,49,FOLLOW_9); if (state.failed) return current;
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

            // InternalGaml.g:2434:3: ( (lv_expr_1_0= ruleEquationRef ) )
            // InternalGaml.g:2435:4: (lv_expr_1_0= ruleEquationRef )
            {
            // InternalGaml.g:2435:4: (lv_expr_1_0= ruleEquationRef )
            // InternalGaml.g:2436:5: lv_expr_1_0= ruleEquationRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getS_SolveAccess().getExprEquationRefParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_6);
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
    // InternalGaml.g:2468:1: entryRuleS_Display returns [EObject current=null] : iv_ruleS_Display= ruleS_Display EOF ;
    public final EObject entryRuleS_Display() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleS_Display = null;


        try {
            // InternalGaml.g:2468:50: (iv_ruleS_Display= ruleS_Display EOF )
            // InternalGaml.g:2469:2: iv_ruleS_Display= ruleS_Display EOF
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
    // InternalGaml.g:2475:1: ruleS_Display returns [EObject current=null] : ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) ;
    public final EObject ruleS_Display() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        Token lv_name_1_2=null;
        AntlrDatatypeRuleToken lv_name_1_1 = null;

        EObject lv_facets_2_0 = null;

        EObject lv_block_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2481:2: ( ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) ) )
            // InternalGaml.g:2482:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            {
            // InternalGaml.g:2482:2: ( ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) ) )
            // InternalGaml.g:2483:3: ( (lv_key_0_0= 'display' ) ) ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) ) ( (lv_facets_2_0= ruleFacet ) )* ( (lv_block_3_0= ruleDisplayBlock ) )
            {
            // InternalGaml.g:2483:3: ( (lv_key_0_0= 'display' ) )
            // InternalGaml.g:2484:4: (lv_key_0_0= 'display' )
            {
            // InternalGaml.g:2484:4: (lv_key_0_0= 'display' )
            // InternalGaml.g:2485:5: lv_key_0_0= 'display'
            {
            lv_key_0_0=(Token)match(input,50,FOLLOW_5); if (state.failed) return current;
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

            // InternalGaml.g:2497:3: ( ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) ) )
            // InternalGaml.g:2498:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            {
            // InternalGaml.g:2498:4: ( (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING ) )
            // InternalGaml.g:2499:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            {
            // InternalGaml.g:2499:5: (lv_name_1_1= ruleValid_ID | lv_name_1_2= RULE_STRING )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==RULE_ID||LA34_0==38||(LA34_0>=51 && LA34_0<=75)) ) {
                alt34=1;
            }
            else if ( (LA34_0==RULE_STRING) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // InternalGaml.g:2500:6: lv_name_1_1= ruleValid_ID
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
                    // InternalGaml.g:2516:6: lv_name_1_2= RULE_STRING
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

            // InternalGaml.g:2533:3: ( (lv_facets_2_0= ruleFacet ) )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==RULE_ID||LA35_0==38||(LA35_0>=51 && LA35_0<=76)||(LA35_0>=84 && LA35_0<=88)) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // InternalGaml.g:2534:4: (lv_facets_2_0= ruleFacet )
            	    {
            	    // InternalGaml.g:2534:4: (lv_facets_2_0= ruleFacet )
            	    // InternalGaml.g:2535:5: lv_facets_2_0= ruleFacet
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
            	    break loop35;
                }
            } while (true);

            // InternalGaml.g:2552:3: ( (lv_block_3_0= ruleDisplayBlock ) )
            // InternalGaml.g:2553:4: (lv_block_3_0= ruleDisplayBlock )
            {
            // InternalGaml.g:2553:4: (lv_block_3_0= ruleDisplayBlock )
            // InternalGaml.g:2554:5: lv_block_3_0= ruleDisplayBlock
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


    // $ANTLR start "entryRuleK_BuiltIn"
    // InternalGaml.g:2575:1: entryRuleK_BuiltIn returns [String current=null] : iv_ruleK_BuiltIn= ruleK_BuiltIn EOF ;
    public final String entryRuleK_BuiltIn() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_BuiltIn = null;


        try {
            // InternalGaml.g:2575:49: (iv_ruleK_BuiltIn= ruleK_BuiltIn EOF )
            // InternalGaml.g:2576:2: iv_ruleK_BuiltIn= ruleK_BuiltIn EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_BuiltInRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_BuiltIn=ruleK_BuiltIn();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_BuiltIn.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleK_BuiltIn"


    // $ANTLR start "ruleK_BuiltIn"
    // InternalGaml.g:2582:1: ruleK_BuiltIn returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'ask' | kw= 'text' | kw= 'assert' | kw= 'setup' | kw= 'add' | kw= 'remove' | kw= 'put' | kw= 'capture' | kw= 'release' | kw= 'migrate' | kw= 'create' | kw= 'error' | kw= 'warn' | kw= 'write' | kw= 'status' | kw= 'focus_on' | kw= 'highlight' | kw= 'layout' | kw= 'save' | kw= 'restore' | kw= 'diffuse' | kw= 'default' ) ;
    public final AntlrDatatypeRuleToken ruleK_BuiltIn() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2588:2: ( (kw= 'ask' | kw= 'text' | kw= 'assert' | kw= 'setup' | kw= 'add' | kw= 'remove' | kw= 'put' | kw= 'capture' | kw= 'release' | kw= 'migrate' | kw= 'create' | kw= 'error' | kw= 'warn' | kw= 'write' | kw= 'status' | kw= 'focus_on' | kw= 'highlight' | kw= 'layout' | kw= 'save' | kw= 'restore' | kw= 'diffuse' | kw= 'default' ) )
            // InternalGaml.g:2589:2: (kw= 'ask' | kw= 'text' | kw= 'assert' | kw= 'setup' | kw= 'add' | kw= 'remove' | kw= 'put' | kw= 'capture' | kw= 'release' | kw= 'migrate' | kw= 'create' | kw= 'error' | kw= 'warn' | kw= 'write' | kw= 'status' | kw= 'focus_on' | kw= 'highlight' | kw= 'layout' | kw= 'save' | kw= 'restore' | kw= 'diffuse' | kw= 'default' )
            {
            // InternalGaml.g:2589:2: (kw= 'ask' | kw= 'text' | kw= 'assert' | kw= 'setup' | kw= 'add' | kw= 'remove' | kw= 'put' | kw= 'capture' | kw= 'release' | kw= 'migrate' | kw= 'create' | kw= 'error' | kw= 'warn' | kw= 'write' | kw= 'status' | kw= 'focus_on' | kw= 'highlight' | kw= 'layout' | kw= 'save' | kw= 'restore' | kw= 'diffuse' | kw= 'default' )
            int alt36=22;
            switch ( input.LA(1) ) {
            case 51:
                {
                alt36=1;
                }
                break;
            case 52:
                {
                alt36=2;
                }
                break;
            case 53:
                {
                alt36=3;
                }
                break;
            case 54:
                {
                alt36=4;
                }
                break;
            case 55:
                {
                alt36=5;
                }
                break;
            case 56:
                {
                alt36=6;
                }
                break;
            case 57:
                {
                alt36=7;
                }
                break;
            case 58:
                {
                alt36=8;
                }
                break;
            case 59:
                {
                alt36=9;
                }
                break;
            case 60:
                {
                alt36=10;
                }
                break;
            case 61:
                {
                alt36=11;
                }
                break;
            case 62:
                {
                alt36=12;
                }
                break;
            case 63:
                {
                alt36=13;
                }
                break;
            case 64:
                {
                alt36=14;
                }
                break;
            case 65:
                {
                alt36=15;
                }
                break;
            case 66:
                {
                alt36=16;
                }
                break;
            case 67:
                {
                alt36=17;
                }
                break;
            case 68:
                {
                alt36=18;
                }
                break;
            case 69:
                {
                alt36=19;
                }
                break;
            case 70:
                {
                alt36=20;
                }
                break;
            case 71:
                {
                alt36=21;
                }
                break;
            case 38:
                {
                alt36=22;
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
                    // InternalGaml.g:2590:3: kw= 'ask'
                    {
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getAskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2596:3: kw= 'text'
                    {
                    kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getTextKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2602:3: kw= 'assert'
                    {
                    kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getAssertKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:2608:3: kw= 'setup'
                    {
                    kw=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getSetupKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2614:3: kw= 'add'
                    {
                    kw=(Token)match(input,55,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getAddKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:2620:3: kw= 'remove'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getRemoveKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:2626:3: kw= 'put'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getPutKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:2632:3: kw= 'capture'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getCaptureKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalGaml.g:2638:3: kw= 'release'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getReleaseKeyword_8());
                      		
                    }

                    }
                    break;
                case 10 :
                    // InternalGaml.g:2644:3: kw= 'migrate'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getMigrateKeyword_9());
                      		
                    }

                    }
                    break;
                case 11 :
                    // InternalGaml.g:2650:3: kw= 'create'
                    {
                    kw=(Token)match(input,61,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getCreateKeyword_10());
                      		
                    }

                    }
                    break;
                case 12 :
                    // InternalGaml.g:2656:3: kw= 'error'
                    {
                    kw=(Token)match(input,62,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getErrorKeyword_11());
                      		
                    }

                    }
                    break;
                case 13 :
                    // InternalGaml.g:2662:3: kw= 'warn'
                    {
                    kw=(Token)match(input,63,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getWarnKeyword_12());
                      		
                    }

                    }
                    break;
                case 14 :
                    // InternalGaml.g:2668:3: kw= 'write'
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getWriteKeyword_13());
                      		
                    }

                    }
                    break;
                case 15 :
                    // InternalGaml.g:2674:3: kw= 'status'
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getStatusKeyword_14());
                      		
                    }

                    }
                    break;
                case 16 :
                    // InternalGaml.g:2680:3: kw= 'focus_on'
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getFocus_onKeyword_15());
                      		
                    }

                    }
                    break;
                case 17 :
                    // InternalGaml.g:2686:3: kw= 'highlight'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getHighlightKeyword_16());
                      		
                    }

                    }
                    break;
                case 18 :
                    // InternalGaml.g:2692:3: kw= 'layout'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getLayoutKeyword_17());
                      		
                    }

                    }
                    break;
                case 19 :
                    // InternalGaml.g:2698:3: kw= 'save'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getSaveKeyword_18());
                      		
                    }

                    }
                    break;
                case 20 :
                    // InternalGaml.g:2704:3: kw= 'restore'
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getRestoreKeyword_19());
                      		
                    }

                    }
                    break;
                case 21 :
                    // InternalGaml.g:2710:3: kw= 'diffuse'
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getDiffuseKeyword_20());
                      		
                    }

                    }
                    break;
                case 22 :
                    // InternalGaml.g:2716:3: kw= 'default'
                    {
                    kw=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_BuiltInAccess().getDefaultKeyword_21());
                      		
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
    // $ANTLR end "ruleK_BuiltIn"


    // $ANTLR start "entryRuleK_Species"
    // InternalGaml.g:2725:1: entryRuleK_Species returns [String current=null] : iv_ruleK_Species= ruleK_Species EOF ;
    public final String entryRuleK_Species() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Species = null;


        try {
            // InternalGaml.g:2725:49: (iv_ruleK_Species= ruleK_Species EOF )
            // InternalGaml.g:2726:2: iv_ruleK_Species= ruleK_Species EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_SpeciesRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Species=ruleK_Species();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Species.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleK_Species"


    // $ANTLR start "ruleK_Species"
    // InternalGaml.g:2732:1: ruleK_Species returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'species' ;
    public final AntlrDatatypeRuleToken ruleK_Species() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2738:2: (kw= 'species' )
            // InternalGaml.g:2739:2: kw= 'species'
            {
            kw=(Token)match(input,72,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_SpeciesAccess().getSpeciesKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleK_Species"


    // $ANTLR start "entryRuleK_Grid"
    // InternalGaml.g:2747:1: entryRuleK_Grid returns [String current=null] : iv_ruleK_Grid= ruleK_Grid EOF ;
    public final String entryRuleK_Grid() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Grid = null;


        try {
            // InternalGaml.g:2747:46: (iv_ruleK_Grid= ruleK_Grid EOF )
            // InternalGaml.g:2748:2: iv_ruleK_Grid= ruleK_Grid EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_GridRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Grid=ruleK_Grid();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Grid.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleK_Grid"


    // $ANTLR start "ruleK_Grid"
    // InternalGaml.g:2754:1: ruleK_Grid returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'grid' ;
    public final AntlrDatatypeRuleToken ruleK_Grid() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2760:2: (kw= 'grid' )
            // InternalGaml.g:2761:2: kw= 'grid'
            {
            kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_GridAccess().getGridKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleK_Grid"


    // $ANTLR start "entryRuleK_Init"
    // InternalGaml.g:2769:1: entryRuleK_Init returns [String current=null] : iv_ruleK_Init= ruleK_Init EOF ;
    public final String entryRuleK_Init() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Init = null;


        try {
            // InternalGaml.g:2769:46: (iv_ruleK_Init= ruleK_Init EOF )
            // InternalGaml.g:2770:2: iv_ruleK_Init= ruleK_Init EOF
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
    // InternalGaml.g:2776:1: ruleK_Init returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'init' ;
    public final AntlrDatatypeRuleToken ruleK_Init() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2782:2: (kw= 'init' )
            // InternalGaml.g:2783:2: kw= 'init'
            {
            kw=(Token)match(input,74,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleK_Experiment"
    // InternalGaml.g:2791:1: entryRuleK_Experiment returns [String current=null] : iv_ruleK_Experiment= ruleK_Experiment EOF ;
    public final String entryRuleK_Experiment() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Experiment = null;


        try {
            // InternalGaml.g:2791:52: (iv_ruleK_Experiment= ruleK_Experiment EOF )
            // InternalGaml.g:2792:2: iv_ruleK_Experiment= ruleK_Experiment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getK_ExperimentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleK_Experiment=ruleK_Experiment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleK_Experiment.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleK_Experiment"


    // $ANTLR start "ruleK_Experiment"
    // InternalGaml.g:2798:1: ruleK_Experiment returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'experiment' ;
    public final AntlrDatatypeRuleToken ruleK_Experiment() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2804:2: (kw= 'experiment' )
            // InternalGaml.g:2805:2: kw= 'experiment'
            {
            kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getK_ExperimentAccess().getExperimentKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleK_Experiment"


    // $ANTLR start "entryRuleK_Assignment"
    // InternalGaml.g:2813:1: entryRuleK_Assignment returns [String current=null] : iv_ruleK_Assignment= ruleK_Assignment EOF ;
    public final String entryRuleK_Assignment() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleK_Assignment = null;


        try {
            // InternalGaml.g:2813:52: (iv_ruleK_Assignment= ruleK_Assignment EOF )
            // InternalGaml.g:2814:2: iv_ruleK_Assignment= ruleK_Assignment EOF
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
    // InternalGaml.g:2820:1: ruleK_Assignment returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) ;
    public final AntlrDatatypeRuleToken ruleK_Assignment() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:2826:2: ( (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' ) )
            // InternalGaml.g:2827:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            {
            // InternalGaml.g:2827:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )
            int alt37=8;
            alt37 = dfa37.predict(input);
            switch (alt37) {
                case 1 :
                    // InternalGaml.g:2828:3: kw= '<-'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignHyphenMinusKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:2834:3: kw= '<<'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:2840:3: (kw= '>' kw= '>' )
                    {
                    // InternalGaml.g:2840:3: (kw= '>' kw= '>' )
                    // InternalGaml.g:2841:4: kw= '>' kw= '>'
                    {
                    kw=(Token)match(input,78,FOLLOW_32); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_0());
                      			
                    }
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:2853:3: kw= '<<+'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignLessThanSignPlusSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:2859:3: (kw= '>' kw= '>-' )
                    {
                    // InternalGaml.g:2859:3: (kw= '>' kw= '>-' )
                    // InternalGaml.g:2860:4: kw= '>' kw= '>-'
                    {
                    kw=(Token)match(input,78,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignKeyword_4_0());
                      			
                    }
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getGreaterThanSignHyphenMinusKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalGaml.g:2872:3: kw= '+<-'
                    {
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getPlusSignLessThanSignHyphenMinusKeyword_5());
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalGaml.g:2878:3: kw= '<+'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getK_AssignmentAccess().getLessThanSignPlusSignKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalGaml.g:2884:3: kw= '>-'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleArgumentDefinition"
    // InternalGaml.g:2893:1: entryRuleArgumentDefinition returns [EObject current=null] : iv_ruleArgumentDefinition= ruleArgumentDefinition EOF ;
    public final EObject entryRuleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArgumentDefinition = null;


        try {
            // InternalGaml.g:2893:59: (iv_ruleArgumentDefinition= ruleArgumentDefinition EOF )
            // InternalGaml.g:2894:2: iv_ruleArgumentDefinition= ruleArgumentDefinition EOF
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
    // InternalGaml.g:2900:1: ruleArgumentDefinition returns [EObject current=null] : ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) ;
    public final EObject ruleArgumentDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject lv_type_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_default_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:2906:2: ( ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? ) )
            // InternalGaml.g:2907:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            {
            // InternalGaml.g:2907:2: ( ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )? )
            // InternalGaml.g:2908:3: ( (lv_type_0_0= ruleTypeRef ) ) ( (lv_name_1_0= ruleValid_ID ) ) (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            {
            // InternalGaml.g:2908:3: ( (lv_type_0_0= ruleTypeRef ) )
            // InternalGaml.g:2909:4: (lv_type_0_0= ruleTypeRef )
            {
            // InternalGaml.g:2909:4: (lv_type_0_0= ruleTypeRef )
            // InternalGaml.g:2910:5: lv_type_0_0= ruleTypeRef
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getTypeTypeRefParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_9);
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

            // InternalGaml.g:2927:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:2928:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:2928:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:2929:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getArgumentDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_34);
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

            // InternalGaml.g:2946:3: (otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) ) )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==76) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // InternalGaml.g:2947:4: otherlv_2= '<-' ( (lv_default_3_0= ruleExpression ) )
                    {
                    otherlv_2=(Token)match(input,76,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getArgumentDefinitionAccess().getLessThanSignHyphenMinusKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:2951:4: ( (lv_default_3_0= ruleExpression ) )
                    // InternalGaml.g:2952:5: (lv_default_3_0= ruleExpression )
                    {
                    // InternalGaml.g:2952:5: (lv_default_3_0= ruleExpression )
                    // InternalGaml.g:2953:6: lv_default_3_0= ruleExpression
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
    // InternalGaml.g:2975:1: entryRuleFacet returns [EObject current=null] : iv_ruleFacet= ruleFacet EOF ;
    public final EObject entryRuleFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFacet = null;


        try {
            // InternalGaml.g:2975:46: (iv_ruleFacet= ruleFacet EOF )
            // InternalGaml.g:2976:2: iv_ruleFacet= ruleFacet EOF
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
    // InternalGaml.g:2982:1: ruleFacet returns [EObject current=null] : (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_FunctionFacet_3= ruleFunctionFacet ) ;
    public final EObject ruleFacet() throws RecognitionException {
        EObject current = null;

        EObject this_ActionFacet_0 = null;

        EObject this_DefinitionFacet_1 = null;

        EObject this_ClassicFacet_2 = null;

        EObject this_FunctionFacet_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:2988:2: ( (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_FunctionFacet_3= ruleFunctionFacet ) )
            // InternalGaml.g:2989:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_FunctionFacet_3= ruleFunctionFacet )
            {
            // InternalGaml.g:2989:2: (this_ActionFacet_0= ruleActionFacet | this_DefinitionFacet_1= ruleDefinitionFacet | this_ClassicFacet_2= ruleClassicFacet | this_FunctionFacet_3= ruleFunctionFacet )
            int alt39=4;
            switch ( input.LA(1) ) {
            case 86:
            case 87:
                {
                alt39=1;
                }
                break;
            case 85:
                {
                alt39=2;
                }
                break;
            case RULE_ID:
            case 38:
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
            case 84:
                {
                alt39=3;
                }
                break;
            case 88:
                {
                alt39=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // InternalGaml.g:2990:3: this_ActionFacet_0= ruleActionFacet
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
                    // InternalGaml.g:2999:3: this_DefinitionFacet_1= ruleDefinitionFacet
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
                    // InternalGaml.g:3008:3: this_ClassicFacet_2= ruleClassicFacet
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
                    // InternalGaml.g:3017:3: this_FunctionFacet_3= ruleFunctionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getFacetAccess().getFunctionFacetParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_FunctionFacet_3=ruleFunctionFacet();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_FunctionFacet_3;
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
    // InternalGaml.g:3029:1: entryRuleClassicFacetKey returns [String current=null] : iv_ruleClassicFacetKey= ruleClassicFacetKey EOF ;
    public final String entryRuleClassicFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleClassicFacetKey = null;


        try {
            // InternalGaml.g:3029:55: (iv_ruleClassicFacetKey= ruleClassicFacetKey EOF )
            // InternalGaml.g:3030:2: iv_ruleClassicFacetKey= ruleClassicFacetKey EOF
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
    // InternalGaml.g:3036:1: ruleClassicFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (this_Valid_ID_0= ruleValid_ID kw= ':' ) | kw= 'as:' ) ;
    public final AntlrDatatypeRuleToken ruleClassicFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_Valid_ID_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3042:2: ( ( (this_Valid_ID_0= ruleValid_ID kw= ':' ) | kw= 'as:' ) )
            // InternalGaml.g:3043:2: ( (this_Valid_ID_0= ruleValid_ID kw= ':' ) | kw= 'as:' )
            {
            // InternalGaml.g:3043:2: ( (this_Valid_ID_0= ruleValid_ID kw= ':' ) | kw= 'as:' )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==RULE_ID||LA40_0==38||(LA40_0>=51 && LA40_0<=75)) ) {
                alt40=1;
            }
            else if ( (LA40_0==84) ) {
                alt40=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // InternalGaml.g:3044:3: (this_Valid_ID_0= ruleValid_ID kw= ':' )
                    {
                    // InternalGaml.g:3044:3: (this_Valid_ID_0= ruleValid_ID kw= ':' )
                    // InternalGaml.g:3045:4: this_Valid_ID_0= ruleValid_ID kw= ':'
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getClassicFacetKeyAccess().getValid_IDParserRuleCall_0_0());
                      			
                    }
                    pushFollow(FOLLOW_35);
                    this_Valid_ID_0=ruleValid_ID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(this_Valid_ID_0);
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getClassicFacetKeyAccess().getColonKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:3062:3: kw= 'as:'
                    {
                    kw=(Token)match(input,84,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getClassicFacetKeyAccess().getAsKeyword_1());
                      		
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
    // $ANTLR end "ruleClassicFacetKey"


    // $ANTLR start "entryRuleDefinitionFacetKey"
    // InternalGaml.g:3071:1: entryRuleDefinitionFacetKey returns [String current=null] : iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF ;
    public final String entryRuleDefinitionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleDefinitionFacetKey = null;


        try {
            // InternalGaml.g:3071:58: (iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF )
            // InternalGaml.g:3072:2: iv_ruleDefinitionFacetKey= ruleDefinitionFacetKey EOF
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
    // InternalGaml.g:3078:1: ruleDefinitionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= 'returns:' ;
    public final AntlrDatatypeRuleToken ruleDefinitionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3084:2: (kw= 'returns:' )
            // InternalGaml.g:3085:2: kw= 'returns:'
            {
            kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getDefinitionFacetKeyAccess().getReturnsKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
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


    // $ANTLR start "entryRuleActionFacetKey"
    // InternalGaml.g:3093:1: entryRuleActionFacetKey returns [String current=null] : iv_ruleActionFacetKey= ruleActionFacetKey EOF ;
    public final String entryRuleActionFacetKey() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleActionFacetKey = null;


        try {
            // InternalGaml.g:3093:54: (iv_ruleActionFacetKey= ruleActionFacetKey EOF )
            // InternalGaml.g:3094:2: iv_ruleActionFacetKey= ruleActionFacetKey EOF
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
    // InternalGaml.g:3100:1: ruleActionFacetKey returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'action:' | kw= 'on_change:' ) ;
    public final AntlrDatatypeRuleToken ruleActionFacetKey() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalGaml.g:3106:2: ( (kw= 'action:' | kw= 'on_change:' ) )
            // InternalGaml.g:3107:2: (kw= 'action:' | kw= 'on_change:' )
            {
            // InternalGaml.g:3107:2: (kw= 'action:' | kw= 'on_change:' )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==86) ) {
                alt41=1;
            }
            else if ( (LA41_0==87) ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // InternalGaml.g:3108:3: kw= 'action:'
                    {
                    kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getActionFacetKeyAccess().getActionKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:3114:3: kw= 'on_change:'
                    {
                    kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleClassicFacet"
    // InternalGaml.g:3123:1: entryRuleClassicFacet returns [EObject current=null] : iv_ruleClassicFacet= ruleClassicFacet EOF ;
    public final EObject entryRuleClassicFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassicFacet = null;


        try {
            // InternalGaml.g:3123:53: (iv_ruleClassicFacet= ruleClassicFacet EOF )
            // InternalGaml.g:3124:2: iv_ruleClassicFacet= ruleClassicFacet EOF
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
    // InternalGaml.g:3130:1: ruleClassicFacet returns [EObject current=null] : ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) ) ( (lv_expr_2_0= ruleExpression ) ) ) ;
    public final EObject ruleClassicFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_1_0=null;
        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3136:2: ( ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) ) ( (lv_expr_2_0= ruleExpression ) ) ) )
            // InternalGaml.g:3137:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) ) ( (lv_expr_2_0= ruleExpression ) ) )
            {
            // InternalGaml.g:3137:2: ( ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) ) ( (lv_expr_2_0= ruleExpression ) ) )
            // InternalGaml.g:3138:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) ) ( (lv_expr_2_0= ruleExpression ) )
            {
            // InternalGaml.g:3138:3: ( ( (lv_key_0_0= ruleClassicFacetKey ) ) | ( (lv_key_1_0= '<-' ) ) )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==RULE_ID||LA42_0==38||(LA42_0>=51 && LA42_0<=75)||LA42_0==84) ) {
                alt42=1;
            }
            else if ( (LA42_0==76) ) {
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
                    // InternalGaml.g:3139:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    {
                    // InternalGaml.g:3139:4: ( (lv_key_0_0= ruleClassicFacetKey ) )
                    // InternalGaml.g:3140:5: (lv_key_0_0= ruleClassicFacetKey )
                    {
                    // InternalGaml.g:3140:5: (lv_key_0_0= ruleClassicFacetKey )
                    // InternalGaml.g:3141:6: lv_key_0_0= ruleClassicFacetKey
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getClassicFacetAccess().getKeyClassicFacetKeyParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
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
                    // InternalGaml.g:3159:4: ( (lv_key_1_0= '<-' ) )
                    {
                    // InternalGaml.g:3159:4: ( (lv_key_1_0= '<-' ) )
                    // InternalGaml.g:3160:5: (lv_key_1_0= '<-' )
                    {
                    // InternalGaml.g:3160:5: (lv_key_1_0= '<-' )
                    // InternalGaml.g:3161:6: lv_key_1_0= '<-'
                    {
                    lv_key_1_0=(Token)match(input,76,FOLLOW_4); if (state.failed) return current;
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

            }

            // InternalGaml.g:3174:3: ( (lv_expr_2_0= ruleExpression ) )
            // InternalGaml.g:3175:4: (lv_expr_2_0= ruleExpression )
            {
            // InternalGaml.g:3175:4: (lv_expr_2_0= ruleExpression )
            // InternalGaml.g:3176:5: lv_expr_2_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getClassicFacetAccess().getExprExpressionParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_expr_2_0=ruleExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getClassicFacetRule());
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
    // $ANTLR end "ruleClassicFacet"


    // $ANTLR start "entryRuleDefinitionFacet"
    // InternalGaml.g:3197:1: entryRuleDefinitionFacet returns [EObject current=null] : iv_ruleDefinitionFacet= ruleDefinitionFacet EOF ;
    public final EObject entryRuleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDefinitionFacet = null;


        try {
            // InternalGaml.g:3197:56: (iv_ruleDefinitionFacet= ruleDefinitionFacet EOF )
            // InternalGaml.g:3198:2: iv_ruleDefinitionFacet= ruleDefinitionFacet EOF
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
    // InternalGaml.g:3204:1: ruleDefinitionFacet returns [EObject current=null] : ( ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleDefinitionFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3210:2: ( ( ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:3211:2: ( ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:3211:2: ( ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:3212:3: ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) ) ( (lv_name_1_0= ruleValid_ID ) )
            {
            // InternalGaml.g:3212:3: ( ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey ) )
            // InternalGaml.g:3213:4: ( 'returns:' )=> (lv_key_0_0= ruleDefinitionFacetKey )
            {
            // InternalGaml.g:3214:4: (lv_key_0_0= ruleDefinitionFacetKey )
            // InternalGaml.g:3215:5: lv_key_0_0= ruleDefinitionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getDefinitionFacetAccess().getKeyDefinitionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_9);
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

            // InternalGaml.g:3232:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:3233:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:3233:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:3234:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getDefinitionFacetAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getDefinitionFacetRule());
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
    // $ANTLR end "ruleDefinitionFacet"


    // $ANTLR start "entryRuleFunctionFacet"
    // InternalGaml.g:3255:1: entryRuleFunctionFacet returns [EObject current=null] : iv_ruleFunctionFacet= ruleFunctionFacet EOF ;
    public final EObject entryRuleFunctionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunctionFacet = null;


        try {
            // InternalGaml.g:3255:54: (iv_ruleFunctionFacet= ruleFunctionFacet EOF )
            // InternalGaml.g:3256:2: iv_ruleFunctionFacet= ruleFunctionFacet EOF
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
    // InternalGaml.g:3262:1: ruleFunctionFacet returns [EObject current=null] : ( ( (lv_key_0_0= '->' ) ) ( (lv_expr_1_0= ruleExpression ) ) ) ;
    public final EObject ruleFunctionFacet() throws RecognitionException {
        EObject current = null;

        Token lv_key_0_0=null;
        EObject lv_expr_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3268:2: ( ( ( (lv_key_0_0= '->' ) ) ( (lv_expr_1_0= ruleExpression ) ) ) )
            // InternalGaml.g:3269:2: ( ( (lv_key_0_0= '->' ) ) ( (lv_expr_1_0= ruleExpression ) ) )
            {
            // InternalGaml.g:3269:2: ( ( (lv_key_0_0= '->' ) ) ( (lv_expr_1_0= ruleExpression ) ) )
            // InternalGaml.g:3270:3: ( (lv_key_0_0= '->' ) ) ( (lv_expr_1_0= ruleExpression ) )
            {
            // InternalGaml.g:3270:3: ( (lv_key_0_0= '->' ) )
            // InternalGaml.g:3271:4: (lv_key_0_0= '->' )
            {
            // InternalGaml.g:3271:4: (lv_key_0_0= '->' )
            // InternalGaml.g:3272:5: lv_key_0_0= '->'
            {
            lv_key_0_0=(Token)match(input,88,FOLLOW_4); if (state.failed) return current;
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

            // InternalGaml.g:3284:3: ( (lv_expr_1_0= ruleExpression ) )
            // InternalGaml.g:3285:4: (lv_expr_1_0= ruleExpression )
            {
            // InternalGaml.g:3285:4: (lv_expr_1_0= ruleExpression )
            // InternalGaml.g:3286:5: lv_expr_1_0= ruleExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFunctionFacetAccess().getExprExpressionParserRuleCall_1_0());
              				
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

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
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


    // $ANTLR start "entryRuleActionFacet"
    // InternalGaml.g:3307:1: entryRuleActionFacet returns [EObject current=null] : iv_ruleActionFacet= ruleActionFacet EOF ;
    public final EObject entryRuleActionFacet() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFacet = null;


        try {
            // InternalGaml.g:3307:52: (iv_ruleActionFacet= ruleActionFacet EOF )
            // InternalGaml.g:3308:2: iv_ruleActionFacet= ruleActionFacet EOF
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
    // InternalGaml.g:3314:1: ruleActionFacet returns [EObject current=null] : ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) ;
    public final EObject ruleActionFacet() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_key_0_0 = null;

        EObject lv_expr_1_0 = null;

        EObject lv_block_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3320:2: ( ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) ) )
            // InternalGaml.g:3321:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            {
            // InternalGaml.g:3321:2: ( ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) ) )
            // InternalGaml.g:3322:3: ( (lv_key_0_0= ruleActionFacetKey ) ) ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            {
            // InternalGaml.g:3322:3: ( (lv_key_0_0= ruleActionFacetKey ) )
            // InternalGaml.g:3323:4: (lv_key_0_0= ruleActionFacetKey )
            {
            // InternalGaml.g:3323:4: (lv_key_0_0= ruleActionFacetKey )
            // InternalGaml.g:3324:5: lv_key_0_0= ruleActionFacetKey
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getActionFacetAccess().getKeyActionFacetKeyParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_36);
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

            // InternalGaml.g:3341:3: ( ( (lv_expr_1_0= ruleActionRef ) ) | ( (lv_block_2_0= ruleBlock ) ) )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==RULE_ID||LA43_0==38||(LA43_0>=51 && LA43_0<=75)) ) {
                alt43=1;
            }
            else if ( (LA43_0==46) ) {
                alt43=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // InternalGaml.g:3342:4: ( (lv_expr_1_0= ruleActionRef ) )
                    {
                    // InternalGaml.g:3342:4: ( (lv_expr_1_0= ruleActionRef ) )
                    // InternalGaml.g:3343:5: (lv_expr_1_0= ruleActionRef )
                    {
                    // InternalGaml.g:3343:5: (lv_expr_1_0= ruleActionRef )
                    // InternalGaml.g:3344:6: lv_expr_1_0= ruleActionRef
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
                    // InternalGaml.g:3362:4: ( (lv_block_2_0= ruleBlock ) )
                    {
                    // InternalGaml.g:3362:4: ( (lv_block_2_0= ruleBlock ) )
                    // InternalGaml.g:3363:5: (lv_block_2_0= ruleBlock )
                    {
                    // InternalGaml.g:3363:5: (lv_block_2_0= ruleBlock )
                    // InternalGaml.g:3364:6: lv_block_2_0= ruleBlock
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


    // $ANTLR start "entryRuleBlock"
    // InternalGaml.g:3386:1: entryRuleBlock returns [EObject current=null] : iv_ruleBlock= ruleBlock EOF ;
    public final EObject entryRuleBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBlock = null;


        try {
            // InternalGaml.g:3386:46: (iv_ruleBlock= ruleBlock EOF )
            // InternalGaml.g:3387:2: iv_ruleBlock= ruleBlock EOF
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
    // InternalGaml.g:3393:1: ruleBlock returns [EObject current=null] : ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) ;
    public final EObject ruleBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3399:2: ( ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) ) )
            // InternalGaml.g:3400:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            {
            // InternalGaml.g:3400:2: ( () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' ) )
            // InternalGaml.g:3401:3: () otherlv_1= '{' ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:3401:3: ()
            // InternalGaml.g:3402:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,46,FOLLOW_37); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:3412:3: ( ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}' )
            // InternalGaml.g:3413:4: ( (lv_statements_2_0= ruleStatement ) )* otherlv_3= '}'
            {
            // InternalGaml.g:3413:4: ( (lv_statements_2_0= ruleStatement ) )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( ((LA44_0>=RULE_STRING && LA44_0<=RULE_KEYWORD)||LA44_0==21||(LA44_0>=26 && LA44_0<=29)||LA44_0==31||LA44_0==33||(LA44_0>=38 && LA44_0<=42)||(LA44_0>=44 && LA44_0<=46)||(LA44_0>=49 && LA44_0<=75)||LA44_0==98||(LA44_0>=102 && LA44_0<=104)) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // InternalGaml.g:3414:5: (lv_statements_2_0= ruleStatement )
            	    {
            	    // InternalGaml.g:3414:5: (lv_statements_2_0= ruleStatement )
            	    // InternalGaml.g:3415:6: lv_statements_2_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBlockAccess().getStatementsStatementParserRuleCall_2_0_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_37);
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
            	    break loop44;
                }
            } while (true);

            otherlv_3=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:3441:1: entryRuleModelBlock returns [EObject current=null] : iv_ruleModelBlock= ruleModelBlock EOF ;
    public final EObject entryRuleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModelBlock = null;


        try {
            // InternalGaml.g:3441:51: (iv_ruleModelBlock= ruleModelBlock EOF )
            // InternalGaml.g:3442:2: iv_ruleModelBlock= ruleModelBlock EOF
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
    // InternalGaml.g:3448:1: ruleModelBlock returns [EObject current=null] : ( () ( (lv_statements_1_0= ruleS_Section ) )* ) ;
    public final EObject ruleModelBlock() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3454:2: ( ( () ( (lv_statements_1_0= ruleS_Section ) )* ) )
            // InternalGaml.g:3455:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            {
            // InternalGaml.g:3455:2: ( () ( (lv_statements_1_0= ruleS_Section ) )* )
            // InternalGaml.g:3456:3: () ( (lv_statements_1_0= ruleS_Section ) )*
            {
            // InternalGaml.g:3456:3: ()
            // InternalGaml.g:3457:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getModelBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:3463:3: ( (lv_statements_1_0= ruleS_Section ) )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( (LA45_0==25||(LA45_0>=72 && LA45_0<=73)||LA45_0==75) ) {
                    alt45=1;
                }


                switch (alt45) {
            	case 1 :
            	    // InternalGaml.g:3464:4: (lv_statements_1_0= ruleS_Section )
            	    {
            	    // InternalGaml.g:3464:4: (lv_statements_1_0= ruleS_Section )
            	    // InternalGaml.g:3465:5: lv_statements_1_0= ruleS_Section
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getModelBlockAccess().getStatementsS_SectionParserRuleCall_1_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_38);
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
            	    break loop45;
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
    // InternalGaml.g:3486:1: entryRuleDisplayBlock returns [EObject current=null] : iv_ruleDisplayBlock= ruleDisplayBlock EOF ;
    public final EObject entryRuleDisplayBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDisplayBlock = null;


        try {
            // InternalGaml.g:3486:53: (iv_ruleDisplayBlock= ruleDisplayBlock EOF )
            // InternalGaml.g:3487:2: iv_ruleDisplayBlock= ruleDisplayBlock EOF
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
    // InternalGaml.g:3493:1: ruleDisplayBlock returns [EObject current=null] : ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' ) ;
    public final EObject ruleDisplayBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3499:2: ( ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' ) )
            // InternalGaml.g:3500:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' )
            {
            // InternalGaml.g:3500:2: ( () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}' )
            // InternalGaml.g:3501:3: () otherlv_1= '{' ( (lv_statements_2_0= ruleS_Other ) )* otherlv_3= '}'
            {
            // InternalGaml.g:3501:3: ()
            // InternalGaml.g:3502:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getDisplayBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,46,FOLLOW_37); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getDisplayBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:3512:3: ( (lv_statements_2_0= ruleS_Other ) )*
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==RULE_ID||LA46_0==38||(LA46_0>=51 && LA46_0<=75)) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // InternalGaml.g:3513:4: (lv_statements_2_0= ruleS_Other )
            	    {
            	    // InternalGaml.g:3513:4: (lv_statements_2_0= ruleS_Other )
            	    // InternalGaml.g:3514:5: lv_statements_2_0= ruleS_Other
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getDisplayBlockAccess().getStatementsS_OtherParserRuleCall_2_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_37);
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
            	    break loop46;
                }
            } while (true);

            otherlv_3=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleMatchBlock"
    // InternalGaml.g:3539:1: entryRuleMatchBlock returns [EObject current=null] : iv_ruleMatchBlock= ruleMatchBlock EOF ;
    public final EObject entryRuleMatchBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMatchBlock = null;


        try {
            // InternalGaml.g:3539:51: (iv_ruleMatchBlock= ruleMatchBlock EOF )
            // InternalGaml.g:3540:2: iv_ruleMatchBlock= ruleMatchBlock EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMatchBlockRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMatchBlock=ruleMatchBlock();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMatchBlock; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMatchBlock"


    // $ANTLR start "ruleMatchBlock"
    // InternalGaml.g:3546:1: ruleMatchBlock returns [EObject current=null] : ( () otherlv_1= '{' ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+ otherlv_3= '}' ) ;
    public final EObject ruleMatchBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_statements_2_1 = null;

        EObject lv_statements_2_2 = null;



        	enterRule();

        try {
            // InternalGaml.g:3552:2: ( ( () otherlv_1= '{' ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+ otherlv_3= '}' ) )
            // InternalGaml.g:3553:2: ( () otherlv_1= '{' ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+ otherlv_3= '}' )
            {
            // InternalGaml.g:3553:2: ( () otherlv_1= '{' ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+ otherlv_3= '}' )
            // InternalGaml.g:3554:3: () otherlv_1= '{' ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+ otherlv_3= '}'
            {
            // InternalGaml.g:3554:3: ()
            // InternalGaml.g:3555:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getMatchBlockAccess().getBlockAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,46,FOLLOW_39); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getMatchBlockAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalGaml.g:3565:3: ( ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) ) )+
            int cnt48=0;
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( ((LA48_0>=34 && LA48_0<=38)) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // InternalGaml.g:3566:4: ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) )
            	    {
            	    // InternalGaml.g:3566:4: ( (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default ) )
            	    // InternalGaml.g:3567:5: (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default )
            	    {
            	    // InternalGaml.g:3567:5: (lv_statements_2_1= ruleS_Match | lv_statements_2_2= ruleS_Default )
            	    int alt47=2;
            	    int LA47_0 = input.LA(1);

            	    if ( ((LA47_0>=34 && LA47_0<=37)) ) {
            	        alt47=1;
            	    }
            	    else if ( (LA47_0==38) ) {
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
            	            // InternalGaml.g:3568:6: lv_statements_2_1= ruleS_Match
            	            {
            	            if ( state.backtracking==0 ) {

            	              						newCompositeNode(grammarAccess.getMatchBlockAccess().getStatementsS_MatchParserRuleCall_2_0_0());
            	              					
            	            }
            	            pushFollow(FOLLOW_40);
            	            lv_statements_2_1=ruleS_Match();

            	            state._fsp--;
            	            if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						if (current==null) {
            	              							current = createModelElementForParent(grammarAccess.getMatchBlockRule());
            	              						}
            	              						add(
            	              							current,
            	              							"statements",
            	              							lv_statements_2_1,
            	              							"gaml.compiler.Gaml.S_Match");
            	              						afterParserOrEnumRuleCall();
            	              					
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:3584:6: lv_statements_2_2= ruleS_Default
            	            {
            	            if ( state.backtracking==0 ) {

            	              						newCompositeNode(grammarAccess.getMatchBlockAccess().getStatementsS_DefaultParserRuleCall_2_0_1());
            	              					
            	            }
            	            pushFollow(FOLLOW_40);
            	            lv_statements_2_2=ruleS_Default();

            	            state._fsp--;
            	            if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						if (current==null) {
            	              							current = createModelElementForParent(grammarAccess.getMatchBlockRule());
            	              						}
            	              						add(
            	              							current,
            	              							"statements",
            	              							lv_statements_2_2,
            	              							"gaml.compiler.Gaml.S_Default");
            	              						afterParserOrEnumRuleCall();
            	              					
            	            }

            	            }
            	            break;

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt48 >= 1 ) break loop48;
            	    if (state.backtracking>0) {state.failed=true; return current;}
                        EarlyExitException eee =
                            new EarlyExitException(48, input);
                        throw eee;
                }
                cnt48++;
            } while (true);

            otherlv_3=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getMatchBlockAccess().getRightCurlyBracketKeyword_3());
              		
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
    // $ANTLR end "ruleMatchBlock"


    // $ANTLR start "entryRuleExpression"
    // InternalGaml.g:3610:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalGaml.g:3610:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalGaml.g:3611:2: iv_ruleExpression= ruleExpression EOF
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
    // InternalGaml.g:3617:1: ruleExpression returns [EObject current=null] : this_Pair_0= rulePair ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        EObject this_Pair_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3623:2: (this_Pair_0= rulePair )
            // InternalGaml.g:3624:2: this_Pair_0= rulePair
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
    // InternalGaml.g:3635:1: entryRulePair returns [EObject current=null] : iv_rulePair= rulePair EOF ;
    public final EObject entryRulePair() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePair = null;


        try {
            // InternalGaml.g:3635:45: (iv_rulePair= rulePair EOF )
            // InternalGaml.g:3636:2: iv_rulePair= rulePair EOF
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
    // InternalGaml.g:3642:1: rulePair returns [EObject current=null] : (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) ;
    public final EObject rulePair() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_If_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3648:2: ( (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? ) )
            // InternalGaml.g:3649:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            {
            // InternalGaml.g:3649:2: (this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )? )
            // InternalGaml.g:3650:3: this_If_0= ruleIf ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getPairAccess().getIfParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_41);
            this_If_0=ruleIf();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_If_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3658:3: ( () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) ) )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==89) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // InternalGaml.g:3659:4: () ( (lv_op_2_0= '::' ) ) ( (lv_right_3_0= ruleIf ) )
                    {
                    // InternalGaml.g:3659:4: ()
                    // InternalGaml.g:3660:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getPairAccess().getBinaryOperatorLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:3666:4: ( (lv_op_2_0= '::' ) )
                    // InternalGaml.g:3667:5: (lv_op_2_0= '::' )
                    {
                    // InternalGaml.g:3667:5: (lv_op_2_0= '::' )
                    // InternalGaml.g:3668:6: lv_op_2_0= '::'
                    {
                    lv_op_2_0=(Token)match(input,89,FOLLOW_4); if (state.failed) return current;
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

                    // InternalGaml.g:3680:4: ( (lv_right_3_0= ruleIf ) )
                    // InternalGaml.g:3681:5: (lv_right_3_0= ruleIf )
                    {
                    // InternalGaml.g:3681:5: (lv_right_3_0= ruleIf )
                    // InternalGaml.g:3682:6: lv_right_3_0= ruleIf
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
    // InternalGaml.g:3704:1: entryRuleIf returns [EObject current=null] : iv_ruleIf= ruleIf EOF ;
    public final EObject entryRuleIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIf = null;


        try {
            // InternalGaml.g:3704:43: (iv_ruleIf= ruleIf EOF )
            // InternalGaml.g:3705:2: iv_ruleIf= ruleIf EOF
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
    // InternalGaml.g:3711:1: ruleIf returns [EObject current=null] : (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) ;
    public final EObject ruleIf() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token otherlv_4=null;
        EObject this_Or_0 = null;

        EObject lv_right_3_0 = null;

        EObject lv_ifFalse_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3717:2: ( (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? ) )
            // InternalGaml.g:3718:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            {
            // InternalGaml.g:3718:2: (this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )? )
            // InternalGaml.g:3719:3: this_Or_0= ruleOr ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getIfAccess().getOrParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_42);
            this_Or_0=ruleOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Or_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3727:3: ( () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) ) )?
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==90) ) {
                alt50=1;
            }
            switch (alt50) {
                case 1 :
                    // InternalGaml.g:3728:4: () ( (lv_op_2_0= '?' ) ) ( (lv_right_3_0= ruleOr ) ) (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    {
                    // InternalGaml.g:3728:4: ()
                    // InternalGaml.g:3729:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElementAndSet(
                      						grammarAccess.getIfAccess().getIfLeftAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:3735:4: ( (lv_op_2_0= '?' ) )
                    // InternalGaml.g:3736:5: (lv_op_2_0= '?' )
                    {
                    // InternalGaml.g:3736:5: (lv_op_2_0= '?' )
                    // InternalGaml.g:3737:6: lv_op_2_0= '?'
                    {
                    lv_op_2_0=(Token)match(input,90,FOLLOW_4); if (state.failed) return current;
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

                    // InternalGaml.g:3749:4: ( (lv_right_3_0= ruleOr ) )
                    // InternalGaml.g:3750:5: (lv_right_3_0= ruleOr )
                    {
                    // InternalGaml.g:3750:5: (lv_right_3_0= ruleOr )
                    // InternalGaml.g:3751:6: lv_right_3_0= ruleOr
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getIfAccess().getRightOrParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_35);
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

                    // InternalGaml.g:3768:4: (otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) ) )
                    // InternalGaml.g:3769:5: otherlv_4= ':' ( (lv_ifFalse_5_0= ruleOr ) )
                    {
                    otherlv_4=(Token)match(input,83,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_4, grammarAccess.getIfAccess().getColonKeyword_1_3_0());
                      				
                    }
                    // InternalGaml.g:3773:5: ( (lv_ifFalse_5_0= ruleOr ) )
                    // InternalGaml.g:3774:6: (lv_ifFalse_5_0= ruleOr )
                    {
                    // InternalGaml.g:3774:6: (lv_ifFalse_5_0= ruleOr )
                    // InternalGaml.g:3775:7: lv_ifFalse_5_0= ruleOr
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
    // InternalGaml.g:3798:1: entryRuleOr returns [EObject current=null] : iv_ruleOr= ruleOr EOF ;
    public final EObject entryRuleOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOr = null;


        try {
            // InternalGaml.g:3798:43: (iv_ruleOr= ruleOr EOF )
            // InternalGaml.g:3799:2: iv_ruleOr= ruleOr EOF
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
    // InternalGaml.g:3805:1: ruleOr returns [EObject current=null] : (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) ;
    public final EObject ruleOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_And_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3811:2: ( (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* ) )
            // InternalGaml.g:3812:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            {
            // InternalGaml.g:3812:2: (this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )* )
            // InternalGaml.g:3813:3: this_And_0= ruleAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getOrAccess().getAndParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_43);
            this_And_0=ruleAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_And_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3821:3: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) ) )*
            loop51:
            do {
                int alt51=2;
                int LA51_0 = input.LA(1);

                if ( (LA51_0==91) ) {
                    alt51=1;
                }


                switch (alt51) {
            	case 1 :
            	    // InternalGaml.g:3822:4: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleAnd ) )
            	    {
            	    // InternalGaml.g:3822:4: ()
            	    // InternalGaml.g:3823:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getOrAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:3829:4: ( (lv_op_2_0= 'or' ) )
            	    // InternalGaml.g:3830:5: (lv_op_2_0= 'or' )
            	    {
            	    // InternalGaml.g:3830:5: (lv_op_2_0= 'or' )
            	    // InternalGaml.g:3831:6: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,91,FOLLOW_4); if (state.failed) return current;
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

            	    // InternalGaml.g:3843:4: ( (lv_right_3_0= ruleAnd ) )
            	    // InternalGaml.g:3844:5: (lv_right_3_0= ruleAnd )
            	    {
            	    // InternalGaml.g:3844:5: (lv_right_3_0= ruleAnd )
            	    // InternalGaml.g:3845:6: lv_right_3_0= ruleAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOrAccess().getRightAndParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_43);
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
            	    break loop51;
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
    // InternalGaml.g:3867:1: entryRuleAnd returns [EObject current=null] : iv_ruleAnd= ruleAnd EOF ;
    public final EObject entryRuleAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAnd = null;


        try {
            // InternalGaml.g:3867:44: (iv_ruleAnd= ruleAnd EOF )
            // InternalGaml.g:3868:2: iv_ruleAnd= ruleAnd EOF
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
    // InternalGaml.g:3874:1: ruleAnd returns [EObject current=null] : (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) ;
    public final EObject ruleAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Cast_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:3880:2: ( (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* ) )
            // InternalGaml.g:3881:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            {
            // InternalGaml.g:3881:2: (this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )* )
            // InternalGaml.g:3882:3: this_Cast_0= ruleCast ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAndAccess().getCastParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_44);
            this_Cast_0=ruleCast();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Cast_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:3890:3: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) ) )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==92) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // InternalGaml.g:3891:4: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleCast ) )
            	    {
            	    // InternalGaml.g:3891:4: ()
            	    // InternalGaml.g:3892:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAndAccess().getBinaryOperatorLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:3898:4: ( (lv_op_2_0= 'and' ) )
            	    // InternalGaml.g:3899:5: (lv_op_2_0= 'and' )
            	    {
            	    // InternalGaml.g:3899:5: (lv_op_2_0= 'and' )
            	    // InternalGaml.g:3900:6: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,92,FOLLOW_4); if (state.failed) return current;
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

            	    // InternalGaml.g:3912:4: ( (lv_right_3_0= ruleCast ) )
            	    // InternalGaml.g:3913:5: (lv_right_3_0= ruleCast )
            	    {
            	    // InternalGaml.g:3913:5: (lv_right_3_0= ruleCast )
            	    // InternalGaml.g:3914:6: lv_right_3_0= ruleCast
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAndAccess().getRightCastParserRuleCall_1_2_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_44);
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
            	    break loop52;
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
    // InternalGaml.g:3936:1: entryRuleCast returns [EObject current=null] : iv_ruleCast= ruleCast EOF ;
    public final EObject entryRuleCast() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCast = null;


        try {
            // InternalGaml.g:3936:45: (iv_ruleCast= ruleCast EOF )
            // InternalGaml.g:3937:2: iv_ruleCast= ruleCast EOF
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
    // InternalGaml.g:3943:1: ruleCast returns [EObject current=null] : (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) ;
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
            // InternalGaml.g:3949:2: ( (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? ) )
            // InternalGaml.g:3950:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            {
            // InternalGaml.g:3950:2: (this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )? )
            // InternalGaml.g:3951:3: this_Comparison_0= ruleComparison ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
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
            // InternalGaml.g:3959:3: ( ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) ) )?
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==19) ) {
                alt54=1;
            }
            switch (alt54) {
                case 1 :
                    // InternalGaml.g:3960:4: ( () ( (lv_op_2_0= 'as' ) ) ) ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    {
                    // InternalGaml.g:3960:4: ( () ( (lv_op_2_0= 'as' ) ) )
                    // InternalGaml.g:3961:5: () ( (lv_op_2_0= 'as' ) )
                    {
                    // InternalGaml.g:3961:5: ()
                    // InternalGaml.g:3962:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getCastAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:3968:5: ( (lv_op_2_0= 'as' ) )
                    // InternalGaml.g:3969:6: (lv_op_2_0= 'as' )
                    {
                    // InternalGaml.g:3969:6: (lv_op_2_0= 'as' )
                    // InternalGaml.g:3970:7: lv_op_2_0= 'as'
                    {
                    lv_op_2_0=(Token)match(input,19,FOLLOW_45); if (state.failed) return current;
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

                    // InternalGaml.g:3983:4: ( ( (lv_right_3_0= ruleTypeRef ) ) | (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' ) )
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( (LA53_0==RULE_ID||LA53_0==72) ) {
                        alt53=1;
                    }
                    else if ( (LA53_0==42) ) {
                        alt53=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 53, 0, input);

                        throw nvae;
                    }
                    switch (alt53) {
                        case 1 :
                            // InternalGaml.g:3984:5: ( (lv_right_3_0= ruleTypeRef ) )
                            {
                            // InternalGaml.g:3984:5: ( (lv_right_3_0= ruleTypeRef ) )
                            // InternalGaml.g:3985:6: (lv_right_3_0= ruleTypeRef )
                            {
                            // InternalGaml.g:3985:6: (lv_right_3_0= ruleTypeRef )
                            // InternalGaml.g:3986:7: lv_right_3_0= ruleTypeRef
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
                            // InternalGaml.g:4004:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            {
                            // InternalGaml.g:4004:5: (otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')' )
                            // InternalGaml.g:4005:6: otherlv_4= '(' ( (lv_right_5_0= ruleTypeRef ) ) otherlv_6= ')'
                            {
                            otherlv_4=(Token)match(input,42,FOLLOW_18); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              						newLeafNode(otherlv_4, grammarAccess.getCastAccess().getLeftParenthesisKeyword_1_1_1_0());
                              					
                            }
                            // InternalGaml.g:4009:6: ( (lv_right_5_0= ruleTypeRef ) )
                            // InternalGaml.g:4010:7: (lv_right_5_0= ruleTypeRef )
                            {
                            // InternalGaml.g:4010:7: (lv_right_5_0= ruleTypeRef )
                            // InternalGaml.g:4011:8: lv_right_5_0= ruleTypeRef
                            {
                            if ( state.backtracking==0 ) {

                              								newCompositeNode(grammarAccess.getCastAccess().getRightTypeRefParserRuleCall_1_1_1_1_0());
                              							
                            }
                            pushFollow(FOLLOW_27);
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

                            otherlv_6=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4039:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
    public final EObject entryRuleComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComparison = null;


        try {
            // InternalGaml.g:4039:51: (iv_ruleComparison= ruleComparison EOF )
            // InternalGaml.g:4040:2: iv_ruleComparison= ruleComparison EOF
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
    // InternalGaml.g:4046:1: ruleComparison returns [EObject current=null] : (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) ;
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
            // InternalGaml.g:4052:2: ( (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? ) )
            // InternalGaml.g:4053:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            {
            // InternalGaml.g:4053:2: (this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )? )
            // InternalGaml.g:4054:3: this_Addition_0= ruleAddition ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getComparisonAccess().getAdditionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_46);
            this_Addition_0=ruleAddition();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Addition_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4062:3: ( ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) ) )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==48||(LA56_0>=93 && LA56_0<=96)) ) {
                alt56=1;
            }
            else if ( (LA56_0==78) ) {
                int LA56_2 = input.LA(2);

                if ( ((LA56_2>=RULE_STRING && LA56_2<=RULE_KEYWORD)||LA56_2==21||LA56_2==38||LA56_2==42||LA56_2==46||(LA56_2>=51 && LA56_2<=75)||LA56_2==98||(LA56_2>=102 && LA56_2<=104)) ) {
                    alt56=1;
                }
            }
            switch (alt56) {
                case 1 :
                    // InternalGaml.g:4063:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) ) ( (lv_right_3_0= ruleAddition ) )
                    {
                    // InternalGaml.g:4063:4: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) ) )
                    // InternalGaml.g:4064:5: () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    {
                    // InternalGaml.g:4064:5: ()
                    // InternalGaml.g:4065:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getComparisonAccess().getBinaryOperatorLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4071:5: ( ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) ) )
                    // InternalGaml.g:4072:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    {
                    // InternalGaml.g:4072:6: ( (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' ) )
                    // InternalGaml.g:4073:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    {
                    // InternalGaml.g:4073:7: (lv_op_2_1= '!=' | lv_op_2_2= '=' | lv_op_2_3= '>=' | lv_op_2_4= '<=' | lv_op_2_5= '<' | lv_op_2_6= '>' )
                    int alt55=6;
                    switch ( input.LA(1) ) {
                    case 93:
                        {
                        alt55=1;
                        }
                        break;
                    case 48:
                        {
                        alt55=2;
                        }
                        break;
                    case 94:
                        {
                        alt55=3;
                        }
                        break;
                    case 95:
                        {
                        alt55=4;
                        }
                        break;
                    case 96:
                        {
                        alt55=5;
                        }
                        break;
                    case 78:
                        {
                        alt55=6;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 55, 0, input);

                        throw nvae;
                    }

                    switch (alt55) {
                        case 1 :
                            // InternalGaml.g:4074:8: lv_op_2_1= '!='
                            {
                            lv_op_2_1=(Token)match(input,93,FOLLOW_4); if (state.failed) return current;
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
                            // InternalGaml.g:4085:8: lv_op_2_2= '='
                            {
                            lv_op_2_2=(Token)match(input,48,FOLLOW_4); if (state.failed) return current;
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
                            // InternalGaml.g:4096:8: lv_op_2_3= '>='
                            {
                            lv_op_2_3=(Token)match(input,94,FOLLOW_4); if (state.failed) return current;
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
                            // InternalGaml.g:4107:8: lv_op_2_4= '<='
                            {
                            lv_op_2_4=(Token)match(input,95,FOLLOW_4); if (state.failed) return current;
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
                            // InternalGaml.g:4118:8: lv_op_2_5= '<'
                            {
                            lv_op_2_5=(Token)match(input,96,FOLLOW_4); if (state.failed) return current;
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
                            // InternalGaml.g:4129:8: lv_op_2_6= '>'
                            {
                            lv_op_2_6=(Token)match(input,78,FOLLOW_4); if (state.failed) return current;
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

                    // InternalGaml.g:4143:4: ( (lv_right_3_0= ruleAddition ) )
                    // InternalGaml.g:4144:5: (lv_right_3_0= ruleAddition )
                    {
                    // InternalGaml.g:4144:5: (lv_right_3_0= ruleAddition )
                    // InternalGaml.g:4145:6: lv_right_3_0= ruleAddition
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
    // InternalGaml.g:4167:1: entryRuleAddition returns [EObject current=null] : iv_ruleAddition= ruleAddition EOF ;
    public final EObject entryRuleAddition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAddition = null;


        try {
            // InternalGaml.g:4167:49: (iv_ruleAddition= ruleAddition EOF )
            // InternalGaml.g:4168:2: iv_ruleAddition= ruleAddition EOF
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
    // InternalGaml.g:4174:1: ruleAddition returns [EObject current=null] : (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) ;
    public final EObject ruleAddition() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_Multiplication_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4180:2: ( (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* ) )
            // InternalGaml.g:4181:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            {
            // InternalGaml.g:4181:2: (this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )* )
            // InternalGaml.g:4182:3: this_Multiplication_0= ruleMultiplication ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAdditionAccess().getMultiplicationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_47);
            this_Multiplication_0=ruleMultiplication();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Multiplication_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4190:3: ( ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) ) )*
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( ((LA58_0>=97 && LA58_0<=98)) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // InternalGaml.g:4191:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ) ( (lv_right_3_0= ruleMultiplication ) )
            	    {
            	    // InternalGaml.g:4191:4: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) )
            	    // InternalGaml.g:4192:5: () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    {
            	    // InternalGaml.g:4192:5: ()
            	    // InternalGaml.g:4193:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getAdditionAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4199:5: ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    // InternalGaml.g:4200:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    {
            	    // InternalGaml.g:4200:6: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    // InternalGaml.g:4201:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    {
            	    // InternalGaml.g:4201:7: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    int alt57=2;
            	    int LA57_0 = input.LA(1);

            	    if ( (LA57_0==97) ) {
            	        alt57=1;
            	    }
            	    else if ( (LA57_0==98) ) {
            	        alt57=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 57, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt57) {
            	        case 1 :
            	            // InternalGaml.g:4202:8: lv_op_2_1= '+'
            	            {
            	            lv_op_2_1=(Token)match(input,97,FOLLOW_4); if (state.failed) return current;
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
            	            // InternalGaml.g:4213:8: lv_op_2_2= '-'
            	            {
            	            lv_op_2_2=(Token)match(input,98,FOLLOW_4); if (state.failed) return current;
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

            	    // InternalGaml.g:4227:4: ( (lv_right_3_0= ruleMultiplication ) )
            	    // InternalGaml.g:4228:5: (lv_right_3_0= ruleMultiplication )
            	    {
            	    // InternalGaml.g:4228:5: (lv_right_3_0= ruleMultiplication )
            	    // InternalGaml.g:4229:6: lv_right_3_0= ruleMultiplication
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getAdditionAccess().getRightMultiplicationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_47);
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
            	    break loop58;
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
    // InternalGaml.g:4251:1: entryRuleMultiplication returns [EObject current=null] : iv_ruleMultiplication= ruleMultiplication EOF ;
    public final EObject entryRuleMultiplication() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMultiplication = null;


        try {
            // InternalGaml.g:4251:55: (iv_ruleMultiplication= ruleMultiplication EOF )
            // InternalGaml.g:4252:2: iv_ruleMultiplication= ruleMultiplication EOF
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
    // InternalGaml.g:4258:1: ruleMultiplication returns [EObject current=null] : (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) ;
    public final EObject ruleMultiplication() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        EObject this_Binary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4264:2: ( (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* ) )
            // InternalGaml.g:4265:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            {
            // InternalGaml.g:4265:2: (this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )* )
            // InternalGaml.g:4266:3: this_Binary_0= ruleBinary ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getMultiplicationAccess().getBinaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_48);
            this_Binary_0=ruleBinary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Binary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4274:3: ( ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) ) )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( ((LA60_0>=99 && LA60_0<=101)) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // InternalGaml.g:4275:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) ) ( (lv_right_3_0= ruleBinary ) )
            	    {
            	    // InternalGaml.g:4275:4: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) ) )
            	    // InternalGaml.g:4276:5: () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    {
            	    // InternalGaml.g:4276:5: ()
            	    // InternalGaml.g:4277:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getMultiplicationAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4283:5: ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) ) )
            	    // InternalGaml.g:4284:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    {
            	    // InternalGaml.g:4284:6: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' ) )
            	    // InternalGaml.g:4285:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    {
            	    // InternalGaml.g:4285:7: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '^' )
            	    int alt59=3;
            	    switch ( input.LA(1) ) {
            	    case 99:
            	        {
            	        alt59=1;
            	        }
            	        break;
            	    case 100:
            	        {
            	        alt59=2;
            	        }
            	        break;
            	    case 101:
            	        {
            	        alt59=3;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return current;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 59, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt59) {
            	        case 1 :
            	            // InternalGaml.g:4286:8: lv_op_2_1= '*'
            	            {
            	            lv_op_2_1=(Token)match(input,99,FOLLOW_4); if (state.failed) return current;
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
            	            // InternalGaml.g:4297:8: lv_op_2_2= '/'
            	            {
            	            lv_op_2_2=(Token)match(input,100,FOLLOW_4); if (state.failed) return current;
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
            	            // InternalGaml.g:4308:8: lv_op_2_3= '^'
            	            {
            	            lv_op_2_3=(Token)match(input,101,FOLLOW_4); if (state.failed) return current;
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

            	    // InternalGaml.g:4322:4: ( (lv_right_3_0= ruleBinary ) )
            	    // InternalGaml.g:4323:5: (lv_right_3_0= ruleBinary )
            	    {
            	    // InternalGaml.g:4323:5: (lv_right_3_0= ruleBinary )
            	    // InternalGaml.g:4324:6: lv_right_3_0= ruleBinary
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getMultiplicationAccess().getRightBinaryParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_48);
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
    // $ANTLR end "ruleMultiplication"


    // $ANTLR start "entryRuleBinary"
    // InternalGaml.g:4346:1: entryRuleBinary returns [EObject current=null] : iv_ruleBinary= ruleBinary EOF ;
    public final EObject entryRuleBinary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBinary = null;


        try {
            // InternalGaml.g:4346:47: (iv_ruleBinary= ruleBinary EOF )
            // InternalGaml.g:4347:2: iv_ruleBinary= ruleBinary EOF
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
    // InternalGaml.g:4353:1: ruleBinary returns [EObject current=null] : (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) ;
    public final EObject ruleBinary() throws RecognitionException {
        EObject current = null;

        EObject this_Unit_0 = null;

        AntlrDatatypeRuleToken lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4359:2: ( (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* ) )
            // InternalGaml.g:4360:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            {
            // InternalGaml.g:4360:2: (this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )* )
            // InternalGaml.g:4361:3: this_Unit_0= ruleUnit ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getBinaryAccess().getUnitParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_49);
            this_Unit_0=ruleUnit();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unit_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4369:3: ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*
            loop61:
            do {
                int alt61=2;
                alt61 = dfa61.predict(input);
                switch (alt61) {
            	case 1 :
            	    // InternalGaml.g:4370:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) )
            	    {
            	    // InternalGaml.g:4370:4: ( () ( (lv_op_2_0= ruleValid_ID ) ) )
            	    // InternalGaml.g:4371:5: () ( (lv_op_2_0= ruleValid_ID ) )
            	    {
            	    // InternalGaml.g:4371:5: ()
            	    // InternalGaml.g:4372:6: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      						current = forceCreateModelElementAndSet(
            	      							grammarAccess.getBinaryAccess().getBinaryOperatorLeftAction_1_0_0(),
            	      							current);
            	      					
            	    }

            	    }

            	    // InternalGaml.g:4378:5: ( (lv_op_2_0= ruleValid_ID ) )
            	    // InternalGaml.g:4379:6: (lv_op_2_0= ruleValid_ID )
            	    {
            	    // InternalGaml.g:4379:6: (lv_op_2_0= ruleValid_ID )
            	    // InternalGaml.g:4380:7: lv_op_2_0= ruleValid_ID
            	    {
            	    if ( state.backtracking==0 ) {

            	      							newCompositeNode(grammarAccess.getBinaryAccess().getOpValid_IDParserRuleCall_1_0_1_0());
            	      						
            	    }
            	    pushFollow(FOLLOW_4);
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

            	    // InternalGaml.g:4398:4: ( (lv_right_3_0= ruleUnit ) )
            	    // InternalGaml.g:4399:5: (lv_right_3_0= ruleUnit )
            	    {
            	    // InternalGaml.g:4399:5: (lv_right_3_0= ruleUnit )
            	    // InternalGaml.g:4400:6: lv_right_3_0= ruleUnit
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getBinaryAccess().getRightUnitParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_49);
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
            	    break loop61;
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
    // InternalGaml.g:4422:1: entryRuleUnit returns [EObject current=null] : iv_ruleUnit= ruleUnit EOF ;
    public final EObject entryRuleUnit() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnit = null;


        try {
            // InternalGaml.g:4422:45: (iv_ruleUnit= ruleUnit EOF )
            // InternalGaml.g:4423:2: iv_ruleUnit= ruleUnit EOF
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
    // InternalGaml.g:4429:1: ruleUnit returns [EObject current=null] : (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) ;
    public final EObject ruleUnit() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_Unary_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4435:2: ( (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? ) )
            // InternalGaml.g:4436:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            {
            // InternalGaml.g:4436:2: (this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )? )
            // InternalGaml.g:4437:3: this_Unary_0= ruleUnary ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getUnitAccess().getUnaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_Unary_0=ruleUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Unary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4445:3: ( ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) ) )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==102) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // InternalGaml.g:4446:4: ( () ( (lv_op_2_0= '#' ) ) ) ( (lv_right_3_0= ruleUnitRef ) )
                    {
                    // InternalGaml.g:4446:4: ( () ( (lv_op_2_0= '#' ) ) )
                    // InternalGaml.g:4447:5: () ( (lv_op_2_0= '#' ) )
                    {
                    // InternalGaml.g:4447:5: ()
                    // InternalGaml.g:4448:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getUnitAccess().getUnitLeftAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalGaml.g:4454:5: ( (lv_op_2_0= '#' ) )
                    // InternalGaml.g:4455:6: (lv_op_2_0= '#' )
                    {
                    // InternalGaml.g:4455:6: (lv_op_2_0= '#' )
                    // InternalGaml.g:4456:7: lv_op_2_0= '#'
                    {
                    lv_op_2_0=(Token)match(input,102,FOLLOW_9); if (state.failed) return current;
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

                    // InternalGaml.g:4469:4: ( (lv_right_3_0= ruleUnitRef ) )
                    // InternalGaml.g:4470:5: (lv_right_3_0= ruleUnitRef )
                    {
                    // InternalGaml.g:4470:5: (lv_right_3_0= ruleUnitRef )
                    // InternalGaml.g:4471:6: lv_right_3_0= ruleUnitRef
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
    // InternalGaml.g:4493:1: entryRuleUnary returns [EObject current=null] : iv_ruleUnary= ruleUnary EOF ;
    public final EObject entryRuleUnary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnary = null;


        try {
            // InternalGaml.g:4493:46: (iv_ruleUnary= ruleUnary EOF )
            // InternalGaml.g:4494:2: iv_ruleUnary= ruleUnary EOF
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
    // InternalGaml.g:4500:1: ruleUnary returns [EObject current=null] : (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) ;
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
            // InternalGaml.g:4506:2: ( (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) ) )
            // InternalGaml.g:4507:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            {
            // InternalGaml.g:4507:2: (this_Access_0= ruleAccess | ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) ) )
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( ((LA65_0>=RULE_STRING && LA65_0<=RULE_KEYWORD)||LA65_0==21||LA65_0==38||LA65_0==42||LA65_0==46||(LA65_0>=51 && LA65_0<=75)) ) {
                alt65=1;
            }
            else if ( (LA65_0==98||(LA65_0>=102 && LA65_0<=104)) ) {
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
                    // InternalGaml.g:4508:3: this_Access_0= ruleAccess
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
                    // InternalGaml.g:4517:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    {
                    // InternalGaml.g:4517:3: ( () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) ) )
                    // InternalGaml.g:4518:4: () ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    {
                    // InternalGaml.g:4518:4: ()
                    // InternalGaml.g:4519:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getUnaryAccess().getUnaryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4525:4: ( ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) ) | ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) ) )
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==102) ) {
                        alt64=1;
                    }
                    else if ( (LA64_0==98||(LA64_0>=103 && LA64_0<=104)) ) {
                        alt64=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 64, 0, input);

                        throw nvae;
                    }
                    switch (alt64) {
                        case 1 :
                            // InternalGaml.g:4526:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            {
                            // InternalGaml.g:4526:5: ( ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) ) )
                            // InternalGaml.g:4527:6: ( (lv_op_2_0= '#' ) ) ( (lv_right_3_0= ruleUnitRef ) )
                            {
                            // InternalGaml.g:4527:6: ( (lv_op_2_0= '#' ) )
                            // InternalGaml.g:4528:7: (lv_op_2_0= '#' )
                            {
                            // InternalGaml.g:4528:7: (lv_op_2_0= '#' )
                            // InternalGaml.g:4529:8: lv_op_2_0= '#'
                            {
                            lv_op_2_0=(Token)match(input,102,FOLLOW_9); if (state.failed) return current;
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

                            // InternalGaml.g:4541:6: ( (lv_right_3_0= ruleUnitRef ) )
                            // InternalGaml.g:4542:7: (lv_right_3_0= ruleUnitRef )
                            {
                            // InternalGaml.g:4542:7: (lv_right_3_0= ruleUnitRef )
                            // InternalGaml.g:4543:8: lv_right_3_0= ruleUnitRef
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
                            // InternalGaml.g:4562:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            {
                            // InternalGaml.g:4562:5: ( ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) ) )
                            // InternalGaml.g:4563:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) ) ( (lv_right_5_0= ruleUnary ) )
                            {
                            // InternalGaml.g:4563:6: ( ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) ) )
                            // InternalGaml.g:4564:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            {
                            // InternalGaml.g:4564:7: ( (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' ) )
                            // InternalGaml.g:4565:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            {
                            // InternalGaml.g:4565:8: (lv_op_4_1= '-' | lv_op_4_2= '!' | lv_op_4_3= 'not' )
                            int alt63=3;
                            switch ( input.LA(1) ) {
                            case 98:
                                {
                                alt63=1;
                                }
                                break;
                            case 103:
                                {
                                alt63=2;
                                }
                                break;
                            case 104:
                                {
                                alt63=3;
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
                                    // InternalGaml.g:4566:9: lv_op_4_1= '-'
                                    {
                                    lv_op_4_1=(Token)match(input,98,FOLLOW_4); if (state.failed) return current;
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
                                    // InternalGaml.g:4577:9: lv_op_4_2= '!'
                                    {
                                    lv_op_4_2=(Token)match(input,103,FOLLOW_4); if (state.failed) return current;
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
                                    // InternalGaml.g:4588:9: lv_op_4_3= 'not'
                                    {
                                    lv_op_4_3=(Token)match(input,104,FOLLOW_4); if (state.failed) return current;
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

                            // InternalGaml.g:4601:6: ( (lv_right_5_0= ruleUnary ) )
                            // InternalGaml.g:4602:7: (lv_right_5_0= ruleUnary )
                            {
                            // InternalGaml.g:4602:7: (lv_right_5_0= ruleUnary )
                            // InternalGaml.g:4603:8: lv_right_5_0= ruleUnary
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
    // InternalGaml.g:4627:1: entryRuleAccess returns [EObject current=null] : iv_ruleAccess= ruleAccess EOF ;
    public final EObject entryRuleAccess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAccess = null;


        try {
            // InternalGaml.g:4627:47: (iv_ruleAccess= ruleAccess EOF )
            // InternalGaml.g:4628:2: iv_ruleAccess= ruleAccess EOF
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
    // InternalGaml.g:4634:1: ruleAccess returns [EObject current=null] : (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) ;
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
            // InternalGaml.g:4640:2: ( (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* ) )
            // InternalGaml.g:4641:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            {
            // InternalGaml.g:4641:2: (this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )* )
            // InternalGaml.g:4642:3: this_Primary_0= rulePrimary ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getAccessAccess().getPrimaryParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_Primary_0=rulePrimary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_Primary_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalGaml.g:4650:3: ( () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) ) )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==21||LA68_0==105) ) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // InternalGaml.g:4651:4: () ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    {
            	    // InternalGaml.g:4651:4: ()
            	    // InternalGaml.g:4652:5: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      					current = forceCreateModelElementAndSet(
            	      						grammarAccess.getAccessAccess().getAccessLeftAction_1_0(),
            	      						current);
            	      				
            	    }

            	    }

            	    // InternalGaml.g:4658:4: ( ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' ) | ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) ) )
            	    int alt67=2;
            	    int LA67_0 = input.LA(1);

            	    if ( (LA67_0==21) ) {
            	        alt67=1;
            	    }
            	    else if ( (LA67_0==105) ) {
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
            	            // InternalGaml.g:4659:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            {
            	            // InternalGaml.g:4659:5: ( ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']' )
            	            // InternalGaml.g:4660:6: ( (lv_op_2_0= '[' ) ) ( (lv_right_3_0= ruleExpressionList ) )? otherlv_4= ']'
            	            {
            	            // InternalGaml.g:4660:6: ( (lv_op_2_0= '[' ) )
            	            // InternalGaml.g:4661:7: (lv_op_2_0= '[' )
            	            {
            	            // InternalGaml.g:4661:7: (lv_op_2_0= '[' )
            	            // InternalGaml.g:4662:8: lv_op_2_0= '['
            	            {
            	            lv_op_2_0=(Token)match(input,21,FOLLOW_14); if (state.failed) return current;
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

            	            // InternalGaml.g:4674:6: ( (lv_right_3_0= ruleExpressionList ) )?
            	            int alt66=2;
            	            int LA66_0 = input.LA(1);

            	            if ( ((LA66_0>=RULE_STRING && LA66_0<=RULE_KEYWORD)||LA66_0==21||LA66_0==38||LA66_0==42||LA66_0==46||(LA66_0>=51 && LA66_0<=75)||(LA66_0>=85 && LA66_0<=87)||LA66_0==98||(LA66_0>=102 && LA66_0<=104)) ) {
            	                alt66=1;
            	            }
            	            switch (alt66) {
            	                case 1 :
            	                    // InternalGaml.g:4675:7: (lv_right_3_0= ruleExpressionList )
            	                    {
            	                    // InternalGaml.g:4675:7: (lv_right_3_0= ruleExpressionList )
            	                    // InternalGaml.g:4676:8: lv_right_3_0= ruleExpressionList
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

            	            otherlv_4=(Token)match(input,22,FOLLOW_51); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              						newLeafNode(otherlv_4, grammarAccess.getAccessAccess().getRightSquareBracketKeyword_1_1_0_2());
            	              					
            	            }

            	            }


            	            }
            	            break;
            	        case 2 :
            	            // InternalGaml.g:4699:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            {
            	            // InternalGaml.g:4699:5: ( ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) ) )
            	            // InternalGaml.g:4700:6: ( (lv_op_5_0= '.' ) ) ( (lv_right_6_0= rulePrimary ) )
            	            {
            	            // InternalGaml.g:4700:6: ( (lv_op_5_0= '.' ) )
            	            // InternalGaml.g:4701:7: (lv_op_5_0= '.' )
            	            {
            	            // InternalGaml.g:4701:7: (lv_op_5_0= '.' )
            	            // InternalGaml.g:4702:8: lv_op_5_0= '.'
            	            {
            	            lv_op_5_0=(Token)match(input,105,FOLLOW_52); if (state.failed) return current;
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

            	            // InternalGaml.g:4714:6: ( (lv_right_6_0= rulePrimary ) )
            	            // InternalGaml.g:4715:7: (lv_right_6_0= rulePrimary )
            	            {
            	            // InternalGaml.g:4715:7: (lv_right_6_0= rulePrimary )
            	            // InternalGaml.g:4716:8: lv_right_6_0= rulePrimary
            	            {
            	            if ( state.backtracking==0 ) {

            	              								newCompositeNode(grammarAccess.getAccessAccess().getRightPrimaryParserRuleCall_1_1_1_1_0());
            	              							
            	            }
            	            pushFollow(FOLLOW_51);
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
    // $ANTLR end "ruleAccess"


    // $ANTLR start "entryRulePrimary"
    // InternalGaml.g:4740:1: entryRulePrimary returns [EObject current=null] : iv_rulePrimary= rulePrimary EOF ;
    public final EObject entryRulePrimary() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePrimary = null;


        try {
            // InternalGaml.g:4740:48: (iv_rulePrimary= rulePrimary EOF )
            // InternalGaml.g:4741:2: iv_rulePrimary= rulePrimary EOF
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
    // InternalGaml.g:4747:1: rulePrimary returns [EObject current=null] : (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) ;
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
            // InternalGaml.g:4753:2: ( (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) ) )
            // InternalGaml.g:4754:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            {
            // InternalGaml.g:4754:2: (this_TerminalExpression_0= ruleTerminalExpression | this_AbstractRef_1= ruleAbstractRef | (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' ) | (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' ) | (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' ) )
            int alt71=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
            case RULE_INTEGER:
            case RULE_DOUBLE:
            case RULE_BOOLEAN:
            case RULE_KEYWORD:
                {
                alt71=1;
                }
                break;
            case RULE_ID:
            case 38:
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
                {
                alt71=2;
                }
                break;
            case 42:
                {
                alt71=3;
                }
                break;
            case 21:
                {
                alt71=4;
                }
                break;
            case 46:
                {
                alt71=5;
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
                    // InternalGaml.g:4755:3: this_TerminalExpression_0= ruleTerminalExpression
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
                    // InternalGaml.g:4764:3: this_AbstractRef_1= ruleAbstractRef
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
                    // InternalGaml.g:4773:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    {
                    // InternalGaml.g:4773:3: (otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')' )
                    // InternalGaml.g:4774:4: otherlv_2= '(' this_ExpressionList_3= ruleExpressionList otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,42,FOLLOW_53); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getPrimaryAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getPrimaryAccess().getExpressionListParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_27);
                    this_ExpressionList_3=ruleExpressionList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = this_ExpressionList_3;
                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_4=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getPrimaryAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalGaml.g:4792:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    {
                    // InternalGaml.g:4792:3: (otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']' )
                    // InternalGaml.g:4793:4: otherlv_5= '[' () ( (lv_exprs_7_0= ruleExpressionList ) )? otherlv_8= ']'
                    {
                    otherlv_5=(Token)match(input,21,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getPrimaryAccess().getLeftSquareBracketKeyword_3_0());
                      			
                    }
                    // InternalGaml.g:4797:4: ()
                    // InternalGaml.g:4798:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getArrayAction_3_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4804:4: ( (lv_exprs_7_0= ruleExpressionList ) )?
                    int alt69=2;
                    int LA69_0 = input.LA(1);

                    if ( ((LA69_0>=RULE_STRING && LA69_0<=RULE_KEYWORD)||LA69_0==21||LA69_0==38||LA69_0==42||LA69_0==46||(LA69_0>=51 && LA69_0<=75)||(LA69_0>=85 && LA69_0<=87)||LA69_0==98||(LA69_0>=102 && LA69_0<=104)) ) {
                        alt69=1;
                    }
                    switch (alt69) {
                        case 1 :
                            // InternalGaml.g:4805:5: (lv_exprs_7_0= ruleExpressionList )
                            {
                            // InternalGaml.g:4805:5: (lv_exprs_7_0= ruleExpressionList )
                            // InternalGaml.g:4806:6: lv_exprs_7_0= ruleExpressionList
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

                    otherlv_8=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getPrimaryAccess().getRightSquareBracketKeyword_3_3());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalGaml.g:4829:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    {
                    // InternalGaml.g:4829:3: (otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}' )
                    // InternalGaml.g:4830:4: otherlv_9= '{' () ( (lv_left_11_0= ruleExpression ) ) ( (lv_op_12_0= ',' ) ) ( (lv_right_13_0= ruleExpression ) ) (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )? otherlv_16= '}'
                    {
                    otherlv_9=(Token)match(input,46,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getPrimaryAccess().getLeftCurlyBracketKeyword_4_0());
                      			
                    }
                    // InternalGaml.g:4834:4: ()
                    // InternalGaml.g:4835:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getPrimaryAccess().getPointAction_4_1(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:4841:4: ( (lv_left_11_0= ruleExpression ) )
                    // InternalGaml.g:4842:5: (lv_left_11_0= ruleExpression )
                    {
                    // InternalGaml.g:4842:5: (lv_left_11_0= ruleExpression )
                    // InternalGaml.g:4843:6: lv_left_11_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getLeftExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_54);
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

                    // InternalGaml.g:4860:4: ( (lv_op_12_0= ',' ) )
                    // InternalGaml.g:4861:5: (lv_op_12_0= ',' )
                    {
                    // InternalGaml.g:4861:5: (lv_op_12_0= ',' )
                    // InternalGaml.g:4862:6: lv_op_12_0= ','
                    {
                    lv_op_12_0=(Token)match(input,24,FOLLOW_4); if (state.failed) return current;
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

                    // InternalGaml.g:4874:4: ( (lv_right_13_0= ruleExpression ) )
                    // InternalGaml.g:4875:5: (lv_right_13_0= ruleExpression )
                    {
                    // InternalGaml.g:4875:5: (lv_right_13_0= ruleExpression )
                    // InternalGaml.g:4876:6: lv_right_13_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getPrimaryAccess().getRightExpressionParserRuleCall_4_4_0());
                      					
                    }
                    pushFollow(FOLLOW_55);
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

                    // InternalGaml.g:4893:4: (otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) ) )?
                    int alt70=2;
                    int LA70_0 = input.LA(1);

                    if ( (LA70_0==24) ) {
                        alt70=1;
                    }
                    switch (alt70) {
                        case 1 :
                            // InternalGaml.g:4894:5: otherlv_14= ',' ( (lv_z_15_0= ruleExpression ) )
                            {
                            otherlv_14=(Token)match(input,24,FOLLOW_4); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getPrimaryAccess().getCommaKeyword_4_5_0());
                              				
                            }
                            // InternalGaml.g:4898:5: ( (lv_z_15_0= ruleExpression ) )
                            // InternalGaml.g:4899:6: (lv_z_15_0= ruleExpression )
                            {
                            // InternalGaml.g:4899:6: (lv_z_15_0= ruleExpression )
                            // InternalGaml.g:4900:7: lv_z_15_0= ruleExpression
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getPrimaryAccess().getZExpressionParserRuleCall_4_5_1_0());
                              						
                            }
                            pushFollow(FOLLOW_56);
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

                    otherlv_16=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:4927:1: entryRuleAbstractRef returns [EObject current=null] : iv_ruleAbstractRef= ruleAbstractRef EOF ;
    public final EObject entryRuleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAbstractRef = null;


        try {
            // InternalGaml.g:4927:52: (iv_ruleAbstractRef= ruleAbstractRef EOF )
            // InternalGaml.g:4928:2: iv_ruleAbstractRef= ruleAbstractRef EOF
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
    // InternalGaml.g:4934:1: ruleAbstractRef returns [EObject current=null] : ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) ;
    public final EObject ruleAbstractRef() throws RecognitionException {
        EObject current = null;

        EObject this_Function_0 = null;

        EObject this_VariableRef_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:4940:2: ( ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef ) )
            // InternalGaml.g:4941:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            {
            // InternalGaml.g:4941:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )
            int alt72=2;
            alt72 = dfa72.predict(input);
            switch (alt72) {
                case 1 :
                    // InternalGaml.g:4942:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    {
                    // InternalGaml.g:4942:3: ( ( ruleFunction )=>this_Function_0= ruleFunction )
                    // InternalGaml.g:4943:4: ( ruleFunction )=>this_Function_0= ruleFunction
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
                    // InternalGaml.g:4954:3: this_VariableRef_1= ruleVariableRef
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
    // InternalGaml.g:4966:1: entryRuleFunction returns [EObject current=null] : iv_ruleFunction= ruleFunction EOF ;
    public final EObject entryRuleFunction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunction = null;


        try {
            // InternalGaml.g:4966:49: (iv_ruleFunction= ruleFunction EOF )
            // InternalGaml.g:4967:2: iv_ruleFunction= ruleFunction EOF
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
    // InternalGaml.g:4973:1: ruleFunction returns [EObject current=null] : ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) ;
    public final EObject ruleFunction() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_left_1_0 = null;

        EObject lv_type_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:4979:2: ( ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' ) )
            // InternalGaml.g:4980:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            {
            // InternalGaml.g:4980:2: ( () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')' )
            // InternalGaml.g:4981:3: () ( (lv_left_1_0= ruleActionRef ) ) ( (lv_type_2_0= ruleTypeInfo ) )? otherlv_3= '(' ( (lv_right_4_0= ruleExpressionList ) )? otherlv_5= ')'
            {
            // InternalGaml.g:4981:3: ()
            // InternalGaml.g:4982:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getFunctionAccess().getFunctionAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:4988:3: ( (lv_left_1_0= ruleActionRef ) )
            // InternalGaml.g:4989:4: (lv_left_1_0= ruleActionRef )
            {
            // InternalGaml.g:4989:4: (lv_left_1_0= ruleActionRef )
            // InternalGaml.g:4990:5: lv_left_1_0= ruleActionRef
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

            // InternalGaml.g:5007:3: ( (lv_type_2_0= ruleTypeInfo ) )?
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==96) ) {
                alt73=1;
            }
            switch (alt73) {
                case 1 :
                    // InternalGaml.g:5008:4: (lv_type_2_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:5008:4: (lv_type_2_0= ruleTypeInfo )
                    // InternalGaml.g:5009:5: lv_type_2_0= ruleTypeInfo
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

            otherlv_3=(Token)match(input,42,FOLLOW_59); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getFunctionAccess().getLeftParenthesisKeyword_3());
              		
            }
            // InternalGaml.g:5030:3: ( (lv_right_4_0= ruleExpressionList ) )?
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( ((LA74_0>=RULE_STRING && LA74_0<=RULE_KEYWORD)||LA74_0==21||LA74_0==38||LA74_0==42||LA74_0==46||(LA74_0>=51 && LA74_0<=75)||(LA74_0>=85 && LA74_0<=87)||LA74_0==98||(LA74_0>=102 && LA74_0<=104)) ) {
                alt74=1;
            }
            switch (alt74) {
                case 1 :
                    // InternalGaml.g:5031:4: (lv_right_4_0= ruleExpressionList )
                    {
                    // InternalGaml.g:5031:4: (lv_right_4_0= ruleExpressionList )
                    // InternalGaml.g:5032:5: lv_right_4_0= ruleExpressionList
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getFunctionAccess().getRightExpressionListParserRuleCall_4_0());
                      				
                    }
                    pushFollow(FOLLOW_27);
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

            otherlv_5=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
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
    // InternalGaml.g:5057:1: entryRuleExpressionList returns [EObject current=null] : iv_ruleExpressionList= ruleExpressionList EOF ;
    public final EObject entryRuleExpressionList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpressionList = null;


        try {
            // InternalGaml.g:5057:55: (iv_ruleExpressionList= ruleExpressionList EOF )
            // InternalGaml.g:5058:2: iv_ruleExpressionList= ruleExpressionList EOF
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
    // InternalGaml.g:5064:1: ruleExpressionList returns [EObject current=null] : ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) ;
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
            // InternalGaml.g:5070:2: ( ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) ) )
            // InternalGaml.g:5071:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            {
            // InternalGaml.g:5071:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )
            int alt77=2;
            alt77 = dfa77.predict(input);
            switch (alt77) {
                case 1 :
                    // InternalGaml.g:5072:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    {
                    // InternalGaml.g:5072:3: ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* )
                    // InternalGaml.g:5073:4: ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    {
                    // InternalGaml.g:5073:4: ( (lv_exprs_0_0= ruleExpression ) )
                    // InternalGaml.g:5074:5: (lv_exprs_0_0= ruleExpression )
                    {
                    // InternalGaml.g:5074:5: (lv_exprs_0_0= ruleExpression )
                    // InternalGaml.g:5075:6: lv_exprs_0_0= ruleExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_17);
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

                    // InternalGaml.g:5092:4: (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )*
                    loop75:
                    do {
                        int alt75=2;
                        int LA75_0 = input.LA(1);

                        if ( (LA75_0==24) ) {
                            alt75=1;
                        }


                        switch (alt75) {
                    	case 1 :
                    	    // InternalGaml.g:5093:5: otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) )
                    	    {
                    	    otherlv_1=(Token)match(input,24,FOLLOW_4); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_1, grammarAccess.getExpressionListAccess().getCommaKeyword_0_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:5097:5: ( (lv_exprs_2_0= ruleExpression ) )
                    	    // InternalGaml.g:5098:6: (lv_exprs_2_0= ruleExpression )
                    	    {
                    	    // InternalGaml.g:5098:6: (lv_exprs_2_0= ruleExpression )
                    	    // InternalGaml.g:5099:7: lv_exprs_2_0= ruleExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsExpressionParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_17);
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
                    	    break loop75;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalGaml.g:5119:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    {
                    // InternalGaml.g:5119:3: ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* )
                    // InternalGaml.g:5120:4: ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    {
                    // InternalGaml.g:5120:4: ( (lv_exprs_3_0= ruleParameter ) )
                    // InternalGaml.g:5121:5: (lv_exprs_3_0= ruleParameter )
                    {
                    // InternalGaml.g:5121:5: (lv_exprs_3_0= ruleParameter )
                    // InternalGaml.g:5122:6: lv_exprs_3_0= ruleParameter
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_0_0());
                      					
                    }
                    pushFollow(FOLLOW_17);
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

                    // InternalGaml.g:5139:4: (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )*
                    loop76:
                    do {
                        int alt76=2;
                        int LA76_0 = input.LA(1);

                        if ( (LA76_0==24) ) {
                            alt76=1;
                        }


                        switch (alt76) {
                    	case 1 :
                    	    // InternalGaml.g:5140:5: otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) )
                    	    {
                    	    otherlv_4=(Token)match(input,24,FOLLOW_53); if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      					newLeafNode(otherlv_4, grammarAccess.getExpressionListAccess().getCommaKeyword_1_1_0());
                    	      				
                    	    }
                    	    // InternalGaml.g:5144:5: ( (lv_exprs_5_0= ruleParameter ) )
                    	    // InternalGaml.g:5145:6: (lv_exprs_5_0= ruleParameter )
                    	    {
                    	    // InternalGaml.g:5145:6: (lv_exprs_5_0= ruleParameter )
                    	    // InternalGaml.g:5146:7: lv_exprs_5_0= ruleParameter
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getExpressionListAccess().getExprsParameterParserRuleCall_1_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_17);
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
                    	    break loop76;
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
    // InternalGaml.g:5169:1: entryRuleParameter returns [EObject current=null] : iv_ruleParameter= ruleParameter EOF ;
    public final EObject entryRuleParameter() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameter = null;


        try {
            // InternalGaml.g:5169:50: (iv_ruleParameter= ruleParameter EOF )
            // InternalGaml.g:5170:2: iv_ruleParameter= ruleParameter EOF
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
    // InternalGaml.g:5176:1: ruleParameter returns [EObject current=null] : ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) ;
    public final EObject ruleParameter() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        AntlrDatatypeRuleToken lv_builtInFacetKey_1_1 = null;

        AntlrDatatypeRuleToken lv_builtInFacetKey_1_2 = null;

        EObject lv_left_2_0 = null;

        EObject lv_right_4_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5182:2: ( ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) ) )
            // InternalGaml.g:5183:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            {
            // InternalGaml.g:5183:2: ( () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) ) )
            // InternalGaml.g:5184:3: () ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) ) ( (lv_right_4_0= ruleExpression ) )
            {
            // InternalGaml.g:5184:3: ()
            // InternalGaml.g:5185:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getParameterAccess().getParameterAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5191:3: ( ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) ) | ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' ) )
            int alt79=2;
            int LA79_0 = input.LA(1);

            if ( ((LA79_0>=85 && LA79_0<=87)) ) {
                alt79=1;
            }
            else if ( (LA79_0==RULE_ID||LA79_0==38||(LA79_0>=51 && LA79_0<=75)) ) {
                alt79=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 79, 0, input);

                throw nvae;
            }
            switch (alt79) {
                case 1 :
                    // InternalGaml.g:5192:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) )
                    {
                    // InternalGaml.g:5192:4: ( ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) ) )
                    // InternalGaml.g:5193:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) )
                    {
                    // InternalGaml.g:5193:5: ( (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey ) )
                    // InternalGaml.g:5194:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey )
                    {
                    // InternalGaml.g:5194:6: (lv_builtInFacetKey_1_1= ruleDefinitionFacetKey | lv_builtInFacetKey_1_2= ruleActionFacetKey )
                    int alt78=2;
                    int LA78_0 = input.LA(1);

                    if ( (LA78_0==85) ) {
                        alt78=1;
                    }
                    else if ( ((LA78_0>=86 && LA78_0<=87)) ) {
                        alt78=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 78, 0, input);

                        throw nvae;
                    }
                    switch (alt78) {
                        case 1 :
                            // InternalGaml.g:5195:7: lv_builtInFacetKey_1_1= ruleDefinitionFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyDefinitionFacetKeyParserRuleCall_1_0_0_0());
                              						
                            }
                            pushFollow(FOLLOW_4);
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
                            // InternalGaml.g:5211:7: lv_builtInFacetKey_1_2= ruleActionFacetKey
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getParameterAccess().getBuiltInFacetKeyActionFacetKeyParserRuleCall_1_0_0_1());
                              						
                            }
                            pushFollow(FOLLOW_4);
                            lv_builtInFacetKey_1_2=ruleActionFacetKey();

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
                              								"gaml.compiler.Gaml.ActionFacetKey");
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
                    // InternalGaml.g:5230:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    {
                    // InternalGaml.g:5230:4: ( ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':' )
                    // InternalGaml.g:5231:5: ( (lv_left_2_0= ruleVariableRef ) ) otherlv_3= ':'
                    {
                    // InternalGaml.g:5231:5: ( (lv_left_2_0= ruleVariableRef ) )
                    // InternalGaml.g:5232:6: (lv_left_2_0= ruleVariableRef )
                    {
                    // InternalGaml.g:5232:6: (lv_left_2_0= ruleVariableRef )
                    // InternalGaml.g:5233:7: lv_left_2_0= ruleVariableRef
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getParameterAccess().getLeftVariableRefParserRuleCall_1_1_0_0());
                      						
                    }
                    pushFollow(FOLLOW_35);
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

                    otherlv_3=(Token)match(input,83,FOLLOW_4); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(otherlv_3, grammarAccess.getParameterAccess().getColonKeyword_1_1_1());
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalGaml.g:5256:3: ( (lv_right_4_0= ruleExpression ) )
            // InternalGaml.g:5257:4: (lv_right_4_0= ruleExpression )
            {
            // InternalGaml.g:5257:4: (lv_right_4_0= ruleExpression )
            // InternalGaml.g:5258:5: lv_right_4_0= ruleExpression
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
    // InternalGaml.g:5279:1: entryRuleUnitRef returns [EObject current=null] : iv_ruleUnitRef= ruleUnitRef EOF ;
    public final EObject entryRuleUnitRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitRef = null;


        try {
            // InternalGaml.g:5279:48: (iv_ruleUnitRef= ruleUnitRef EOF )
            // InternalGaml.g:5280:2: iv_ruleUnitRef= ruleUnitRef EOF
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
    // InternalGaml.g:5286:1: ruleUnitRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleUnitRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5292:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5293:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5293:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5294:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5294:3: ()
            // InternalGaml.g:5295:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getUnitRefAccess().getUnitNameAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5301:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5302:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5302:4: ( ruleValid_ID )
            // InternalGaml.g:5303:5: ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElement(grammarAccess.getUnitRefRule());
              					}
              				
            }
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getUnitRefAccess().getRefUnitFakeDefinitionCrossReference_1_0());
              				
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
    // $ANTLR end "ruleUnitRef"


    // $ANTLR start "entryRuleVariableRef"
    // InternalGaml.g:5321:1: entryRuleVariableRef returns [EObject current=null] : iv_ruleVariableRef= ruleVariableRef EOF ;
    public final EObject entryRuleVariableRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVariableRef = null;


        try {
            // InternalGaml.g:5321:52: (iv_ruleVariableRef= ruleVariableRef EOF )
            // InternalGaml.g:5322:2: iv_ruleVariableRef= ruleVariableRef EOF
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
    // InternalGaml.g:5328:1: ruleVariableRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleVariableRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5334:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5335:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5335:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5336:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5336:3: ()
            // InternalGaml.g:5337:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getVariableRefAccess().getVariableRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5343:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5344:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5344:4: ( ruleValid_ID )
            // InternalGaml.g:5345:5: ruleValid_ID
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


    // $ANTLR start "entryRuleActionRef"
    // InternalGaml.g:5363:1: entryRuleActionRef returns [EObject current=null] : iv_ruleActionRef= ruleActionRef EOF ;
    public final EObject entryRuleActionRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionRef = null;


        try {
            // InternalGaml.g:5363:50: (iv_ruleActionRef= ruleActionRef EOF )
            // InternalGaml.g:5364:2: iv_ruleActionRef= ruleActionRef EOF
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
    // InternalGaml.g:5370:1: ruleActionRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleActionRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5376:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5377:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5377:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5378:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5378:3: ()
            // InternalGaml.g:5379:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getActionRefAccess().getActionRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5385:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5386:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5386:4: ( ruleValid_ID )
            // InternalGaml.g:5387:5: ruleValid_ID
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
    // InternalGaml.g:5405:1: entryRuleEquationRef returns [EObject current=null] : iv_ruleEquationRef= ruleEquationRef EOF ;
    public final EObject entryRuleEquationRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationRef = null;


        try {
            // InternalGaml.g:5405:52: (iv_ruleEquationRef= ruleEquationRef EOF )
            // InternalGaml.g:5406:2: iv_ruleEquationRef= ruleEquationRef EOF
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
    // InternalGaml.g:5412:1: ruleEquationRef returns [EObject current=null] : ( () ( ( ruleValid_ID ) ) ) ;
    public final EObject ruleEquationRef() throws RecognitionException {
        EObject current = null;


        	enterRule();

        try {
            // InternalGaml.g:5418:2: ( ( () ( ( ruleValid_ID ) ) ) )
            // InternalGaml.g:5419:2: ( () ( ( ruleValid_ID ) ) )
            {
            // InternalGaml.g:5419:2: ( () ( ( ruleValid_ID ) ) )
            // InternalGaml.g:5420:3: () ( ( ruleValid_ID ) )
            {
            // InternalGaml.g:5420:3: ()
            // InternalGaml.g:5421:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getEquationRefAccess().getEquationRefAction_0(),
              					current);
              			
            }

            }

            // InternalGaml.g:5427:3: ( ( ruleValid_ID ) )
            // InternalGaml.g:5428:4: ( ruleValid_ID )
            {
            // InternalGaml.g:5428:4: ( ruleValid_ID )
            // InternalGaml.g:5429:5: ruleValid_ID
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


    // $ANTLR start "entryRuleTypeRef"
    // InternalGaml.g:5447:1: entryRuleTypeRef returns [EObject current=null] : iv_ruleTypeRef= ruleTypeRef EOF ;
    public final EObject entryRuleTypeRef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeRef = null;


        try {
            // InternalGaml.g:5447:48: (iv_ruleTypeRef= ruleTypeRef EOF )
            // InternalGaml.g:5448:2: iv_ruleTypeRef= ruleTypeRef EOF
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
    // InternalGaml.g:5454:1: ruleTypeRef returns [EObject current=null] : ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) ;
    public final EObject ruleTypeRef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_parameter_2_0 = null;

        EObject lv_parameter_5_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5460:2: ( ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) ) )
            // InternalGaml.g:5461:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            {
            // InternalGaml.g:5461:2: ( ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) ) | ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) ) )
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==RULE_ID) ) {
                alt81=1;
            }
            else if ( (LA81_0==72) ) {
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
                    // InternalGaml.g:5462:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    {
                    // InternalGaml.g:5462:3: ( () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? ) )
                    // InternalGaml.g:5463:4: () ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    {
                    // InternalGaml.g:5463:4: ()
                    // InternalGaml.g:5464:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5470:4: ( ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )? )
                    // InternalGaml.g:5471:5: ( (otherlv_1= RULE_ID ) ) ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    {
                    // InternalGaml.g:5471:5: ( (otherlv_1= RULE_ID ) )
                    // InternalGaml.g:5472:6: (otherlv_1= RULE_ID )
                    {
                    // InternalGaml.g:5472:6: (otherlv_1= RULE_ID )
                    // InternalGaml.g:5473:7: otherlv_1= RULE_ID
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

                    // InternalGaml.g:5484:5: ( (lv_parameter_2_0= ruleTypeInfo ) )?
                    int alt80=2;
                    int LA80_0 = input.LA(1);

                    if ( (LA80_0==96) ) {
                        alt80=1;
                    }
                    switch (alt80) {
                        case 1 :
                            // InternalGaml.g:5485:6: (lv_parameter_2_0= ruleTypeInfo )
                            {
                            // InternalGaml.g:5485:6: (lv_parameter_2_0= ruleTypeInfo )
                            // InternalGaml.g:5486:7: lv_parameter_2_0= ruleTypeInfo
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
                    // InternalGaml.g:5506:3: ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    {
                    // InternalGaml.g:5506:3: ( () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) ) )
                    // InternalGaml.g:5507:4: () ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    {
                    // InternalGaml.g:5507:4: ()
                    // InternalGaml.g:5508:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTypeRefAccess().getTypeRefAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:5514:4: ( ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) ) )
                    // InternalGaml.g:5515:5: ruleK_Species ( (lv_parameter_5_0= ruleTypeInfo ) )
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getTypeRefAccess().getK_SpeciesParserRuleCall_1_1_0());
                      				
                    }
                    pushFollow(FOLLOW_61);
                    ruleK_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					afterParserOrEnumRuleCall();
                      				
                    }
                    // InternalGaml.g:5522:5: ( (lv_parameter_5_0= ruleTypeInfo ) )
                    // InternalGaml.g:5523:6: (lv_parameter_5_0= ruleTypeInfo )
                    {
                    // InternalGaml.g:5523:6: (lv_parameter_5_0= ruleTypeInfo )
                    // InternalGaml.g:5524:7: lv_parameter_5_0= ruleTypeInfo
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
    // InternalGaml.g:5547:1: entryRuleTypeInfo returns [EObject current=null] : iv_ruleTypeInfo= ruleTypeInfo EOF ;
    public final EObject entryRuleTypeInfo() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeInfo = null;


        try {
            // InternalGaml.g:5547:49: (iv_ruleTypeInfo= ruleTypeInfo EOF )
            // InternalGaml.g:5548:2: iv_ruleTypeInfo= ruleTypeInfo EOF
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
    // InternalGaml.g:5554:1: ruleTypeInfo returns [EObject current=null] : (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) ;
    public final EObject ruleTypeInfo() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_first_1_0 = null;

        EObject lv_second_3_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5560:2: ( (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) ) )
            // InternalGaml.g:5561:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            {
            // InternalGaml.g:5561:2: (otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' ) )
            // InternalGaml.g:5562:3: otherlv_0= '<' ( (lv_first_1_0= ruleTypeRef ) ) (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )? ( ( '>' )=>otherlv_4= '>' )
            {
            otherlv_0=(Token)match(input,96,FOLLOW_18); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeInfoAccess().getLessThanSignKeyword_0());
              		
            }
            // InternalGaml.g:5566:3: ( (lv_first_1_0= ruleTypeRef ) )
            // InternalGaml.g:5567:4: (lv_first_1_0= ruleTypeRef )
            {
            // InternalGaml.g:5567:4: (lv_first_1_0= ruleTypeRef )
            // InternalGaml.g:5568:5: lv_first_1_0= ruleTypeRef
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

            // InternalGaml.g:5585:3: (otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) ) )?
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0==24) ) {
                alt82=1;
            }
            switch (alt82) {
                case 1 :
                    // InternalGaml.g:5586:4: otherlv_2= ',' ( (lv_second_3_0= ruleTypeRef ) )
                    {
                    otherlv_2=(Token)match(input,24,FOLLOW_18); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getTypeInfoAccess().getCommaKeyword_2_0());
                      			
                    }
                    // InternalGaml.g:5590:4: ( (lv_second_3_0= ruleTypeRef ) )
                    // InternalGaml.g:5591:5: (lv_second_3_0= ruleTypeRef )
                    {
                    // InternalGaml.g:5591:5: (lv_second_3_0= ruleTypeRef )
                    // InternalGaml.g:5592:6: lv_second_3_0= ruleTypeRef
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getTypeInfoAccess().getSecondTypeRefParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_32);
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

            // InternalGaml.g:5610:3: ( ( '>' )=>otherlv_4= '>' )
            // InternalGaml.g:5611:4: ( '>' )=>otherlv_4= '>'
            {
            otherlv_4=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleEquationDefinition"
    // InternalGaml.g:5621:1: entryRuleEquationDefinition returns [EObject current=null] : iv_ruleEquationDefinition= ruleEquationDefinition EOF ;
    public final EObject entryRuleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationDefinition = null;


        try {
            // InternalGaml.g:5621:59: (iv_ruleEquationDefinition= ruleEquationDefinition EOF )
            // InternalGaml.g:5622:2: iv_ruleEquationDefinition= ruleEquationDefinition EOF
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
    // InternalGaml.g:5628:1: ruleEquationDefinition returns [EObject current=null] : (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) ;
    public final EObject ruleEquationDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Equations_0 = null;

        EObject this_EquationFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5634:2: ( (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition ) )
            // InternalGaml.g:5635:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            {
            // InternalGaml.g:5635:2: (this_S_Equations_0= ruleS_Equations | this_EquationFakeDefinition_1= ruleEquationFakeDefinition )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0==45) ) {
                alt83=1;
            }
            else if ( (LA83_0==111) ) {
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
                    // InternalGaml.g:5636:3: this_S_Equations_0= ruleS_Equations
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
                    // InternalGaml.g:5645:3: this_EquationFakeDefinition_1= ruleEquationFakeDefinition
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
    // InternalGaml.g:5657:1: entryRuleTypeDefinition returns [EObject current=null] : iv_ruleTypeDefinition= ruleTypeDefinition EOF ;
    public final EObject entryRuleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDefinition = null;


        try {
            // InternalGaml.g:5657:55: (iv_ruleTypeDefinition= ruleTypeDefinition EOF )
            // InternalGaml.g:5658:2: iv_ruleTypeDefinition= ruleTypeDefinition EOF
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
    // InternalGaml.g:5664:1: ruleTypeDefinition returns [EObject current=null] : (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) ;
    public final EObject ruleTypeDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Species_0 = null;

        EObject this_TypeFakeDefinition_1 = null;



        	enterRule();

        try {
            // InternalGaml.g:5670:2: ( (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition ) )
            // InternalGaml.g:5671:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            {
            // InternalGaml.g:5671:2: (this_S_Species_0= ruleS_Species | this_TypeFakeDefinition_1= ruleTypeFakeDefinition )
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( ((LA84_0>=72 && LA84_0<=73)) ) {
                alt84=1;
            }
            else if ( (LA84_0==107) ) {
                alt84=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 84, 0, input);

                throw nvae;
            }
            switch (alt84) {
                case 1 :
                    // InternalGaml.g:5672:3: this_S_Species_0= ruleS_Species
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
                    // InternalGaml.g:5681:3: this_TypeFakeDefinition_1= ruleTypeFakeDefinition
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


    // $ANTLR start "entryRuleActionDefinition"
    // InternalGaml.g:5693:1: entryRuleActionDefinition returns [EObject current=null] : iv_ruleActionDefinition= ruleActionDefinition EOF ;
    public final EObject entryRuleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionDefinition = null;


        try {
            // InternalGaml.g:5693:57: (iv_ruleActionDefinition= ruleActionDefinition EOF )
            // InternalGaml.g:5694:2: iv_ruleActionDefinition= ruleActionDefinition EOF
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
    // InternalGaml.g:5700:1: ruleActionDefinition returns [EObject current=null] : (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) ;
    public final EObject ruleActionDefinition() throws RecognitionException {
        EObject current = null;

        EObject this_S_Action_0 = null;

        EObject this_ActionFakeDefinition_1 = null;

        EObject this_S_Definition_2 = null;

        EObject this_TypeDefinition_3 = null;



        	enterRule();

        try {
            // InternalGaml.g:5706:2: ( (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition ) )
            // InternalGaml.g:5707:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            {
            // InternalGaml.g:5707:2: (this_S_Action_0= ruleS_Action | this_ActionFakeDefinition_1= ruleActionFakeDefinition | this_S_Definition_2= ruleS_Definition | this_TypeDefinition_3= ruleTypeDefinition )
            int alt85=4;
            switch ( input.LA(1) ) {
            case 44:
                {
                alt85=1;
                }
                break;
            case 108:
                {
                alt85=2;
                }
                break;
            case RULE_ID:
                {
                alt85=3;
                }
                break;
            case 72:
                {
                int LA85_4 = input.LA(2);

                if ( (LA85_4==96) ) {
                    alt85=3;
                }
                else if ( (LA85_4==RULE_ID) ) {
                    alt85=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 85, 4, input);

                    throw nvae;
                }
                }
                break;
            case 73:
            case 107:
                {
                alt85=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }

            switch (alt85) {
                case 1 :
                    // InternalGaml.g:5708:3: this_S_Action_0= ruleS_Action
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
                    // InternalGaml.g:5717:3: this_ActionFakeDefinition_1= ruleActionFakeDefinition
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
                    // InternalGaml.g:5726:3: this_S_Definition_2= ruleS_Definition
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
                    // InternalGaml.g:5735:3: this_TypeDefinition_3= ruleTypeDefinition
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


    // $ANTLR start "entryRuleVarDefinition"
    // InternalGaml.g:5747:1: entryRuleVarDefinition returns [EObject current=null] : iv_ruleVarDefinition= ruleVarDefinition EOF ;
    public final EObject entryRuleVarDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDefinition = null;


        try {
            // InternalGaml.g:5747:54: (iv_ruleVarDefinition= ruleVarDefinition EOF )
            // InternalGaml.g:5748:2: iv_ruleVarDefinition= ruleVarDefinition EOF
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
    // InternalGaml.g:5754:1: ruleVarDefinition returns [EObject current=null] : ( ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition ) | ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex ) | ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action ) | ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment ) ;
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
            // InternalGaml.g:5760:2: ( ( ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition ) | ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex ) | ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action ) | ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment ) )
            // InternalGaml.g:5761:2: ( ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition ) | ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex ) | ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action ) | ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )
            {
            // InternalGaml.g:5761:2: ( ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition ) | ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex ) | ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action ) | ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )
            int alt86=11;
            alt86 = dfa86.predict(input);
            switch (alt86) {
                case 1 :
                    // InternalGaml.g:5762:3: ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition )
                    {
                    // InternalGaml.g:5762:3: ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition )
                    // InternalGaml.g:5763:4: ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_DefinitionParserRuleCall_0());
                      			
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
                    // InternalGaml.g:5774:3: ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species )
                    {
                    // InternalGaml.g:5774:3: ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species )
                    // InternalGaml.g:5775:4: ( ruleS_Species )=>this_S_Species_1= ruleS_Species
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_SpeciesParserRuleCall_1());
                      			
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


                    }
                    break;
                case 3 :
                    // InternalGaml.g:5786:3: ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex )
                    {
                    // InternalGaml.g:5786:3: ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex )
                    // InternalGaml.g:5787:4: ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ReflexParserRuleCall_2());
                      			
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


                    }
                    break;
                case 4 :
                    // InternalGaml.g:5798:3: ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action )
                    {
                    // InternalGaml.g:5798:3: ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action )
                    // InternalGaml.g:5799:4: ( ruleS_Action )=>this_S_Action_3= ruleS_Action
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ActionParserRuleCall_3());
                      			
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


                    }
                    break;
                case 5 :
                    // InternalGaml.g:5810:3: ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop )
                    {
                    // InternalGaml.g:5810:3: ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop )
                    // InternalGaml.g:5811:4: ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop
                    {
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_LoopParserRuleCall_4());
                      			
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


                    }
                    break;
                case 6 :
                    // InternalGaml.g:5822:3: this_Model_5= ruleModel
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getModelParserRuleCall_5());
                      		
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
                case 7 :
                    // InternalGaml.g:5831:3: this_ArgumentDefinition_6= ruleArgumentDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getArgumentDefinitionParserRuleCall_6());
                      		
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
                case 8 :
                    // InternalGaml.g:5840:3: this_DefinitionFacet_7= ruleDefinitionFacet
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getDefinitionFacetParserRuleCall_7());
                      		
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
                case 9 :
                    // InternalGaml.g:5849:3: this_VarFakeDefinition_8= ruleVarFakeDefinition
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getVarFakeDefinitionParserRuleCall_8());
                      		
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
                case 10 :
                    // InternalGaml.g:5858:3: this_Import_9= ruleImport
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getImportParserRuleCall_9());
                      		
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
                case 11 :
                    // InternalGaml.g:5867:3: this_S_Experiment_10= ruleS_Experiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getVarDefinitionAccess().getS_ExperimentParserRuleCall_10());
                      		
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


    // $ANTLR start "entryRuleValid_ID"
    // InternalGaml.g:5879:1: entryRuleValid_ID returns [String current=null] : iv_ruleValid_ID= ruleValid_ID EOF ;
    public final String entryRuleValid_ID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValid_ID = null;


        try {
            // InternalGaml.g:5879:48: (iv_ruleValid_ID= ruleValid_ID EOF )
            // InternalGaml.g:5880:2: iv_ruleValid_ID= ruleValid_ID EOF
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
    // InternalGaml.g:5886:1: ruleValid_ID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_K_Species_0= ruleK_Species | this_K_Grid_1= ruleK_Grid | this_K_BuiltIn_2= ruleK_BuiltIn | this_K_Init_3= ruleK_Init | this_K_Experiment_4= ruleK_Experiment | this_ID_5= RULE_ID ) ;
    public final AntlrDatatypeRuleToken ruleValid_ID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_5=null;
        AntlrDatatypeRuleToken this_K_Species_0 = null;

        AntlrDatatypeRuleToken this_K_Grid_1 = null;

        AntlrDatatypeRuleToken this_K_BuiltIn_2 = null;

        AntlrDatatypeRuleToken this_K_Init_3 = null;

        AntlrDatatypeRuleToken this_K_Experiment_4 = null;



        	enterRule();

        try {
            // InternalGaml.g:5892:2: ( (this_K_Species_0= ruleK_Species | this_K_Grid_1= ruleK_Grid | this_K_BuiltIn_2= ruleK_BuiltIn | this_K_Init_3= ruleK_Init | this_K_Experiment_4= ruleK_Experiment | this_ID_5= RULE_ID ) )
            // InternalGaml.g:5893:2: (this_K_Species_0= ruleK_Species | this_K_Grid_1= ruleK_Grid | this_K_BuiltIn_2= ruleK_BuiltIn | this_K_Init_3= ruleK_Init | this_K_Experiment_4= ruleK_Experiment | this_ID_5= RULE_ID )
            {
            // InternalGaml.g:5893:2: (this_K_Species_0= ruleK_Species | this_K_Grid_1= ruleK_Grid | this_K_BuiltIn_2= ruleK_BuiltIn | this_K_Init_3= ruleK_Init | this_K_Experiment_4= ruleK_Experiment | this_ID_5= RULE_ID )
            int alt87=6;
            switch ( input.LA(1) ) {
            case 72:
                {
                alt87=1;
                }
                break;
            case 73:
                {
                alt87=2;
                }
                break;
            case 38:
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
                alt87=3;
                }
                break;
            case 74:
                {
                alt87=4;
                }
                break;
            case 75:
                {
                alt87=5;
                }
                break;
            case RULE_ID:
                {
                alt87=6;
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
                    // InternalGaml.g:5894:3: this_K_Species_0= ruleK_Species
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_SpeciesParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Species_0=ruleK_Species();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Species_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalGaml.g:5905:3: this_K_Grid_1= ruleK_Grid
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_GridParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Grid_1=ruleK_Grid();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Grid_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalGaml.g:5916:3: this_K_BuiltIn_2= ruleK_BuiltIn
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_BuiltInParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_BuiltIn_2=ruleK_BuiltIn();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_BuiltIn_2);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalGaml.g:5927:3: this_K_Init_3= ruleK_Init
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_InitParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Init_3=ruleK_Init();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Init_3);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalGaml.g:5938:3: this_K_Experiment_4= ruleK_Experiment
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getValid_IDAccess().getK_ExperimentParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_K_Experiment_4=ruleK_Experiment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_K_Experiment_4);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalGaml.g:5949:3: this_ID_5= RULE_ID
                    {
                    this_ID_5=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ID_5);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_ID_5, grammarAccess.getValid_IDAccess().getIDTerminalRuleCall_5());
                      		
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
    // InternalGaml.g:5960:1: entryRuleUnitFakeDefinition returns [EObject current=null] : iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF ;
    public final EObject entryRuleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnitFakeDefinition = null;


        try {
            // InternalGaml.g:5960:59: (iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF )
            // InternalGaml.g:5961:2: iv_ruleUnitFakeDefinition= ruleUnitFakeDefinition EOF
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
    // InternalGaml.g:5967:1: ruleUnitFakeDefinition returns [EObject current=null] : (otherlv_0= '**unit*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleUnitFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:5973:2: ( (otherlv_0= '**unit*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:5974:2: (otherlv_0= '**unit*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:5974:2: (otherlv_0= '**unit*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:5975:3: otherlv_0= '**unit*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,106,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getUnitFakeDefinitionAccess().getUnitKeyword_0());
              		
            }
            // InternalGaml.g:5979:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:5980:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:5980:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:5981:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getUnitFakeDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getUnitFakeDefinitionRule());
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
    // $ANTLR end "ruleUnitFakeDefinition"


    // $ANTLR start "entryRuleTypeFakeDefinition"
    // InternalGaml.g:6002:1: entryRuleTypeFakeDefinition returns [EObject current=null] : iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF ;
    public final EObject entryRuleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeFakeDefinition = null;


        try {
            // InternalGaml.g:6002:59: (iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF )
            // InternalGaml.g:6003:2: iv_ruleTypeFakeDefinition= ruleTypeFakeDefinition EOF
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
    // InternalGaml.g:6009:1: ruleTypeFakeDefinition returns [EObject current=null] : (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6015:2: ( (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalGaml.g:6016:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalGaml.g:6016:2: (otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalGaml.g:6017:3: otherlv_0= '**type*' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,107,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getTypeFakeDefinitionAccess().getTypeKeyword_0());
              		
            }
            // InternalGaml.g:6021:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalGaml.g:6022:4: (lv_name_1_0= RULE_ID )
            {
            // InternalGaml.g:6022:4: (lv_name_1_0= RULE_ID )
            // InternalGaml.g:6023:5: lv_name_1_0= RULE_ID
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
    // InternalGaml.g:6043:1: entryRuleActionFakeDefinition returns [EObject current=null] : iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF ;
    public final EObject entryRuleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleActionFakeDefinition = null;


        try {
            // InternalGaml.g:6043:61: (iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF )
            // InternalGaml.g:6044:2: iv_ruleActionFakeDefinition= ruleActionFakeDefinition EOF
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
    // InternalGaml.g:6050:1: ruleActionFakeDefinition returns [EObject current=null] : (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleActionFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6056:2: ( (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6057:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6057:2: (otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6058:3: otherlv_0= '**action*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,108,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getActionFakeDefinitionAccess().getActionKeyword_0());
              		
            }
            // InternalGaml.g:6062:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6063:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6063:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6064:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6085:1: entryRuleSkillFakeDefinition returns [EObject current=null] : iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF ;
    public final EObject entryRuleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSkillFakeDefinition = null;


        try {
            // InternalGaml.g:6085:60: (iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF )
            // InternalGaml.g:6086:2: iv_ruleSkillFakeDefinition= ruleSkillFakeDefinition EOF
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
    // InternalGaml.g:6092:1: ruleSkillFakeDefinition returns [EObject current=null] : (otherlv_0= '**skill*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleSkillFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6098:2: ( (otherlv_0= '**skill*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6099:2: (otherlv_0= '**skill*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6099:2: (otherlv_0= '**skill*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6100:3: otherlv_0= '**skill*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,109,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSkillFakeDefinitionAccess().getSkillKeyword_0());
              		
            }
            // InternalGaml.g:6104:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6105:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6105:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6106:5: lv_name_1_0= ruleValid_ID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getSkillFakeDefinitionAccess().getNameValid_IDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValid_ID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getSkillFakeDefinitionRule());
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
    // $ANTLR end "ruleSkillFakeDefinition"


    // $ANTLR start "entryRuleVarFakeDefinition"
    // InternalGaml.g:6127:1: entryRuleVarFakeDefinition returns [EObject current=null] : iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF ;
    public final EObject entryRuleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarFakeDefinition = null;


        try {
            // InternalGaml.g:6127:58: (iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF )
            // InternalGaml.g:6128:2: iv_ruleVarFakeDefinition= ruleVarFakeDefinition EOF
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
    // InternalGaml.g:6134:1: ruleVarFakeDefinition returns [EObject current=null] : (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleVarFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6140:2: ( (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6141:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6141:2: (otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6142:3: otherlv_0= '**var*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,110,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getVarFakeDefinitionAccess().getVarKeyword_0());
              		
            }
            // InternalGaml.g:6146:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6147:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6147:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6148:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6169:1: entryRuleEquationFakeDefinition returns [EObject current=null] : iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF ;
    public final EObject entryRuleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEquationFakeDefinition = null;


        try {
            // InternalGaml.g:6169:63: (iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF )
            // InternalGaml.g:6170:2: iv_ruleEquationFakeDefinition= ruleEquationFakeDefinition EOF
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
    // InternalGaml.g:6176:1: ruleEquationFakeDefinition returns [EObject current=null] : (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) ;
    public final EObject ruleEquationFakeDefinition() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6182:2: ( (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) ) )
            // InternalGaml.g:6183:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            {
            // InternalGaml.g:6183:2: (otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) ) )
            // InternalGaml.g:6184:3: otherlv_0= '**equation*' ( (lv_name_1_0= ruleValid_ID ) )
            {
            otherlv_0=(Token)match(input,111,FOLLOW_9); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getEquationFakeDefinitionAccess().getEquationKeyword_0());
              		
            }
            // InternalGaml.g:6188:3: ( (lv_name_1_0= ruleValid_ID ) )
            // InternalGaml.g:6189:4: (lv_name_1_0= ruleValid_ID )
            {
            // InternalGaml.g:6189:4: (lv_name_1_0= ruleValid_ID )
            // InternalGaml.g:6190:5: lv_name_1_0= ruleValid_ID
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
    // InternalGaml.g:6211:1: entryRuleTerminalExpression returns [EObject current=null] : iv_ruleTerminalExpression= ruleTerminalExpression EOF ;
    public final EObject entryRuleTerminalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTerminalExpression = null;


        try {
            // InternalGaml.g:6211:59: (iv_ruleTerminalExpression= ruleTerminalExpression EOF )
            // InternalGaml.g:6212:2: iv_ruleTerminalExpression= ruleTerminalExpression EOF
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
    // InternalGaml.g:6218:1: ruleTerminalExpression returns [EObject current=null] : (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) ;
    public final EObject ruleTerminalExpression() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        Token lv_op_4_0=null;
        Token lv_op_6_0=null;
        Token lv_op_8_0=null;
        EObject this_StringLiteral_0 = null;



        	enterRule();

        try {
            // InternalGaml.g:6224:2: ( (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) ) )
            // InternalGaml.g:6225:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            {
            // InternalGaml.g:6225:2: (this_StringLiteral_0= ruleStringLiteral | ( () ( (lv_op_2_0= RULE_INTEGER ) ) ) | ( () ( (lv_op_4_0= RULE_DOUBLE ) ) ) | ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) ) | ( () ( (lv_op_8_0= RULE_KEYWORD ) ) ) )
            int alt88=5;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt88=1;
                }
                break;
            case RULE_INTEGER:
                {
                alt88=2;
                }
                break;
            case RULE_DOUBLE:
                {
                alt88=3;
                }
                break;
            case RULE_BOOLEAN:
                {
                alt88=4;
                }
                break;
            case RULE_KEYWORD:
                {
                alt88=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 88, 0, input);

                throw nvae;
            }

            switch (alt88) {
                case 1 :
                    // InternalGaml.g:6226:3: this_StringLiteral_0= ruleStringLiteral
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
                    // InternalGaml.g:6235:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    {
                    // InternalGaml.g:6235:3: ( () ( (lv_op_2_0= RULE_INTEGER ) ) )
                    // InternalGaml.g:6236:4: () ( (lv_op_2_0= RULE_INTEGER ) )
                    {
                    // InternalGaml.g:6236:4: ()
                    // InternalGaml.g:6237:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getIntLiteralAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6243:4: ( (lv_op_2_0= RULE_INTEGER ) )
                    // InternalGaml.g:6244:5: (lv_op_2_0= RULE_INTEGER )
                    {
                    // InternalGaml.g:6244:5: (lv_op_2_0= RULE_INTEGER )
                    // InternalGaml.g:6245:6: lv_op_2_0= RULE_INTEGER
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
                    // InternalGaml.g:6263:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    {
                    // InternalGaml.g:6263:3: ( () ( (lv_op_4_0= RULE_DOUBLE ) ) )
                    // InternalGaml.g:6264:4: () ( (lv_op_4_0= RULE_DOUBLE ) )
                    {
                    // InternalGaml.g:6264:4: ()
                    // InternalGaml.g:6265:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getDoubleLiteralAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6271:4: ( (lv_op_4_0= RULE_DOUBLE ) )
                    // InternalGaml.g:6272:5: (lv_op_4_0= RULE_DOUBLE )
                    {
                    // InternalGaml.g:6272:5: (lv_op_4_0= RULE_DOUBLE )
                    // InternalGaml.g:6273:6: lv_op_4_0= RULE_DOUBLE
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
                    // InternalGaml.g:6291:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    {
                    // InternalGaml.g:6291:3: ( () ( (lv_op_6_0= RULE_BOOLEAN ) ) )
                    // InternalGaml.g:6292:4: () ( (lv_op_6_0= RULE_BOOLEAN ) )
                    {
                    // InternalGaml.g:6292:4: ()
                    // InternalGaml.g:6293:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getBooleanLiteralAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6299:4: ( (lv_op_6_0= RULE_BOOLEAN ) )
                    // InternalGaml.g:6300:5: (lv_op_6_0= RULE_BOOLEAN )
                    {
                    // InternalGaml.g:6300:5: (lv_op_6_0= RULE_BOOLEAN )
                    // InternalGaml.g:6301:6: lv_op_6_0= RULE_BOOLEAN
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
                    // InternalGaml.g:6319:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    {
                    // InternalGaml.g:6319:3: ( () ( (lv_op_8_0= RULE_KEYWORD ) ) )
                    // InternalGaml.g:6320:4: () ( (lv_op_8_0= RULE_KEYWORD ) )
                    {
                    // InternalGaml.g:6320:4: ()
                    // InternalGaml.g:6321:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getTerminalExpressionAccess().getReservedLiteralAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalGaml.g:6327:4: ( (lv_op_8_0= RULE_KEYWORD ) )
                    // InternalGaml.g:6328:5: (lv_op_8_0= RULE_KEYWORD )
                    {
                    // InternalGaml.g:6328:5: (lv_op_8_0= RULE_KEYWORD )
                    // InternalGaml.g:6329:6: lv_op_8_0= RULE_KEYWORD
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
    // InternalGaml.g:6350:1: entryRuleStringLiteral returns [EObject current=null] : iv_ruleStringLiteral= ruleStringLiteral EOF ;
    public final EObject entryRuleStringLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStringLiteral = null;


        try {
            // InternalGaml.g:6350:54: (iv_ruleStringLiteral= ruleStringLiteral EOF )
            // InternalGaml.g:6351:2: iv_ruleStringLiteral= ruleStringLiteral EOF
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
    // InternalGaml.g:6357:1: ruleStringLiteral returns [EObject current=null] : ( (lv_op_0_0= RULE_STRING ) ) ;
    public final EObject ruleStringLiteral() throws RecognitionException {
        EObject current = null;

        Token lv_op_0_0=null;


        	enterRule();

        try {
            // InternalGaml.g:6363:2: ( ( (lv_op_0_0= RULE_STRING ) ) )
            // InternalGaml.g:6364:2: ( (lv_op_0_0= RULE_STRING ) )
            {
            // InternalGaml.g:6364:2: ( (lv_op_0_0= RULE_STRING ) )
            // InternalGaml.g:6365:3: (lv_op_0_0= RULE_STRING )
            {
            // InternalGaml.g:6365:3: (lv_op_0_0= RULE_STRING )
            // InternalGaml.g:6366:4: lv_op_0_0= RULE_STRING
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
        // InternalGaml.g:1031:4: ( ruleS_Species )
        // InternalGaml.g:1031:5: ruleS_Species
        {
        pushFollow(FOLLOW_2);
        ruleS_Species();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_InternalGaml

    // $ANTLR start synpred3_InternalGaml
    public final void synpred3_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1043:4: ( ruleS_Reflex )
        // InternalGaml.g:1043:5: ruleS_Reflex
        {
        pushFollow(FOLLOW_2);
        ruleS_Reflex();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_InternalGaml

    // $ANTLR start synpred4_InternalGaml
    public final void synpred4_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1055:4: ( ruleS_Assignment )
        // InternalGaml.g:1055:5: ruleS_Assignment
        {
        pushFollow(FOLLOW_2);
        ruleS_Assignment();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_InternalGaml

    // $ANTLR start synpred5_InternalGaml
    public final void synpred5_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1067:4: ( ruleS_Definition )
        // InternalGaml.g:1067:5: ruleS_Definition
        {
        pushFollow(FOLLOW_2);
        ruleS_Definition();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_InternalGaml

    // $ANTLR start synpred6_InternalGaml
    public final void synpred6_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1324:5: ( 'else' )
        // InternalGaml.g:1324:6: 'else'
        {
        match(input,30,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_InternalGaml

    // $ANTLR start synpred7_InternalGaml
    public final void synpred7_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1422:5: ( 'catch' )
        // InternalGaml.g:1422:6: 'catch'
        {
        match(input,32,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_InternalGaml

    // $ANTLR start synpred8_InternalGaml
    public final void synpred8_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:1773:5: ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )
        // InternalGaml.g:1773:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        {
        // InternalGaml.g:1773:6: ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] )
        // InternalGaml.g:1774:6: ( ( ruleExpression ) ) ruleFacetsAndBlock[null]
        {
        // InternalGaml.g:1774:6: ( ( ruleExpression ) )
        // InternalGaml.g:1775:7: ( ruleExpression )
        {
        // InternalGaml.g:1775:7: ( ruleExpression )
        // InternalGaml.g:1776:8: ruleExpression
        {
        pushFollow(FOLLOW_6);
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
    // $ANTLR end synpred8_InternalGaml

    // $ANTLR start synpred10_InternalGaml
    public final void synpred10_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:4943:4: ( ruleFunction )
        // InternalGaml.g:4943:5: ruleFunction
        {
        pushFollow(FOLLOW_2);
        ruleFunction();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_InternalGaml

    // $ANTLR start synpred11_InternalGaml
    public final void synpred11_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5611:4: ( '>' )
        // InternalGaml.g:5611:5: '>'
        {
        match(input,78,FOLLOW_2); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_InternalGaml

    // $ANTLR start synpred12_InternalGaml
    public final void synpred12_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5763:4: ( ruleS_Definition )
        // InternalGaml.g:5763:5: ruleS_Definition
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
        // InternalGaml.g:5775:4: ( ruleS_Species )
        // InternalGaml.g:5775:5: ruleS_Species
        {
        pushFollow(FOLLOW_2);
        ruleS_Species();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_InternalGaml

    // $ANTLR start synpred14_InternalGaml
    public final void synpred14_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5787:4: ( ruleS_Reflex )
        // InternalGaml.g:5787:5: ruleS_Reflex
        {
        pushFollow(FOLLOW_2);
        ruleS_Reflex();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_InternalGaml

    // $ANTLR start synpred15_InternalGaml
    public final void synpred15_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5799:4: ( ruleS_Action )
        // InternalGaml.g:5799:5: ruleS_Action
        {
        pushFollow(FOLLOW_2);
        ruleS_Action();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_InternalGaml

    // $ANTLR start synpred16_InternalGaml
    public final void synpred16_InternalGaml_fragment() throws RecognitionException {   
        // InternalGaml.g:5811:4: ( ruleS_Loop )
        // InternalGaml.g:5811:5: ruleS_Loop
        {
        pushFollow(FOLLOW_2);
        ruleS_Loop();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_InternalGaml

    // Delegated rules

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


    protected DFA15 dfa15 = new DFA15(this);
    protected DFA24 dfa24 = new DFA24(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA33 dfa33 = new DFA33(this);
    protected DFA37 dfa37 = new DFA37(this);
    protected DFA61 dfa61 = new DFA61(this);
    protected DFA72 dfa72 = new DFA72(this);
    protected DFA77 dfa77 = new DFA77(this);
    protected DFA86 dfa86 = new DFA86(this);
    static final String dfa_1s = "\70\uffff";
    static final String dfa_2s = "\1\4\13\uffff\2\0\2\uffff\1\0\5\uffff\30\0\12\uffff";
    static final String dfa_3s = "\1\150\13\uffff\2\0\2\uffff\1\0\5\uffff\30\0\12\uffff";
    static final String dfa_4s = "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\uffff\1\7\1\10\1\11\1\12\2\uffff\2\14\1\uffff\5\15\30\uffff\7\15\1\13\1\16\1\17";
    static final String dfa_5s = "\1\0\13\uffff\1\1\1\2\2\uffff\1\3\5\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\12\uffff}>";
    static final String[] dfa_6s = {
            "\1\21\1\55\1\22\1\23\1\24\1\25\13\uffff\1\57\4\uffff\2\6\1\10\1\4\1\uffff\1\5\1\uffff\1\11\4\uffff\1\53\1\2\1\16\1\17\1\56\1\uffff\1\13\1\12\1\60\2\uffff\1\3\1\1\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\14\1\15\1\20\1\54\26\uffff\1\62\3\uffff\1\61\1\63\1\64",
            "",
            "",
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

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = dfa_1;
            this.eof = dfa_1;
            this.min = dfa_2;
            this.max = dfa_3;
            this.accept = dfa_4;
            this.special = dfa_5;
            this.transition = dfa_6;
        }
        public String getDescription() {
            return "939:2: (this_S_Display_0= ruleS_Display | this_S_Return_1= ruleS_Return | this_S_Solve_2= ruleS_Solve | this_S_If_3= ruleS_If | this_S_Try_4= ruleS_Try | this_S_Do_5= ruleS_Do | this_S_Loop_6= ruleS_Loop | this_S_Switch_7= ruleS_Switch | this_S_Equations_8= ruleS_Equations | this_S_Action_9= ruleS_Action | ( ( ruleS_Species )=>this_S_Species_10= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_11= ruleS_Reflex ) | ( ( ruleS_Assignment )=>this_S_Assignment_12= ruleS_Assignment ) | ( ( ruleS_Definition )=>this_S_Definition_13= ruleS_Definition ) | this_S_Other_14= ruleS_Other )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA15_0 = input.LA(1);

                         
                        int index15_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA15_0==50) ) {s = 1;}

                        else if ( (LA15_0==39) ) {s = 2;}

                        else if ( (LA15_0==49) ) {s = 3;}

                        else if ( (LA15_0==29) ) {s = 4;}

                        else if ( (LA15_0==31) ) {s = 5;}

                        else if ( ((LA15_0>=26 && LA15_0<=27)) ) {s = 6;}

                        else if ( (LA15_0==28) ) {s = 8;}

                        else if ( (LA15_0==33) ) {s = 9;}

                        else if ( (LA15_0==45) ) {s = 10;}

                        else if ( (LA15_0==44) ) {s = 11;}

                        else if ( (LA15_0==72) ) {s = 12;}

                        else if ( (LA15_0==73) ) {s = 13;}

                        else if ( (LA15_0==40) && (synpred3_InternalGaml())) {s = 14;}

                        else if ( (LA15_0==41) && (synpred3_InternalGaml())) {s = 15;}

                        else if ( (LA15_0==74) ) {s = 16;}

                        else if ( (LA15_0==RULE_STRING) && (synpred4_InternalGaml())) {s = 17;}

                        else if ( (LA15_0==RULE_INTEGER) && (synpred4_InternalGaml())) {s = 18;}

                        else if ( (LA15_0==RULE_DOUBLE) && (synpred4_InternalGaml())) {s = 19;}

                        else if ( (LA15_0==RULE_BOOLEAN) && (synpred4_InternalGaml())) {s = 20;}

                        else if ( (LA15_0==RULE_KEYWORD) && (synpred4_InternalGaml())) {s = 21;}

                        else if ( (LA15_0==51) ) {s = 22;}

                        else if ( (LA15_0==52) ) {s = 23;}

                        else if ( (LA15_0==53) ) {s = 24;}

                        else if ( (LA15_0==54) ) {s = 25;}

                        else if ( (LA15_0==55) ) {s = 26;}

                        else if ( (LA15_0==56) ) {s = 27;}

                        else if ( (LA15_0==57) ) {s = 28;}

                        else if ( (LA15_0==58) ) {s = 29;}

                        else if ( (LA15_0==59) ) {s = 30;}

                        else if ( (LA15_0==60) ) {s = 31;}

                        else if ( (LA15_0==61) ) {s = 32;}

                        else if ( (LA15_0==62) ) {s = 33;}

                        else if ( (LA15_0==63) ) {s = 34;}

                        else if ( (LA15_0==64) ) {s = 35;}

                        else if ( (LA15_0==65) ) {s = 36;}

                        else if ( (LA15_0==66) ) {s = 37;}

                        else if ( (LA15_0==67) ) {s = 38;}

                        else if ( (LA15_0==68) ) {s = 39;}

                        else if ( (LA15_0==69) ) {s = 40;}

                        else if ( (LA15_0==70) ) {s = 41;}

                        else if ( (LA15_0==71) ) {s = 42;}

                        else if ( (LA15_0==38) ) {s = 43;}

                        else if ( (LA15_0==75) ) {s = 44;}

                        else if ( (LA15_0==RULE_ID) ) {s = 45;}

                        else if ( (LA15_0==42) && (synpred4_InternalGaml())) {s = 46;}

                        else if ( (LA15_0==21) && (synpred4_InternalGaml())) {s = 47;}

                        else if ( (LA15_0==46) && (synpred4_InternalGaml())) {s = 48;}

                        else if ( (LA15_0==102) && (synpred4_InternalGaml())) {s = 49;}

                        else if ( (LA15_0==98) && (synpred4_InternalGaml())) {s = 50;}

                        else if ( (LA15_0==103) && (synpred4_InternalGaml())) {s = 51;}

                        else if ( (LA15_0==104) && (synpred4_InternalGaml())) {s = 52;}

                         
                        input.seek(index15_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA15_12 = input.LA(1);

                         
                        int index15_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_InternalGaml()) ) {s = 53;}

                        else if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (synpred5_InternalGaml()) ) {s = 54;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_12);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA15_13 = input.LA(1);

                         
                        int index15_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_InternalGaml()) ) {s = 53;}

                        else if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_13);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA15_16 = input.LA(1);

                         
                        int index15_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_InternalGaml()) ) {s = 15;}

                        else if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_16);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA15_22 = input.LA(1);

                         
                        int index15_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_22);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA15_23 = input.LA(1);

                         
                        int index15_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_23);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA15_24 = input.LA(1);

                         
                        int index15_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_24);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA15_25 = input.LA(1);

                         
                        int index15_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_25);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA15_26 = input.LA(1);

                         
                        int index15_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_26);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA15_27 = input.LA(1);

                         
                        int index15_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_27);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA15_28 = input.LA(1);

                         
                        int index15_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_28);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA15_29 = input.LA(1);

                         
                        int index15_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_29);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA15_30 = input.LA(1);

                         
                        int index15_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_30);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA15_31 = input.LA(1);

                         
                        int index15_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_31);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA15_32 = input.LA(1);

                         
                        int index15_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_32);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA15_33 = input.LA(1);

                         
                        int index15_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_33);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA15_34 = input.LA(1);

                         
                        int index15_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_34);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA15_35 = input.LA(1);

                         
                        int index15_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_35);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA15_36 = input.LA(1);

                         
                        int index15_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_36);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA15_37 = input.LA(1);

                         
                        int index15_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_37);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA15_38 = input.LA(1);

                         
                        int index15_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_38);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA15_39 = input.LA(1);

                         
                        int index15_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_39);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA15_40 = input.LA(1);

                         
                        int index15_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_40);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA15_41 = input.LA(1);

                         
                        int index15_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_41);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA15_42 = input.LA(1);

                         
                        int index15_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_42);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA15_43 = input.LA(1);

                         
                        int index15_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_43);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA15_44 = input.LA(1);

                         
                        int index15_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_44);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA15_45 = input.LA(1);

                         
                        int index15_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_InternalGaml()) ) {s = 52;}

                        else if ( (synpred5_InternalGaml()) ) {s = 54;}

                        else if ( (true) ) {s = 55;}

                         
                        input.seek(index15_45);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 15, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_7s = "\57\uffff";
    static final String dfa_8s = "\1\4\5\uffff\33\0\2\uffff\1\0\13\uffff";
    static final String dfa_9s = "\1\150\5\uffff\33\0\2\uffff\1\0\13\uffff";
    static final String dfa_10s = "\1\uffff\5\1\33\uffff\2\1\1\uffff\4\1\1\2\6\uffff";
    static final String dfa_11s = "\1\0\5\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\2\uffff\1\34\13\uffff}>";
    static final String[] dfa_12s = {
            "\1\1\1\40\1\2\1\3\1\4\1\5\13\uffff\1\42\1\uffff\1\50\16\uffff\1\35\3\uffff\1\41\3\uffff\1\43\4\uffff\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\6\1\7\1\36\1\37\1\50\7\uffff\5\50\11\uffff\1\45\3\uffff\1\44\1\46\1\47",
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
            ""
    };

    static final short[] dfa_7 = DFA.unpackEncodedString(dfa_7s);
    static final char[] dfa_8 = DFA.unpackEncodedStringToUnsignedChars(dfa_8s);
    static final char[] dfa_9 = DFA.unpackEncodedStringToUnsignedChars(dfa_9s);
    static final short[] dfa_10 = DFA.unpackEncodedString(dfa_10s);
    static final short[] dfa_11 = DFA.unpackEncodedString(dfa_11s);
    static final short[][] dfa_12 = unpackEncodedStringArray(dfa_12s);

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = dfa_7;
            this.eof = dfa_7;
            this.min = dfa_8;
            this.max = dfa_9;
            this.accept = dfa_10;
            this.special = dfa_11;
            this.transition = dfa_12;
        }
        public String getDescription() {
            return "1771:3: ( ( ( ( ( ( ruleExpression ) ) ruleFacetsAndBlock[null] ) )=> ( ( (lv_expr_1_0= ruleExpression ) ) this_FacetsAndBlock_2= ruleFacetsAndBlock[$current] ) ) | this_FacetsAndBlock_3= ruleFacetsAndBlock[$current] )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_0 = input.LA(1);

                         
                        int index24_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA24_0==RULE_STRING) && (synpred8_InternalGaml())) {s = 1;}

                        else if ( (LA24_0==RULE_INTEGER) && (synpred8_InternalGaml())) {s = 2;}

                        else if ( (LA24_0==RULE_DOUBLE) && (synpred8_InternalGaml())) {s = 3;}

                        else if ( (LA24_0==RULE_BOOLEAN) && (synpred8_InternalGaml())) {s = 4;}

                        else if ( (LA24_0==RULE_KEYWORD) && (synpred8_InternalGaml())) {s = 5;}

                        else if ( (LA24_0==72) ) {s = 6;}

                        else if ( (LA24_0==73) ) {s = 7;}

                        else if ( (LA24_0==51) ) {s = 8;}

                        else if ( (LA24_0==52) ) {s = 9;}

                        else if ( (LA24_0==53) ) {s = 10;}

                        else if ( (LA24_0==54) ) {s = 11;}

                        else if ( (LA24_0==55) ) {s = 12;}

                        else if ( (LA24_0==56) ) {s = 13;}

                        else if ( (LA24_0==57) ) {s = 14;}

                        else if ( (LA24_0==58) ) {s = 15;}

                        else if ( (LA24_0==59) ) {s = 16;}

                        else if ( (LA24_0==60) ) {s = 17;}

                        else if ( (LA24_0==61) ) {s = 18;}

                        else if ( (LA24_0==62) ) {s = 19;}

                        else if ( (LA24_0==63) ) {s = 20;}

                        else if ( (LA24_0==64) ) {s = 21;}

                        else if ( (LA24_0==65) ) {s = 22;}

                        else if ( (LA24_0==66) ) {s = 23;}

                        else if ( (LA24_0==67) ) {s = 24;}

                        else if ( (LA24_0==68) ) {s = 25;}

                        else if ( (LA24_0==69) ) {s = 26;}

                        else if ( (LA24_0==70) ) {s = 27;}

                        else if ( (LA24_0==71) ) {s = 28;}

                        else if ( (LA24_0==38) ) {s = 29;}

                        else if ( (LA24_0==74) ) {s = 30;}

                        else if ( (LA24_0==75) ) {s = 31;}

                        else if ( (LA24_0==RULE_ID) ) {s = 32;}

                        else if ( (LA24_0==42) && (synpred8_InternalGaml())) {s = 33;}

                        else if ( (LA24_0==21) && (synpred8_InternalGaml())) {s = 34;}

                        else if ( (LA24_0==46) ) {s = 35;}

                        else if ( (LA24_0==102) && (synpred8_InternalGaml())) {s = 36;}

                        else if ( (LA24_0==98) && (synpred8_InternalGaml())) {s = 37;}

                        else if ( (LA24_0==103) && (synpred8_InternalGaml())) {s = 38;}

                        else if ( (LA24_0==104) && (synpred8_InternalGaml())) {s = 39;}

                        else if ( (LA24_0==23||LA24_0==76||(LA24_0>=84 && LA24_0<=88)) ) {s = 40;}

                         
                        input.seek(index24_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA24_6 = input.LA(1);

                         
                        int index24_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_6);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA24_7 = input.LA(1);

                         
                        int index24_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA24_8 = input.LA(1);

                         
                        int index24_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA24_9 = input.LA(1);

                         
                        int index24_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA24_10 = input.LA(1);

                         
                        int index24_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_10);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA24_11 = input.LA(1);

                         
                        int index24_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA24_12 = input.LA(1);

                         
                        int index24_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA24_13 = input.LA(1);

                         
                        int index24_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_13);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA24_14 = input.LA(1);

                         
                        int index24_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_14);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA24_15 = input.LA(1);

                         
                        int index24_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_15);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA24_16 = input.LA(1);

                         
                        int index24_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_16);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA24_17 = input.LA(1);

                         
                        int index24_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_17);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA24_18 = input.LA(1);

                         
                        int index24_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_18);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA24_19 = input.LA(1);

                         
                        int index24_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_19);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA24_20 = input.LA(1);

                         
                        int index24_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_20);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA24_21 = input.LA(1);

                         
                        int index24_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_21);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA24_22 = input.LA(1);

                         
                        int index24_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_22);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA24_23 = input.LA(1);

                         
                        int index24_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_23);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA24_24 = input.LA(1);

                         
                        int index24_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_24);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA24_25 = input.LA(1);

                         
                        int index24_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_25);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA24_26 = input.LA(1);

                         
                        int index24_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_26);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA24_27 = input.LA(1);

                         
                        int index24_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_27);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA24_28 = input.LA(1);

                         
                        int index24_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_28);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA24_29 = input.LA(1);

                         
                        int index24_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_29);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA24_30 = input.LA(1);

                         
                        int index24_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_30);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA24_31 = input.LA(1);

                         
                        int index24_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_31);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA24_32 = input.LA(1);

                         
                        int index24_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_32);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA24_35 = input.LA(1);

                         
                        int index24_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_InternalGaml()) ) {s = 39;}

                        else if ( (true) ) {s = 40;}

                         
                        input.seek(index24_35);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 24, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_13s = "\36\uffff";
    static final String dfa_14s = "\34\5\2\uffff";
    static final String dfa_15s = "\34\130\2\uffff";
    static final String dfa_16s = "\34\uffff\1\2\1\1";
    static final String dfa_17s = "\36\uffff}>";
    static final String[] dfa_18s = {
            "\1\33\21\uffff\1\34\16\uffff\1\30\7\uffff\1\34\4\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\1\1\2\1\31\1\32\1\34\7\uffff\5\34",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "\1\35\21\uffff\1\35\16\uffff\1\35\7\uffff\1\35\4\uffff\32\35\6\uffff\1\34\5\35",
            "",
            ""
    };

    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final char[] dfa_14 = DFA.unpackEncodedStringToUnsignedChars(dfa_14s);
    static final char[] dfa_15 = DFA.unpackEncodedStringToUnsignedChars(dfa_15s);
    static final short[] dfa_16 = DFA.unpackEncodedString(dfa_16s);
    static final short[] dfa_17 = DFA.unpackEncodedString(dfa_17s);
    static final short[][] dfa_18 = unpackEncodedStringArray(dfa_18s);

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_14;
            this.max = dfa_15;
            this.accept = dfa_16;
            this.special = dfa_17;
            this.transition = dfa_18;
        }
        public String getDescription() {
            return "1890:3: ( (lv_name_1_0= ruleValid_ID ) )?";
        }
    }
    static final String dfa_19s = "\1\5\33\52\2\uffff";
    static final String dfa_20s = "\1\113\33\140\2\uffff";
    static final String dfa_21s = "\34\uffff\1\1\1\2";
    static final String[] dfa_22s = {
            "\1\33\40\uffff\1\30\14\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\1\1\2\1\31\1\32",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "\1\34\5\uffff\1\35\57\uffff\1\34",
            "",
            ""
    };
    static final char[] dfa_19 = DFA.unpackEncodedStringToUnsignedChars(dfa_19s);
    static final char[] dfa_20 = DFA.unpackEncodedStringToUnsignedChars(dfa_20s);
    static final short[] dfa_21 = DFA.unpackEncodedString(dfa_21s);
    static final short[][] dfa_22 = unpackEncodedStringArray(dfa_22s);

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_19;
            this.max = dfa_20;
            this.accept = dfa_21;
            this.special = dfa_17;
            this.transition = dfa_22;
        }
        public String getDescription() {
            return "2333:5: (lv_expr_0_1= ruleFunction | lv_expr_0_2= ruleVariableRef )";
        }
    }
    static final String dfa_23s = "\12\uffff";
    static final String dfa_24s = "\1\114\2\uffff\1\116\6\uffff";
    static final String dfa_25s = "\1\122\2\uffff\1\120\6\uffff";
    static final String dfa_26s = "\1\uffff\1\1\1\2\1\uffff\1\4\1\6\1\7\1\10\1\5\1\3";
    static final String dfa_27s = "\12\uffff}>";
    static final String[] dfa_28s = {
            "\1\1\1\2\1\3\1\4\1\7\1\5\1\6",
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

    static final short[] dfa_23 = DFA.unpackEncodedString(dfa_23s);
    static final char[] dfa_24 = DFA.unpackEncodedStringToUnsignedChars(dfa_24s);
    static final char[] dfa_25 = DFA.unpackEncodedStringToUnsignedChars(dfa_25s);
    static final short[] dfa_26 = DFA.unpackEncodedString(dfa_26s);
    static final short[] dfa_27 = DFA.unpackEncodedString(dfa_27s);
    static final short[][] dfa_28 = unpackEncodedStringArray(dfa_28s);

    class DFA37 extends DFA {

        public DFA37(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 37;
            this.eot = dfa_23;
            this.eof = dfa_23;
            this.min = dfa_24;
            this.max = dfa_25;
            this.accept = dfa_26;
            this.special = dfa_27;
            this.transition = dfa_28;
        }
        public String getDescription() {
            return "2827:2: (kw= '<-' | kw= '<<' | (kw= '>' kw= '>' ) | kw= '<<+' | (kw= '>' kw= '>-' ) | kw= '+<-' | kw= '<+' | kw= '>-' )";
        }
    }
    static final String dfa_29s = "\1\1\35\uffff";
    static final String dfa_30s = "\1\5\1\uffff\33\4\1\uffff";
    static final String dfa_31s = "\1\145\1\uffff\33\150\1\uffff";
    static final String dfa_32s = "\1\uffff\1\2\33\uffff\1\1";
    static final String[] dfa_33s = {
            "\1\34\15\uffff\1\1\2\uffff\3\1\15\uffff\1\31\4\uffff\1\1\2\uffff\3\1\2\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\2\1\3\1\32\1\33\32\1",
            "",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            "\6\35\13\uffff\1\35\20\uffff\1\35\3\uffff\1\35\3\uffff\1\35\4\uffff\31\35\7\uffff\1\1\16\uffff\1\35\3\uffff\3\35",
            ""
    };
    static final short[] dfa_29 = DFA.unpackEncodedString(dfa_29s);
    static final char[] dfa_30 = DFA.unpackEncodedStringToUnsignedChars(dfa_30s);
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final short[] dfa_32 = DFA.unpackEncodedString(dfa_32s);
    static final short[][] dfa_33 = unpackEncodedStringArray(dfa_33s);

    class DFA61 extends DFA {

        public DFA61(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 61;
            this.eot = dfa_13;
            this.eof = dfa_29;
            this.min = dfa_30;
            this.max = dfa_31;
            this.accept = dfa_32;
            this.special = dfa_17;
            this.transition = dfa_33;
        }
        public String getDescription() {
            return "()* loopback of 4369:3: ( ( () ( (lv_op_2_0= ruleValid_ID ) ) ) ( (lv_right_3_0= ruleUnit ) ) )*";
        }
    }
    static final String dfa_34s = "\1\5\33\0\2\uffff";
    static final String dfa_35s = "\1\113\33\0\2\uffff";
    static final String dfa_36s = "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\2\uffff}>";
    static final String[] dfa_37s = {
            "\1\33\40\uffff\1\30\14\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\1\1\2\1\31\1\32",
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
    static final char[] dfa_34 = DFA.unpackEncodedStringToUnsignedChars(dfa_34s);
    static final char[] dfa_35 = DFA.unpackEncodedStringToUnsignedChars(dfa_35s);
    static final short[] dfa_36 = DFA.unpackEncodedString(dfa_36s);
    static final short[][] dfa_37 = unpackEncodedStringArray(dfa_37s);

    class DFA72 extends DFA {

        public DFA72(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 72;
            this.eot = dfa_13;
            this.eof = dfa_13;
            this.min = dfa_34;
            this.max = dfa_35;
            this.accept = dfa_21;
            this.special = dfa_36;
            this.transition = dfa_37;
        }
        public String getDescription() {
            return "4941:2: ( ( ( ruleFunction )=>this_Function_0= ruleFunction ) | this_VariableRef_1= ruleVariableRef )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA72_1 = input.LA(1);

                         
                        int index72_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA72_2 = input.LA(1);

                         
                        int index72_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA72_3 = input.LA(1);

                         
                        int index72_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA72_4 = input.LA(1);

                         
                        int index72_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA72_5 = input.LA(1);

                         
                        int index72_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA72_6 = input.LA(1);

                         
                        int index72_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA72_7 = input.LA(1);

                         
                        int index72_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA72_8 = input.LA(1);

                         
                        int index72_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA72_9 = input.LA(1);

                         
                        int index72_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA72_10 = input.LA(1);

                         
                        int index72_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA72_11 = input.LA(1);

                         
                        int index72_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA72_12 = input.LA(1);

                         
                        int index72_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA72_13 = input.LA(1);

                         
                        int index72_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA72_14 = input.LA(1);

                         
                        int index72_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA72_15 = input.LA(1);

                         
                        int index72_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA72_16 = input.LA(1);

                         
                        int index72_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA72_17 = input.LA(1);

                         
                        int index72_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA72_18 = input.LA(1);

                         
                        int index72_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA72_19 = input.LA(1);

                         
                        int index72_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA72_20 = input.LA(1);

                         
                        int index72_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA72_21 = input.LA(1);

                         
                        int index72_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA72_22 = input.LA(1);

                         
                        int index72_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA72_23 = input.LA(1);

                         
                        int index72_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA72_24 = input.LA(1);

                         
                        int index72_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA72_25 = input.LA(1);

                         
                        int index72_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA72_26 = input.LA(1);

                         
                        int index72_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA72_27 = input.LA(1);

                         
                        int index72_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_InternalGaml()) ) {s = 28;}

                        else if ( (true) ) {s = 29;}

                         
                        input.seek(index72_27);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 72, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_38s = "\2\uffff\33\1\1\uffff";
    static final String dfa_39s = "\1\4\1\uffff\33\5\1\uffff";
    static final String dfa_40s = "\1\150\1\uffff\33\151\1\uffff";
    static final String dfa_41s = "\1\uffff\1\1\33\uffff\1\2";
    static final String[] dfa_42s = {
            "\1\1\1\34\4\1\13\uffff\1\1\20\uffff\1\31\3\uffff\1\1\3\uffff\1\1\4\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\2\1\3\1\32\1\33\11\uffff\3\35\12\uffff\1\1\3\uffff\3\1",
            "",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            "\1\1\15\uffff\1\1\1\uffff\2\1\1\uffff\1\1\15\uffff\1\1\3\uffff\2\1\4\uffff\1\1\2\uffff\31\1\2\uffff\1\1\4\uffff\1\35\5\uffff\16\1\2\uffff\1\1",
            ""
    };
    static final short[] dfa_38 = DFA.unpackEncodedString(dfa_38s);
    static final char[] dfa_39 = DFA.unpackEncodedStringToUnsignedChars(dfa_39s);
    static final char[] dfa_40 = DFA.unpackEncodedStringToUnsignedChars(dfa_40s);
    static final short[] dfa_41 = DFA.unpackEncodedString(dfa_41s);
    static final short[][] dfa_42 = unpackEncodedStringArray(dfa_42s);

    class DFA77 extends DFA {

        public DFA77(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 77;
            this.eot = dfa_13;
            this.eof = dfa_38;
            this.min = dfa_39;
            this.max = dfa_40;
            this.accept = dfa_41;
            this.special = dfa_17;
            this.transition = dfa_42;
        }
        public String getDescription() {
            return "5071:2: ( ( ( (lv_exprs_0_0= ruleExpression ) ) (otherlv_1= ',' ( (lv_exprs_2_0= ruleExpression ) ) )* ) | ( ( (lv_exprs_3_0= ruleParameter ) ) (otherlv_4= ',' ( (lv_exprs_5_0= ruleParameter ) ) )* ) )";
        }
    }
    static final String dfa_43s = "\21\uffff";
    static final String dfa_44s = "\1\5\2\0\16\uffff";
    static final String dfa_45s = "\1\156\2\0\16\uffff";
    static final String dfa_46s = "\3\uffff\1\2\3\3\1\4\1\5\1\6\1\uffff\1\10\1\11\1\12\1\13\1\1\1\7";
    static final String dfa_47s = "\1\0\1\1\1\2\16\uffff}>";
    static final String[] dfa_48s = {
            "\1\1\13\uffff\1\11\1\15\1\uffff\1\11\7\uffff\1\10\13\uffff\1\4\1\5\2\uffff\1\7\33\uffff\1\2\1\3\1\6\1\16\11\uffff\1\13\30\uffff\1\14",
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

    static final short[] dfa_43 = DFA.unpackEncodedString(dfa_43s);
    static final char[] dfa_44 = DFA.unpackEncodedStringToUnsignedChars(dfa_44s);
    static final char[] dfa_45 = DFA.unpackEncodedStringToUnsignedChars(dfa_45s);
    static final short[] dfa_46 = DFA.unpackEncodedString(dfa_46s);
    static final short[] dfa_47 = DFA.unpackEncodedString(dfa_47s);
    static final short[][] dfa_48 = unpackEncodedStringArray(dfa_48s);

    class DFA86 extends DFA {

        public DFA86(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 86;
            this.eot = dfa_43;
            this.eof = dfa_43;
            this.min = dfa_44;
            this.max = dfa_45;
            this.accept = dfa_46;
            this.special = dfa_47;
            this.transition = dfa_48;
        }
        public String getDescription() {
            return "5761:2: ( ( ( ruleS_Definition )=>this_S_Definition_0= ruleS_Definition ) | ( ( ruleS_Species )=>this_S_Species_1= ruleS_Species ) | ( ( ruleS_Reflex )=>this_S_Reflex_2= ruleS_Reflex ) | ( ( ruleS_Action )=>this_S_Action_3= ruleS_Action ) | ( ( ruleS_Loop )=>this_S_Loop_4= ruleS_Loop ) | this_Model_5= ruleModel | this_ArgumentDefinition_6= ruleArgumentDefinition | this_DefinitionFacet_7= ruleDefinitionFacet | this_VarFakeDefinition_8= ruleVarFakeDefinition | this_Import_9= ruleImport | this_S_Experiment_10= ruleS_Experiment )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA86_0 = input.LA(1);

                         
                        int index86_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA86_0==RULE_ID) ) {s = 1;}

                        else if ( (LA86_0==72) ) {s = 2;}

                        else if ( (LA86_0==73) && (synpred13_InternalGaml())) {s = 3;}

                        else if ( (LA86_0==40) && (synpred14_InternalGaml())) {s = 4;}

                        else if ( (LA86_0==41) && (synpred14_InternalGaml())) {s = 5;}

                        else if ( (LA86_0==74) && (synpred14_InternalGaml())) {s = 6;}

                        else if ( (LA86_0==44) && (synpred15_InternalGaml())) {s = 7;}

                        else if ( (LA86_0==28) && (synpred16_InternalGaml())) {s = 8;}

                        else if ( (LA86_0==17||LA86_0==20) ) {s = 9;}

                        else if ( (LA86_0==85) ) {s = 11;}

                        else if ( (LA86_0==110) ) {s = 12;}

                        else if ( (LA86_0==18) ) {s = 13;}

                        else if ( (LA86_0==75) ) {s = 14;}

                         
                        input.seek(index86_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA86_1 = input.LA(1);

                         
                        int index86_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_InternalGaml()) ) {s = 15;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index86_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA86_2 = input.LA(1);

                         
                        int index86_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_InternalGaml()) ) {s = 15;}

                        else if ( (synpred13_InternalGaml()) ) {s = 3;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index86_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 86, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0xFFF84440002003F0L,0x000001C400000FFFL});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0xFFF8004000000030L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0xFFF8404000810020L,0x0000000001F01FFFL});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000120000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0xFFF8004000000020L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000002040000L,0x0000000000000B00L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0xFFF84440006003F0L,0x000001C400E00FFFL});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0xFFF8404000800020L,0x0000000001F01FFFL});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000000000000020L,0x0000000000000100L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0xFFF8404000000020L,0x0000000001F01FFFL});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000400020000000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0xFFF8444000A003F0L,0x000001C400000FFFL});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0xFFF8444000A103F0L,0x000001C401F01FFFL});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0xFFF8444000810020L,0x0000000001F01FFFL});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0000000000000000L,0x000000000007F000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0xFFF8004000800020L,0x0000000001F01FFFL});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0xFFF8804000000020L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0xFFF8404000000020L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0xFFFEF7C2BC2003F0L,0x000001C400000FFFL});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000000002000002L,0x0000000000000B00L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000007C00000000L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0000807C00000000L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000002L,0x0000000010000000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000040000000020L,0x0000000000000100L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0001000000000002L,0x00000001E0004000L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000002L,0x0000000600000000L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000000002L,0x0000003800000000L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0xFFF8004000000022L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000000002L,0x0000004000000000L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000200002L,0x0000020000000000L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0xFFF84440002003F0L,0x0000000000000FFFL});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0xFFF84440002003F0L,0x000001C400E00FFFL});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000800001000000L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000040000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0xFFF84C40002003F0L,0x000001C400E00FFFL});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0x0000000001000000L,0x0000000000004000L});

}