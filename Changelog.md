Changelog
=========

Version 0.16.0
--------------
* *attention* changed export folder structure for views 
* changed id generation of plantuml views to make the plantuml output
  location more reasonable
* added rendering of cardinalities for fields in UML class diagrams
* enhanced enum modelling and rendering in UML class diagrams
* enhanced method modelling, added parameters
* extracted criteria predicates to named functions
* added selection criteria
* updated overarch data-model and documentation


Version 0.15.1
--------------
* fixed handling of generation-config option
* removed debug output


Version 0.15.0
--------------
* added generation of artifacts via templates
  * integrated comb template engine
* added selection criteria
* added specs for criteria
* added analytics functions
* added accessors in model-repository
* added hierarchy related functions for ancestor/descendant nodes
* enhanced model-info output
* refactored view rendering rules
* updated tests
* updated documentation


Version 0.14.0
--------------
* added selection of model elements by criteria
* added options for element/reference selection in CLI
* added :tags key to model nodes and relations
  * can be used in element selection
* added check for unmatched relation namespaces
* updated graphviz rendering to use style="filled" on nodes
* updated tests
* updated documentation


Version 0.13.0
--------------
* added semantic relation types in concept model (:is-a, :has)
* added containers from architecture model to allowed actors in use case model
* added rendering of element subtype in markdown (e.g Database Container)
* added an explicit example of a concept model and view
* fixed rendering of :tech attribute for systems in C4 diagrams
* restructured overarch models
* updated documentation


Version 0.12.0
--------------
* enhanched dispatch hierarchy for views
  * fixes missing dispatch values in renderers


Version 0.11.0
--------------
* added include specifications to graphviz and markdown renderers
* updated overarch and test models
* updated documentation
* fixed rendering of references in markdown
* fixed referrer-id->relations lookup creation for nodes


Version 0.10.0
--------------
* added support for new semantic relation types in architecture and deployment models
  * additional architecture relations
    * :request, :response, :publish, :subscribe, :send, :dataflow
  * additional deployment relation
    * :link
* added reporting for unresolvable references in relations
* added support for references in markdown rendering
  * configurable per view via spec
* added support for themes to make consistent styling easier
* removed rendering of ids as transition names in statemachines
* separated domain model from external data representation
  * domain model can be navigated relationally and hierarchically
* separated rendering from file output
* added/refactored model accessor functions
* added/refactored element predicates and type sets
* refactored and enhanced model spec and checks
  * use expound for spec error reporting (not ideal)
* refactored and enhanced tests
* refactored and enhanced example models
* updated documentation


Version 0.9.0
-------------
* added model warnings for inconsistencies
* added reporting for unresolvable references in views
* added support for skinparams nodesep and ranksep for plantuml
* enhanced model analytics functions
* enhanced model information
* refactored reference resolution
* refactored and enhanced hierarchical model traversal
* fixed arity exception in structurizr export


Version 0.8.0
-------------
* support for loading models from multiple locations by specifying a model path
* (WIP) adding include specification for views
* refactored codebase to clean architecture for better maintainablity/extensibility
* added a namespace for each kind of view for view specific logic
* added separate namespaces for plantuml UML and C4 rendering
* added tests for model and view logic
* added analytics namespace for model analytics
* extracted spec namespace
* added compilation tests for each namespace


Version 0.7.0
-------------
* make file watches work on Macs by using beholder instead of hawk 
* added direction rendering to relations in class and state machine views
* enhanced example models


Version 0.6.0
-------------
* refactored exports, distinguish between
  * exports of model data (to JSON, structurizr)
  * rendering of views (e.g. to PlantUML)
* changed command line options to reflect the refactoring
* added render-format 'all' to generate all formats in one go
* updated usage and design documentation and diagrams


Version 0.5.0
-------------
* fixed and enhanced class view rendering
* enhanced example models


Version 0.4.0
-------------
* added concept view for concept maps
* added graphviz export for concept view
* updated logical data model to incorporate enhancements
* enhanced example models
* updated and enhanced usage documentation
* updated design document


Version 0.3.0
-------------
* added first markdown export
* added concept model elements
* added glossary view (textual view)
* added logical data model for overarch
* enhanced documentation and examples


Version 0.2.0
-------------
* added sprite includes for :sprite entries
* added support for UML use case, state and class views/diagrams
* enhanced documentation


Version 0.1.0
-------------
* initial import
* data format specification
* data loading
* support for views
  * system landscape
  * system context
  * container
  * component
  * deployment
  * dynamic
* command line interface
  * exports to json, plantuml and structurizr
  * file system watch for exports on changes
  * print sprite mappings
  * infos about the loaded model
* json export
  * based on the individual EDN files 
* plantuml export
  * styling support
  * sprite support
* structurizr export *experimental*
  * export structurizr workspace with model and views

