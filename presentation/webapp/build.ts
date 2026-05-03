await Bun.build({
    entrypoints: ['./ts/index.ts'],
    outdir: '../src/main/resources/static/dist',
    target: 'browser',
    external: ['/*'],
    minify: true
});