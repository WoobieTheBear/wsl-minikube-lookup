import express from 'express';
import path from 'path';

const frontendPort = 1337;
const app = express();
app.use(express.json());

app.use(express.static(path.resolve('./public')));

app.get('/theimage', async (req, res) => {
    res.send({ message: 'you found the image' });
});

app.listen(frontendPort, () => {
    console.log(`[pod] app is running on port: ${frontendPort}`);
});