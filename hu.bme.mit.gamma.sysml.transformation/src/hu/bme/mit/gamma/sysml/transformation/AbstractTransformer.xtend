package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.action.model.ActionModelFactory
import hu.bme.mit.gamma.expression.model.ExpressionModelFactory
import hu.bme.mit.gamma.statechart.composite.CompositeModelFactory
import hu.bme.mit.gamma.statechart.interface_.InterfaceModelFactory
import hu.bme.mit.gamma.statechart.statechart.StatechartModelFactory
import hu.bme.mit.gamma.statechart.util.StatechartUtil
import hu.bme.mit.gamma.util.GammaEcoreUtil
import hu.bme.mit.gamma.util.JavaUtil
import java.util.logging.Logger

abstract class AbstractTransformer {
	
	protected final extension GammaEcoreUtil ecoreUtil = GammaEcoreUtil.INSTANCE
	protected final extension JavaUtil javaUtil = JavaUtil.INSTANCE
	protected final extension StatechartUtil statechartUtil = StatechartUtil.INSTANCE
	
	protected final extension ExpressionModelFactory expressionModelFactory = ExpressionModelFactory.eINSTANCE
	protected final extension ActionModelFactory actionModelFactory = ActionModelFactory.eINSTANCE
	protected final extension InterfaceModelFactory interfaceModelFactory = InterfaceModelFactory.eINSTANCE
	protected final extension StatechartModelFactory statechartModelFactory = StatechartModelFactory.eINSTANCE
	protected final extension CompositeModelFactory compositeModelFactory = CompositeModelFactory.eINSTANCE
	
	protected final Logger logger = Logger.getLogger("GammaLogger")
	
}