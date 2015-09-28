package jamel.basic.gui;

/**
 * When something went wrong while creating an {@link HtmlElement}.
 */
class ErrorElement implements HtmlElement {

	@Override
	public String getText() {
		return "<font color=\"red\">Error</font>";
	}

}

// ***
