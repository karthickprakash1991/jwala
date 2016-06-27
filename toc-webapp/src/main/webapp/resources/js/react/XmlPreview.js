/** @jsx React.DOM */
var XmlPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        return <div ref="theContainer" className="xml-preview-container">
                   <RTextPreview ref="textPreview">{this.state.content}</RTextPreview>
               </div>
    },
    refresh: function(content) {
        this.setState({content: content});
    },
    resize: function() {
        var textPreviewHeight = $(this.refs.theContainer.getDOMNode()).height() -
                                $(this.refs.theToolbar.getDOMNode()).height() - XmlPreview.SPLITTER_DISTANCE_FROM_TOOLBAR;
        $(this.refs.textPreview.getDOMNode()).css("height", textPreviewHeight);
    },
    statics: {
        SPLITTER_DISTANCE_FROM_TOOLBAR: 19
    }
})