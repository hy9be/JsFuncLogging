# JsFuncLogging #
A instrumentation tool to process js source code, so that all function calls are printed into console.

## Motivation ##
The DevTool of main stream browsers cannot help to profile the browser freeze / hang issues since the main thread is blocked. Now developers have to trace the code to find the root cause. This tool is mean to help the investigation by logging all functions calls.

## How it works ##
The tool instruments javascript source code files / bundles offline. ``console.log`` code is added at the entry and exit of any functions.

## Example ##
For one-tier, we can modify the paths in the client.Main class to generate the instrumented one-tier bundle. By copying the new bundle back to replace the original bundle in code/ folder, you could see the function call logs in the DevTool of CEF.

* By clicking the row number associated with the log in browser console, you will be navigated to the source code line. But it will be more ideal when using the debug-bundle instead, which is not minified with full name for all classes and functions.

## Warning ##
The instrumented javascript code will generate large amount of logs in the console. Thus try to minimize the actions needed to reproduce the freeze / hang, to avoid high memory usage of browser / CEF.
  
## Credits ##
The tool used the javascript parser in ``Rhino`` js engine. It always borrowed some code from ``jscover`` and ``clematis`` projects on github.