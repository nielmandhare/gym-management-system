// Selenium IDE runner 4.0.13 builds an unquoted shell command for Jest.
// Preserve its test engine while passing Windows paths as actual argv entries.
const path = require('path');
const childProcess = require('child_process');
const originalSpawn = childProcess.spawn;
childProcess.spawn = function(command,args,options) {
  if (typeof command === 'string' && command.startsWith('node ')) {
    return originalSpawn(process.execPath,[command.slice(5),...args],{...options,shell:false});
  }
  return originalSpawn(command,args,options);
};
const root=path.resolve(__dirname,'../..');
const runner=path.join(root,'.local/selenium-ide/node_modules/selenium-side-runner/dist/bin.js');
require.main.paths.unshift(path.resolve(path.dirname(require.resolve(runner)),'../..'));
process.argv[1]=runner;
require(runner);
