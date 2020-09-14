local util = require("cfgGen.util")
local asset = {}

---@class asset.asset
local name2index = {
    guid = 1,
    path = 2,
    eName = 3,
    bundleName = 4,
    assetName = 5,
}

local name2ref = {}

local data = {
    [1] = { 1, "asset/icon/dog.png", "DOG", "bundle/icon", "dog", },
    [2] = { 2, "asset/icon/cat.png", "CAT", "bundle/icon", "cat", },
}

---@return asset.asset
function asset.get(id)
    return data[id]
end

util.setMetaGet(data, name2index, name2ref)

asset.DOG = asset.get(1)
asset.CAT = asset.get(2)

return asset