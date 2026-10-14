"""Local-only Kotlin host runner. It discovers existing approved cache jars; never downloads."""
import argparse, pathlib, subprocess, tempfile, os
ROOT = pathlib.Path(__file__).resolve().parents[2]

def args():
    p=argparse.ArgumentParser()
    p.add_argument('--gradle-cache',type=pathlib.Path,required=True)
    p.add_argument('--java-home',type=pathlib.Path,required=True)
    return p.parse_args()

def run(sources, main, config):
    cache=config.gradle_cache/'caches/modules-2/files-2.1'
    def jar(group, artifact, version=None):
        base=cache/group/artifact
        hits=sorted((base/version if version else base).rglob('*.jar'))
        if not hits: raise RuntimeError(f'Missing approved local jar: {base}/{version or "*"}')
        return str(hits[-1])
    compiler=[jar('org.jetbrains.kotlin',a,'2.4.10') for a in ['kotlin-compiler-embeddable','kotlin-stdlib','kotlin-build-tools-api','kotlin-daemon-embeddable','kotlin-script-runtime']]
    compiler += [jar('org.jetbrains.kotlin','kotlin-reflect'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm'),jar('org.jetbrains','annotations')]
    stdlib=jar('org.jetbrains.kotlin','kotlin-stdlib','2.4.10')
    java=str(config.java_home/'bin/java')
    with tempfile.TemporaryDirectory(prefix='rpn-host-') as d:
        d=pathlib.Path(d); inputs=[]
        for name, content in sources.items():
            p=d/name;p.write_text(content);inputs.append(str(p))
        out=d/'classes'
        subprocess.run([java,'-cp',os.pathsep.join(compiler),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-classpath',stdlib,'-d',str(out),*inputs],check=True)
        subprocess.run([java,'-cp',os.pathsep.join([str(out),stdlib]),main],check=True)
