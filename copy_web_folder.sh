# shell script to push the web folder for https://numberoneguy.github.io/actx-dev.github.io/
mkdir -p /root/actx-web/
shopt -s extglob
cp /root/ACTX-source-code/target_teavm_wasm_gc/javascript_dist/!(ActX_1.8_WASM-GC_Offline_Download.html) /root/actx-web/ && cd /root/actx-web/
git add .
git commit -m "actx dev"
git push -u origin web

