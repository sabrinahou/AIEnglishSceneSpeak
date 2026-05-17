const fs = require("fs");
const path = require("path");
const {
	spawnSync
} = require("child_process");
const rootDir = path.resolve(__dirname, "..");
const statusPath = path.join(rootDir, "status.json");
const gradlePath = path.join(rootDir, "app", "build.gradle.kts");
const gradleWrapper = process.platform === "win32" ? "gradlew.bat" : "./gradlew";
const resetStatus = {
	needsUpdate: false,
	majorUp: false,
	minorUp: false,
	useVersion: null
};

function readJson(filePath) {
	return JSON.parse(fs.readFileSync(filePath, "utf8"))
}

function writeGitHubOutput(name, value) {
	if (!process.env.GITHUB_OUTPUT) {
		return
	}
	fs.appendFileSync(process.env.GITHUB_OUTPUT, `${name}=${value}\n`)
}

function resetStatusFile() {
	fs.writeFileSync(statusPath, `${JSON.stringify(resetStatus, null, "\t")}\n`);
	console.log("status.json reset.")
}

function parseVersion(version) {
	const match = /^(\d+)\.(\d+)\.(\d+)$/.exec(version);
	if (!match) {
		throw new Error(`versionName must use x.y.z format before auto bumping. Current value: ${version}`)
	}
	return {
		major: Number(match[1]),
		minor: Number(match[2]),
		patch: Number(match[3])
	}
}

function resolveNextVersion(currentVersion, status) {
	if (status.useVersion && typeof status.useVersion === "string") {
		console.log(`Version set manually to ${status.useVersion}`);
		return status.useVersion
	}
	const {
		major,
		minor,
		patch
	} = parseVersion(currentVersion);
	if (status.majorUp) {
		const nextVersion = `${major+1}.0.0`;
		console.log(`Major version bumped to ${nextVersion}`);
		return nextVersion
	}
	if (status.minorUp) {
		const nextVersion = `${major}.${minor+1}.0`;
		console.log(`Minor version bumped to ${nextVersion}`);
		return nextVersion
	}
	const nextVersion = `${major}.${minor}.${patch+1}`;
	console.log(`Patch version bumped to ${nextVersion}`);
	return nextVersion
}

function updateGradleVersion(status) {
	const source = fs.readFileSync(gradlePath, "utf8");
	const versionCodeMatch = source.match(/versionCode\s*=\s*(\d+)/);
	const versionNameMatch = source.match(/versionName\s*=\s*"([^"]+)"/);
	if (!versionCodeMatch || !versionNameMatch) {
		throw new Error("Could not find versionCode or versionName in app/build.gradle.kts")
	}
	const currentCode = Number(versionCodeMatch[1]);
	const currentName = versionNameMatch[1];
	const nextCode = currentCode + 1;
	const nextName = resolveNextVersion(currentName, status);
	const updated = source.replace(/versionCode\s*=\s*\d+/, `versionCode = ${nextCode}`).replace(/versionName\s*=\s*"[^"]+"/, `versionName = "${nextName}"`);
	fs.writeFileSync(gradlePath, updated);
	console.log(`versionCode bumped to ${nextCode}`)
}

function runReleaseBuild() {
	const result = spawnSync(gradleWrapper, ["assembleRelease"], {
		cwd: rootDir,
		shell: process.platform === "win32",
		stdio: "inherit"
	});
	if (result.status !== 0) {
		throw new Error(`Release build failed with exit code ${result.status}`)
	}
}

function main() {
	const status = readJson(statusPath);
	if (!status.needsUpdate) {
		console.log("needsUpdate is false. Release skipped.");
		writeGitHubOutput("released", "false");
		return
	}
	updateGradleVersion(status);
	runReleaseBuild();
	resetStatusFile();
	writeGitHubOutput("released", "true");
	console.log("Release build completed.")
}
main();
